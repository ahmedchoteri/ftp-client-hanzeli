package com.hanzeli.transfer;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;



import com.hanzeli.ftpdroid.MainApplication;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.managers.HandlerResult;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerException;
import com.hanzeli.managers.ManagerListener;

public class TransferManager {
	
	private ManagerListener manListener;
	private TransferFragmListener fragListener;
	private int transferNum;
	private List<Transfer> allTrasfers;
	private FTPClient client;
	
	public TransferManager(ManagerListener manList, TransferFragmListener fragList){
		manListener = manList;
		fragListener = fragList;
		allTrasfers= new ArrayList<Transfer>();
		transferNum=1;
	}
	
	public void setClient(FTPClient client){
		this.client=client;
	}
	
	
	public void addNewTransfer(FileInfo file, int direction) {
		
		synchronized(allTrasfers){
			//setting parameters for new transfer
			Transfer tr = new Transfer();
			tr.setId(transferNum);
			tr.setFromPath(file.getParentPath());
			tr.setDone(false);
			tr.setFileName(file.getName());
			tr.setDirection(direction);
			tr.setSize(file.getSize());

			//path to destination according to direction of transfer
			if ( direction==1 ) {
				tr.setToPath(MainApplication.getInstance().getRemoteManager().getCurrDir());
			} else {
				tr.setToPath(MainApplication.getInstance().getLocalManager().getCurrDir());
			}
		
			allTrasfers.add(tr);
		}
		
	}
	
	public void processTransfers() {
		synchronized (allTrasfers) {
			if ((allTrasfers == null) || allTrasfers.isEmpty()) {
				return;
			}
			else{
				new TransferTaskHandler().execute(allTrasfers.toArray(new Transfer[allTrasfers.size()]));
			}
		}
	}
	
	
	
	private class TransferTaskHandler extends AsyncTask<Transfer, Integer, HandlerResult>{

		
		@Override
		protected HandlerResult doInBackground(Transfer... params) {
			HandlerResult result = new HandlerResult();
			try{
				for(Transfer tr : params){
					if(tr.getDirection()==0){
						doDownload(tr);
					}
					else doUpload(tr);
				}
			}catch(ManagerException e){
				result.addEvent(new ManagerEvent(e.getEvent()));
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values){
			super.onProgressUpdate(values);
			fragListener.onProcessUpdate(values[0], values[1]);
		}
		
		@Override
		protected void onPostExecute(HandlerResult result){
			super.onPostExecute(result);
			for(ManagerEvent event : result.getEvents()){
				manListener.managerEvent(event);
			}
		}
		
		private void doDownload(Transfer tr) throws ManagerException{
			
		}
		
		private void doUpload(Transfer tr) throws ManagerException{
			
		}

	}
}
