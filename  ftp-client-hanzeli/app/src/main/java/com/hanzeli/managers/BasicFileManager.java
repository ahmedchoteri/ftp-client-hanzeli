package com.hanzeli.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import android.os.AsyncTask;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Order;

public abstract class BasicFileManager implements Manager{

	/** current directory */
	protected String currentDir;	
	/** root directory */
	protected String rootDir;		
	/** status of connection */
	protected boolean connection;	
	/** list with information about files */
	protected ArrayList<FileInfo> fileList;
	/** File manager listeners */
	protected ManagerListener listener;
	/** Order */
	protected Ordering orderingCompare;
	//protected Ordering orderingAscDesc;
	

	public BasicFileManager() {
		currentDir = null;
		rootDir = null;
		connection = false;
		fileList = null;
		listener = null;
		orderingCompare = new Ordering(Order.NAME,Order.ASC);
		
		
	}

	public String getCurrDir() {
		return currentDir;
	}
	
	public String getRootDir(){
		return rootDir;
	}
	
	public boolean isConnected() {
		return connection;
	}
	
	public boolean isRootFolder(){
		return rootDir.equals(currentDir);
	}
	
	public ArrayList<FileInfo> getFiles() {
		return fileList;
	}

    public ArrayList<FileInfo> getSelectedFiles() {
        ArrayList<FileInfo> filesOut = new ArrayList<FileInfo>();
        for(FileInfo fi : fileList){
            if (fi.getChecked()){
                filesOut.add(fi);
            }
        }
        return filesOut;
    }

    public void selectFile(int position) {
        boolean b = fileList.get(position).getChecked();
        fileList.get(position).setChecked(!b);
    }

    public void selectAllFiles(boolean checked){
        for(FileInfo file : fileList){
            file.setChecked(checked);
        }
    }

    /**
     * zmazanie oznacenych suborov
     */
    public void delFiles() {
        ArrayList<FileInfo> filesToDelete = getSelectedFiles();
        FileInfo[] aFiles = new FileInfo[filesToDelete.size()];
        filesToDelete.toArray(aFiles);

        new ManagerTaskHandler(ManagerTask.DELETE).execute(aFiles);
    }

    public void setClient(FTPClient client){
		
	}
	
	public FTPClient getClient(){
		return null;
	}
	
	public void connect() {
		new ManagerTaskHandler(ManagerTask.CONNECT).execute();
	}
	
	public void disconnect() {
		new ManagerTaskHandler(ManagerTask.DISCONNECT).execute();
	}
	
	/**
	 * registering a new listener
	 * @param listener for this manager
	 */
	public void attach(ManagerListener listener) {
		this.listener=listener;
			//ManagerEvent newManagerEvent = new ManagerEvent(EventTypes.FILES_LIST_CHANGE);
			//newManagerEvent.setManager(this);
			//listener.managerEvent(newManagerEvent);
	}

    /**
     * removing listener
     */
    public void detach(){
        this.listener=null;
    }

	/**
	 * notifying listeners of this manager that event happened
	 * @param event
	 */
	protected void notifyListener(ManagerEvent event) {
		if (listener!=null) {
			event.setManager(this);
			listener.managerEvent(event);
		}
	}

	/** 
	 * notifying listeners of this manager that more than one events happened 
	 * @param events list of events
	 */
	protected void notifyListener(List<ManagerEvent> events) {
		for (ManagerEvent event : events) {
			notifyListener(event);
		}
	}
	
	
	public void toParDir() {
		// Can we go to parent folder
		if (!isRootFolder()) {
			new ManagerTaskHandler(ManagerTask.GO_PARENT).execute();
		}
	}

	public void toHomeDir(){
		if (!isRootFolder()){
			new ManagerTaskHandler(ManagerTask.GO_ROOT).execute();
		}
	}
	
	public boolean existParent() {
		return !isRootFolder();
	}
	
	
	public void chngWorkDir(final String dirname) {
		FileInfo dir = null;
		for (FileInfo file : fileList) {
			if (dirname.equals(file.getName())) {
				dir = file;
				break;
			}
		}

		new ManagerTaskHandler(ManagerTask.CHNG_DIR).execute(dir);
	}

	/**
	 * change type of ordering
	 */
	public void chngOrdering(final Order order) {
		//setup for ordering
		orderingCompare.setType(order);
		//reorder list of files
		if ((fileList != null) && !fileList.isEmpty()) {
			new ManagerTaskHandler(ManagerTask.CHNG_ORDER).execute();
		}
	}

