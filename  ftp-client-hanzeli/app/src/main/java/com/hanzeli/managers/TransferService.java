package com.hanzeli.managers;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.values.Values;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class TransferService extends Service {
    private final String TAG = "TransferService";

    public static final String TRANSFER = "transfer";

    private final IBinder binder = new TransferBinder();
    private LocalBroadcastManager localBroadcastManager = null;
    private FTPClient client = null;


    public TransferService() {

    }

    @Override
    public void onCreate(){
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG,"Starting service");
        //Transfer transfer = intent.getParcelableExtra(TRANSFER);
        int transferID = intent.getIntExtra(TRANSFER,0);
        Transfer transfer = MainApplication.getInstance().getTransferManager().getTransfer(transferID);
        if(!transfer.copyOp){ connectClient(); }
        TransferHandler trTask = new TransferHandler(transfer, ManagerTask.START_TRANSFER);
        trTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void connectClient(){
        client = MainApplication.getInstance().getTransferManager().getClient();
        client.setControlKeepAliveTimeout(15);
        client.addProtocolCommandListener(new ProtocolCommandListener() {
            public void protocolCommandSent(ProtocolCommandEvent event) {
                Log.d("TS Command sent",event.getMessage());
            }

            public void protocolReplyReceived(ProtocolCommandEvent event) {
                Log.d("TS Command received",event.getMessage());
            }
        });
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
            Log.d(TAG,"Transferring package with size: " + transfer.getSize());
            if(localBroadcastManager == null){Log.d(TAG,"Broadcast manager is null");}
            Log.d(TAG,"Sending broadcast cancel transfer waiting");
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
                        if(transfer.copyOp) {
                            Log.d(TAG,"Starting copy");
                            doCopy();
                        }
                        else {
                            if (transfer.getDirection() == 0) {
                                doDownload();
                            } else doUpload();
                        }
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
            Log.d(TAG, "onPostExecute calling stopSelf");
            stopSelf();
        }
        // ********************************
        // UPLOADING
        // ********************************
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
                /*if(!client.printWorkingDirectory().equals(transfer.getToPath())){
                    client.changeWorkingDirectory(transfer.getToPath());
                }*/
                for (FileInfo info : transfer.transferFiles){
                    if(info.isFolder()){
                        uploadDirectory(transfer.getToPath(),transfer.getFromPath(),info.getName());
                    }
                    else {
                        uploadFile(transfer.getToPath()+ info.getName(),info.getAbsPath(),info.getSize());
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
                Log.d(TAG,"Uploading of file failed");
                throw new ManagerException(Values.UPLOAD_ERROR);
            }
        }

        private void uploadDirectory(String remotePath, String localPath, String name) throws IOException{
            String remoteDirPath = remotePath + File.separatorChar + name;
            String localDirPath = localPath + File.separatorChar + name;
            boolean created = client.makeDirectory(remoteDirPath);
            if(created){
                Log.d(TAG,"UPLOAD: created directory " + remoteDirPath);
            }
            else{
                Log.d(TAG,"UPLOAD: creating directory " + remoteDirPath + " FAILED");
            }
            Log.d(TAG,"UPLOAD: listing directory: " + localDirPath);
            File localDir = new File(localDirPath);
            File[] localSubDir = localDir.listFiles();
            if(localSubDir != null && localSubDir.length>0){
                for (File f : localSubDir){
                    if(f.isFile()){
                        String localFilePath = f.getAbsolutePath();
                        String remoteFilePath = remoteDirPath + File.separator + f.getName();
                        uploadFile(remoteFilePath, localFilePath, f.length());
                    }
                    else{
                        uploadDirectory(remoteDirPath,localDirPath,f.getName());
                    }
                }
            }
        }

        private void uploadFile(String remotePath, String localPath, long size) throws IOException{
            Log.d(TAG,"UPLOAD: transferring file: " + localPath + ", to: " + remotePath);
            FileInputStream fis = new FileInputStream(localPath);
            CountingInputStream cis = new CountingInputStream(fis) {
                int progressOld = transfer.getProgress();
                protected void afterRead(int n) {
                    super.afterRead(n);
                    Log.d(TAG,"Uploaded " + getCount() + " bytes");
                    int progressNew = Math.round(((getCount() + transfer.getTmpSize()) * 100) / transfer.getSize());
                    if (progressNew > progressOld) {
                        publishProgress(progressNew);
                        progressOld = progressNew;
                    }
                }
            };
            try{
                //client.setFileType(FTP.BINARY_FILE_TYPE);
                client.storeFile(remotePath, cis);
                Log.d(TAG,"File uploaded");
                transfer.setTmpSize(transfer.getTmpSize() + size);
            }
            finally {
                cis.close();
            }
        }

        // ********************************
        // DOWNLOADING
        // ********************************
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
                /*if(!client.printWorkingDirectory().equals(transfer.getFromPath())){
                    client.changeWorkingDirectory(transfer.getFromPath());
                }*/
                for (FileInfo info : transfer.transferFiles){
                    if(info.isFolder()){
                        downloadDirectory(transfer.getFromPath(), transfer.getToPath(), info.getName());
                    }
                    else {
                        downloadFile(info.getAbsPath(),transfer.getToPath() + info.getName(),info.getSize());
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
                Log.d(TAG,"Downloading of file failed");
                throw new ManagerException(Values.DOWNLOAD_ERROR);
            }
        }

        private void downloadDirectory(String remotePath, String localPath, String name) throws IOException{
            String remoteDirPath = remotePath + File.separatorChar + name;
            String localDirPath = localPath + File.separatorChar + name;
            File localDir = new File(localDirPath);
            boolean created = localDir.mkdir();
            if(created){
                Log.d(TAG,"DOWNLOAD: created directory " + localDirPath);
            }
            else{
                Log.d(TAG,"DOWNLOAD: creating directory " + localDirPath + " FAILED");
            }
            Log.d(TAG,"DOWNLOAD: listing directory: " + remoteDirPath);
            FTPFile[] remoteSubDir = client.listFiles(remoteDirPath, new FTPFileFilter() {
                public boolean accept(FTPFile file) {
                    return !file.getName().startsWith(".") && !file.isSymbolicLink() && (file.isDirectory() || file.isFile());
                }
            });
            if(remoteSubDir != null && remoteSubDir.length>0){
                for (FTPFile f : remoteSubDir){
                    if(f.isFile()){
                        String remoteFilePath = remoteDirPath + File.separator + f.getName();
                        String localFilePath = localDirPath + File.separator + f.getName();
                        downloadFile(remoteFilePath, localFilePath,  f.getSize());
                    }
                    else{
                        downloadDirectory(remoteDirPath, localDirPath, f.getName());
                    }
                }
            }
        }

        private void downloadFile(String remotePath, String localPath, long size) throws IOException{
            Log.d(TAG,"DOWNLOAD: transferring file: " + remotePath + ", to: " + localPath);
            FileOutputStream fos = new FileOutputStream(localPath);
            CountingOutputStream cos = new CountingOutputStream(fos) {
                int progressOld = 0;
                protected void beforeWrite(int n) {
                    super.beforeWrite(n);
                    Log.d(TAG,"Downloaded " + getCount() + " bytes");
                    int progressNew = Math.round(((getCount() + transfer.getTmpSize()) * 100) / transfer.getSize());
                    if (progressNew > progressOld) {
                        publishProgress(progressNew);
                        progressOld = progressNew;
                    }
                }
            };
            try{
                //client.setFileType(FTP.BINARY_FILE_TYPE);
                client.retrieveFile(remotePath, cos);
                Log.d(TAG,"File downloaded");
                transfer.setTmpSize(transfer.getTmpSize() + size);
            }
            finally {
                cos.close();
            }
        }

        // ********************************
        // COPYING
        // ********************************

        private void doCopy() throws ManagerException{
            Log.d(TAG,"Starting copy operation");
            try{
                for (FileInfo file:transfer.transferFiles){
                    if(file.isFolder()) {
                        copyDirectory(file.getAbsPath(), transfer.getToPath(),file.getName());
                    }
                    else {
                        copyFile(file.getAbsPath(),transfer.getToPath() + File.separatorChar + file.getName(),file.getSize());
                    }
                    if(transfer.cut){
                        File f = new File(file.getAbsPath());
                        boolean succ = f.delete();
                        if(succ){ Log.d(TAG,"COPY: File deleted after copy op"); }
                        else { Log.d(TAG,"COPY: File NOT deleted after copy op"); }
                    }
                }
            } catch (IOException e){
                Log.d(TAG,"Copy ended with error");
                throw new ManagerException(Values.COPY_ERROR);
            }
        }
        private void copyDirectory(String src, String dst,String name) throws IOException{
            Log.d(TAG,"COPY: transfering transferring file: " + src + ", to: " + dst);
            File dstFile = new File(dst + File.separatorChar + name);
            File srcFile = new File(src);
            if(!dstFile.exists()){
                boolean created = dstFile.mkdir();
                if(created){
                    Log.d(TAG,"COPY: created directory " + dst);
                }
                else{
                    Log.d(TAG,"COPY: creating directory " + dst + " FAILED");
                }
            }
            //vypisat subory ktore priecinok obsahuje
            File[] files = srcFile.listFiles();
            for (File file : files) {
                //nove cesty k suborom
                String newSrc = src + File.separatorChar + file.getName();
                String newDest = dst + File.separatorChar + name  ;
                if (file.isFile()){
                    //kopirovanie suboru
                    copyFile(newSrc, newDest + File.separatorChar + file.getName(), file.length());

                }
                else {
                     copyDirectory(newSrc, newDest, file.getName());
                }
                if(transfer.cut){
                    boolean succ = file.delete();
                    if(succ){ Log.d(TAG,"COPY: File deleted after copy op"); }
                    else { Log.d(TAG,"COPY: File NOT deleted after copy op"); }
                }
            }
        }

        private void copyFile(String src, String dst,long size) throws IOException{
            Log.d(TAG,"COPY: transferring file: " + src + ", to: " + dst);
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            byte[] buffer = new byte[1024];
            int progressOld = 0;
            int length;
            int counter = 0;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
                counter += length;
                int progressNew = Math.round(((counter + transfer.getTmpSize()) * 100) / transfer.getSize());
                if (progressNew > progressOld) {
                    publishProgress(progressNew);
                    progressOld = progressNew;
                }
            }
            transfer.setTmpSize(transfer.getTmpSize() + size);
            in.close();
            out.close();
            Log.d(TAG,"COPY: file copied from " + src + " to " + dst);
        }

        private void doStop() throws ManagerException{
            //TODO dorobit odstavenie klienta
        }

    }
}
