package com.hanzeli.transfer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import android.os.AsyncTask;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.managers.AsyncTaskResult;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerException;
import com.hanzeli.managers.ManagerListener;
import com.hanzeli.managers.ManagerTask;
import com.hanzeli.values.EventTypes;

public class TransferManager {
	
	private TransferProcessListener fragListener;	//listener for updating status of task
	private int transferNum;
	private List<Transfer> allTransfers;
	//private List<TransferHandler> allTrAsyncTasks;
	private FTPClient client;
	private ManagerListener managerListener;
    private boolean busy;
	
	
	public TransferManager(){	
		allTransfers = new ArrayList<Transfer>();
		//allTrAsyncTasks=new ArrayList<TransferHandler>();
		transferNum=0;
		
		
	}
	
	public void setManList(ManagerListener listener){
		managerListener=listener;
	}
	public void setFragList(TransferProcessListener fragList){
		fragListener = fragList;
	}
	
	public void setClient(FTPClient client){
		this.client=client;
	}
	
	public List<Transfer> getTransfers(){
		return allTransfers;
	}
	
	public void addNewTransfer(FileInfo file, int direction) {
		//setting parameters for new transfer
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
		managerListener.managerEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
	}
	
	public void processTransfers() {

		if (((allTransfers == null) || allTransfers.isEmpty()) && busy) {
			return;
		}
		else{
            Transfer transfer = allTransfers.get(0);
            allTransfers.remove(0);
            busy=true;
			TransferHandler trTask = new TransferHandler(ManagerTask.START_TRANSFER);
			//allTrAsyncTasks.add(trTask);
			trTask.execute(transfer);
			//new TransferTaskHandler(ManagerTask.START_TRANSFER).execute(allTransfers.toArray(new Transfer[allTransfers.size()]));
		}
	}
	
	public void stopTransfers(){
		//TODO stop transfers
	}
	
	
	
	private class TransferHandler extends AsyncTask<Transfer, Integer, AsyncTaskResult>{
		
		private ManagerTask managTask;
		private List<Transfer> allTransfers; 
		private Transfer transfer;
		public TransferHandler(ManagerTask task){
			managTask =  task;
		}
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			ManagerEvent event = new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE);
			managerListener.managerEvent(event);
		}
		
		@Override
		protected AsyncTaskResult doInBackground(Transfer... params) {
			AsyncTaskResult result = new AsyncTaskResult();
            this.transfer = params[0];
			switch(managTask.getTask()){
			case START:
				try{
					if (transfer.isWaiting()){
								transfer.setWaiting(false);
								if(transfer.getDirection()==0){
									doDownload();
								}
								else doUpload();
					}
					
				}catch(ManagerException e){
					result.addEvent(new ManagerEvent(e.getEvent()));
				}
				break;
			case STOP:
				try{
					doStop();
				}catch(ManagerException e){
					result.addEvent(new ManagerEvent(e.getEvent()));
				}
			default:
				break;
			}
			
			return result;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values);
			fragListener.onProcessUpdate(values[0], values[1]);
		}
		
		@Override
		protected void onPostExecute(AsyncTaskResult result){
			super.onPostExecute(result);
			/*for(ManagerEvent event : result.getEvents()){
				manListener.managerEvent(event);
			}*/
		}
		
		private void doDownload() throws ManagerException{
			if (client == null){
				connectClient();
			}
			if (!client.isConnected()){
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
						transfer.setProgress(progress);
						//skusit brzdit publish progress
						publishProgress(transfer.getId(), progress);
					}
				};
				client.retrieveFile(transfer.getFileName(), cos);
				cos.close();
				publishProgress(transfer.getId(),100);
				
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
		private void doUpload() throws ManagerException{
			if (!client.isConnected()){
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
						transfer.setProgress(progress);
						publishProgress(transfer.getId(), progress);
					}
				};
				client.storeFile(transfer.getFileName(), cis);
				cis.close();
				publishProgress(transfer.getId(),100);
			} catch (SocketException e){
				throw new ManagerException(EventTypes.UPLOAD_ERR);
				
			} catch (IOException e){
				throw new ManagerException(EventTypes.UPLOAD_ERR);
			}
		}
		
		private void doStop() throws ManagerException{
			
		}

	}
	
	
	private void connectClient(){
		client = MainApplication.getInstance().getRemoteManager().getClient();
	}
}