	/**
	 * change order ASC/DESC
	 */
	public void chngOrderingAscDesc(final Order order){
		orderingCompare.setOrder(order);
		if ((fileList != null) && !fileList.isEmpty()) {
			new ManagerTaskHandler(ManagerTask.CHNG_ORDER).execute();
		}
	}
	/**
	 * 
	 */
	public void newFolder(final String dirname) {

		//check if file already exist
		if ((fileList != null) && !fileList.isEmpty()) {
			//nemusi byt lowerCase !!!
			for (FileInfo fileEntry : fileList) {
				if (dirname.equals(fileEntry.getName())) {
					notifyListener(new ManagerEvent(EventTypes.NEW_FOLDER_ERR));
					return;
				}
			}
		}

		FileInfo dir = new FileInfo(dirname);
		if (currentDir.endsWith(File.separator)) {
			dir.setAbsPath(currentDir + dirname);
		} else {
			dir.setAbsPath(currentDir + File.separator + dirname);
		}

		new ManagerTaskHandler(ManagerTask.NEW_FOLDER).execute(dir);
	}

	/**
	 * 
	 */
	public void renameFile(final String fileName, final String newFileName) {

		boolean isSameName = newFileName.equals(fileName);
		if (isSameName) {
			return;
		}

		// Old file check if a file with the same name already exist
		FileInfo file = null;
		for (FileInfo fileEntry : fileList) {

			if ((file == null) && fileName.equals(fileEntry.getName())) {
				file = fileEntry;
			}

			if (newFileName.equals(fileEntry.getName())) {
				notifyListener(new ManagerEvent(EventTypes.RENAME_ERR));
				return;
			}
		}

		// New file
		FileInfo newFile = new FileInfo(newFileName);
		if (currentDir.endsWith(File.separator)) {
			newFile.setAbsPath(currentDir + newFileName);
		} else {
			newFile.setAbsPath(currentDir + File.separator + newFileName);
		}

		new ManagerTaskHandler(ManagerTask.RENAME).execute(file, newFile);
	}

	/**
	 *
	 */
	public void refresh() {
		new ManagerTaskHandler(ManagerTask.REFRESH).execute();
	}

	/**
	 * second thread class for running manager operations
	 */
	private class ManagerTaskHandler extends AsyncTask<FileInfo, Void, AsyncTaskResult> {

		/** new task for this thread */
		private ManagerTask handlerTask;

		public ManagerTaskHandler(ManagerTask task) {
			handlerTask = task;
		}

		/**
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//notifying listeners about events of this task
			notifyListener(handlerTask.getStartEvents());
		}

		/**
		 *
		 */
		@Override
		protected AsyncTaskResult doInBackground(FileInfo... params) {
			AsyncTaskResult result = new AsyncTaskResult();

			try {
				switch (handlerTask.getTask()) {

					case CONNECT:
						execConnect();
						break;	
					case DISCONNECT:
						execDisconnect();
					case CHNG_DIR:
						execChngWorkDir(params[0]);
						break;			
					case GO_PARENT:
						execToParDir();
						break;
					case GO_ROOT:
						execToHomeDir();
					case REFRESH:
						execRefresh();
						break;
					case RENAME:
						execRename(params[0],params[1]);
						break;
					case DELETE:
						execDelete(params);
						break;
					case NEW_FOLDER:
						execNewFolder(params[0]);
						break;
					case CHNG_ORDER:
						Collections.sort(fileList, orderingCompare);
						break;
					default:
						break;
					
				}

				result.setResult(true);

			} catch (ManagerException e) {
				result.setResult(false);
				result.addEvent(new ManagerEvent(e.getEvent()));
			}
			return result;
		}

		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(AsyncTaskResult result) {
			super.onPostExecute(result);

			//sending ending events after successful task
			if (result.getResult()) {
				notifyListener(handlerTask.getEndEvents());
			}
			
			
			// additional events if error will occur
			if (!result.getEvents().isEmpty()) {
				notifyListener(result.getEvents());
			}
			
		}
	}

	/**
	 * method for connecting to local disk or to server disk
	 */
	protected abstract void execConnect() throws ManagerException;
	
	/**
	 * method for disconnecting remote manager
	 */
	protected abstract void execDisconnect() throws ManagerException;
	
	/**
	 * method for change of directory operation
	 * @param dir - target directory
	 */
	protected abstract void execChngWorkDir(FileInfo dir) throws ManagerException;
	
	/**
	 * method for jump to parent operation
	 */
	protected abstract void execToParDir() throws ManagerException;
	
	/**
	 * method for jump to root directory
	 */
	
	protected abstract void execToHomeDir() throws ManagerException;
	/**
	 * method for refreshing list of files
	 */
	protected abstract void execRefresh() throws ManagerException;

	/**
	 * method for renaming renaming file
	 * @param file - file to rename
	 * @param newFile - file with new name
	 */
	protected abstract void execRename(FileInfo file, FileInfo newFile) throws ManagerException;
	
	/**
	 * method for deleting file
	 * @param files - list with files to delete
	 */
	protected abstract void execDelete(FileInfo[] files) throws ManagerException;

	/**
	 * method for creating new folder
	 * @param dir - new folder
	 */
	protected abstract void execNewFolder(FileInfo dir) throws ManagerException;

	

}