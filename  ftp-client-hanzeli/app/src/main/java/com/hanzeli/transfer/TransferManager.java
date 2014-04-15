package com.hanzeli.transfer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;

import com.hanzeli.fragments.TransferFragment;
import com.hanzeli.karlftp.MainActivity;
import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.values.EventTypes;

public class TransferManager {
	
	private final String TAG = "TransferManager";

    private TransferFragment resultListener;    //listener ktory reaguje na vysledok
    private MainActivity errorListener;         //listener ktory reaguje na chybu
    public int transferNum;
    private ArrayList<Transfer> allTransfers;
    private FTPClient client;
    private boolean busy;


	public TransferManager(){
		allTransfers = new ArrayList<Transfer>();   //TODO ConcurrentLinkedQueue
		transferNum=0;
	}

	public void attachResultListener(TransferFragment listener){
		resultListener=listener;
	}

    public void detachReslutListener(){ resultListener = null; }

    public void attachErrorListener(MainActivity listener){
		errorListener = listener;
	}

    public void detachErrorListener() { errorListener = null; }
	
	public void setClient(FTPClient client){
		this.client=client;
	}
	
	public ArrayList<Transfer> getTransfers(){
		return allTransfers;
	}
	
	public void addNewTransfer(FileInfo file, int direction) {
		//setting parameters for new transfer
        Log.d(TAG,"Adding new transfer");
		Transfer tr = new Transfer();
		tr.setId(transferNum);
		transferNum++;
		tr.setFromPath(file.getParentPath());
		tr.setDone(false);
		tr.setWaiting(true);
		tr.setFileName(file.getName());
		tr.setDirection(direction);
		tr.setSize(file.getSize());
		tr.setProgress(0);
		//path to destination according to direction of transfer
		if ( direction==1 ) {
			tr.setToPath(MainApplication.getInstance().getRemoteManager().getCurrDir());
		} else {
			tr.setToPath(MainApplication.getInstance().getLocalManager().getCurrDir());
		}
		allTransfers.add(tr);
        if (resultListener != null) {
            resultListener.managerEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
	}
	
	public void processTransfers() {

		if (allTransfers != null && !allTransfers.isEmpty() && !busy) {
            Log.d(TAG,"Starting processTransfer");
            Transfer transfer = allTransfers.get(0);
            Intent intent = new Intent(errorListener,TransferService.class);
            intent.putExtra(TransferService.TRANSFER,transfer); //pridanie transfera s ktorym sa ma spravit prenos
            //
            errorListener.startService(intent); //spustenie service od MainActivity
            errorListener.getApplicationContext().bindService(intent,resultListener.connection, Context.BIND_AUTO_CREATE);
            busy=true;
            //TransferHandler trTask = new TransferHandler(transfer, ManagerTask.START_TRANSFER);
            //trTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else{
            Log.d(TAG,"ProcessTransfer started with empty queue");
		}
	}
	
	public void stopTransfers(){
		//stop transfer od managera mi nebude treba pretoze stopovat transfer budem cez fragment
	}


	
//	private class TransferHandler extends AsyncTask<Void, Void, AsyncTaskResult>{
//
//		private ManagerTask managTask;
//		private Transfer transfer;
//		public TransferHandler(Transfer transfer, ManagerTask task){
//			this.transfer = transfer;
//            managTask =  task;
//		}
//
//		@Override
//		protected void onPreExecute(){
//			super.onPreExecute();
//            transfer.setWaiting(false);
//			ManagerEvent event = new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE);
//			resultListener.managerEvent(event);
//		}
//
//		@Override
//		protected AsyncTaskResult doInBackground(Void ... params) {
//			AsyncTaskResult result = new AsyncTaskResult();
//
//            try {
//                switch (managTask.getTask()) {
//                    case START:
//                        if (transfer.getDirection() == 0) {
//                                doDownload();
//                        } else doUpload();
//                        break;
//                    case STOP:
//                        doStop();
//                        break;
//                    default:
//                        break;
//                }
//                result.setResult(true);
//            } catch (ManagerException e){
//                result.setResult(false);
//                result.addmEvent(new ManagerEvent(e.getEvent()));
//            }
//			return result;
//		}
//
//		@Override
//		protected void onProgressUpdate(Void... values){
//			super.onProgressUpdate(values);
//            //update pre progress bar
//            resultListener.onProcessUpdate();
//		}
//
//		@Override
//		protected void onPostExecute(AsyncTaskResult result){
//			super.onPostExecute(result);
//            if(result.getResult()){
//                transfer.setDone(true);
//                ManagerEvent event = new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE);
//                resultListener.managerEvent(event);
//            }else{
//                //nastala chyba a error listener musi reagovat
//                for (ManagerEvent event : result.getmEvents()) {
//                    errorListener.managerEvent(event);
//                }
//            }
//		}
//
//		private void doDownload() throws ManagerException{
//            Log.d(TAG,"Starting download");
//            if (client == null){
//                Log.d(TAG,"client is null during download, reconnecting");
//				connectClient();
//			}
//
//			if (!client.isConnected()){
//                Log.d(TAG,"client is not connected, reconnecting");
//				connectClient();
//			}
//
//			try{
//				if(!client.printWorkingDirectory().equals(transfer.getFromPath())){
//					client.changeWorkingDirectory(transfer.getFromPath());
//				}
//
//				FileOutputStream fos = new FileOutputStream(transfer.getCmplToPath());
//				CountingOutputStream cos = new CountingOutputStream(fos) {
//					protected void beforeWrite(int n) {
//						super.beforeWrite(n);
//
//						int progress = Math.round((getCount() * 100) / transfer.getSize());
//						transfer.setProgress(progress);
//						//skusit brzdit publish progress
//                        publishProgress();
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//					}
//				};
//				client.retrieveFile(transfer.getFileName(), cos);
//				cos.close();
//                transfer.setProgress(100);
//                Log.d(TAG,"File downloaded");
//				publishProgress();
//
//			} catch (IOException e){
//                Log.d(TAG,"Downloading of file failed");
//				throw new ManagerException(EventTypes.DOWNLOAD_ERR);
//			}
//		}
//
//		private void doUpload() throws ManagerException{
//            Log.d(TAG,"Starting upload");
//            if (client == null){
//                Log.d(TAG,"client is null during upload, reconnecting");
//                connectClient();
//            }
//            if (!client.isConnected()){
//                Log.d(TAG,"client is not connected during upload, reconnecting");
//				connectClient();
//			}
//			try{
//				if(!client.printWorkingDirectory().equals(transfer.getToPath())){
//					client.changeWorkingDirectory(transfer.getToPath());
//				}
//
//				FileInputStream fis = new FileInputStream(transfer.getCmplFromPath());
//				CountingInputStream cis = new CountingInputStream(fis) {
//					protected void afterRead(int n) {
//						super.afterRead(n);
//
//						int progress = Math.round((getCount() * 100) / transfer.getSize());
//						transfer.setProgress(progress);
//						publishProgress();
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//					}
//				};
//				client.storeFile(transfer.getFileName(), cis);
//				cis.close();
//                transfer.setProgress(100);
//                Log.d(TAG,"File uploaded");
//				publishProgress();
//			} catch (IOException e){
//                Log.d(TAG,"Uploading of file failed");
//				throw new ManagerException(EventTypes.UPLOAD_ERR);
//			}
//		}
//
//		private void doStop() throws ManagerException{
//			//TODO dorobit odstavenie klienta
//		}
//
//	}
//
//
//	private void connectClient(){
//		client = MainApplication.getInstance().getRemoteManager().getClient();
//	}
}
