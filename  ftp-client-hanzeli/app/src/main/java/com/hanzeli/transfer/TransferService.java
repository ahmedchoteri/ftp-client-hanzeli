package com.hanzeli.transfer;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.managers.AsyncTaskResult;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerException;
import com.hanzeli.managers.ManagerTask;
import com.hanzeli.values.Values;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TransferService extends Service {
    private final String TAG = "TransferService";

    public static final String CLIENT = "client";
    public static final String TRANSFER = "transfer";
    public static final String TASK = "task";

    private final IBinder binder = new TransferBinder();
    private LocalBroadcastManager localBroadcastManager = null;
    private FTPClient client;


    public TransferService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        connectClient();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG,"Starting service");
        Transfer transfer = intent.getParcelableExtra(TRANSFER);
        TransferHandler trTask = new TransferHandler(transfer, ManagerTask.START_TRANSFER);
        trTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_REDELIVER_INTENT;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void connectClient(){
        client = MainApplication.getInstance().getRemoteManager().getClient();
    }

    public class TransferBinder extends Binder{
        public TransferService getService() {
            return TransferService.this;
        }
    }

    public void onDestroy(){
        stopSelf();
        Log.d(TAG,"Stopping service");
        super.onDestroy();
    }

    private class TransferHandler extends AsyncTask<Void, Integer, AsyncTaskResult> {

        private ManagerTask managTask;
        private Transfer transfer;
        private Intent broadcastIntent;
        public TransferHandler(Transfer transfer, ManagerTask task){
            this.transfer = transfer;
            managTask =  task;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            if(localBroadcastManager == null){Log.d(TAG,"Broadcast manager is null");}
            Log.d(TAG,"Sending broadcast transfer waiting");
            broadcastIntent = new Intent(Values.TRANSFER_WAITING);
            broadcastIntent.putExtra(Values.TRANSFER_ID,transfer.getId());
            localBroadcastManager.sendBroadcast(broadcastIntent);
        }

        @Override
        protected AsyncTaskResult doInBackground(Void ... params) {
            AsyncTaskResult result = new AsyncTaskResult();

            try {
                switch (managTask.getTask()) {
                    case START:
                        if (transfer.getDirection() == 0) {
                            doDownload();
                        } else doUpload();
                        break;
                    case STOP:
                        doStop();
                        break;
                    default:
                        break;
                }
                result.setResult(true);
            } catch (ManagerException e){
                result.setResult(false);
                result.addsEvent(e.getValue());
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate(values);
            //update pre progress bar
            Log.d(TAG,"Sending broadcast transfer progress: "+values[0]);
            broadcastIntent = new Intent(Values.TRANSFER_PROGRESS);
            broadcastIntent.putExtra(Values.TRANSFER_ID,transfer.getId());
            broadcastIntent.putExtra(Values.TRANSFER_PROGRESS,values[0]);
            localBroadcastManager.sendBroadcast(broadcastIntent);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult result){

            super.onPostExecute(result);
            if(result.getResult()){
                Log.d(TAG,"Sending broadcast transfer done");
                broadcastIntent = new Intent(Values.TRANSFER_DONE);
                broadcastIntent.putExtra(Values.TRANSFER_ID,transfer.getId());
                localBroadcastManager.sendBroadcast(broadcastIntent);
            }else{
                //nastala chyba a error listener musi reagovat
                for (String event : result.getsEvents()) {
                    Log.d(TAG,"Sending broadcast transfer error");
                    broadcastIntent = new Intent(Values.SERVICE_ERROR);
                    broadcastIntent.putExtra(Values.SERVICE_ERROR,event);
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                }
            }
            //zastavenie service
            Log.d(TAG,"onPostExecute calling stopSelf");
            stopSelf();
        }

        private void doDownload() throws ManagerException{
            Log.d(TAG, "Starting download");
            if (client == null){
                Log.d(TAG,"client is null during download, reconnecting");
                connectClient();
            }

            if (!client.isConnected()){
                Log.d(TAG,"client is not connected, reconnecting");
                connectClient();
            }

            try{
                if(!client.printWorkingDirectory().equals(transfer.getFromPath())){
                    client.changeWorkingDirectory(transfer.getFromPath());
                }

                FileOutputStream fos = new FileOutputStream(transfer.getCmplToPath());
                CountingOutputStream cos = new CountingOutputStream(fos) {
                    protected void beforeWrite(int n) {
                        super.beforeWrite(n);

                        int progress = Math.round((getCount() * 100) / transfer.getSize());
                        publishProgress(progress);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                client.retrieveFile(transfer.getFileName(), cos);
                cos.close();
                Log.d(TAG,"File downloaded");
                publishProgress(100);

            } catch (IOException e){
                Log.d(TAG,"Downloading of file failed");
                throw new ManagerException(Values.DOWNLOAD_ERROR);
            }
        }

        private void doUpload() throws ManagerException{
            Log.d(TAG,"Starting upload");
            if (client == null){
                Log.d(TAG,"client is null during upload, reconnecting");
                connectClient();
            }
            if (!client.isConnected()){
                Log.d(TAG,"client is not connected during upload, reconnecting");
                connectClient();
            }
            try{
                if(!client.printWorkingDirectory().equals(transfer.getToPath())){
                    client.changeWorkingDirectory(transfer.getToPath());
                }

                FileInputStream fis = new FileInputStream(transfer.getCmplFromPath());
                CountingInputStream cis = new CountingInputStream(fis) {
                    protected void afterRead(int n) {
                        super.afterRead(n);

                        int progress = Math.round((getCount() * 100) / transfer.getSize());
                        publishProgress(progress);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                client.storeFile(transfer.getFileName(), cis);
                cis.close();
                Log.d(TAG,"File uploaded");
                publishProgress(100);
            } catch (IOException e){
                Log.d(TAG,"Uploading of file failed");
                throw new ManagerException(Values.UPLOAD_ERROR);
            }
        }

        private void doStop() throws ManagerException{
            //TODO dorobit odstavenie klienta
        }

    }
}
