package com.hanzeli.managers;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	protected List<FileInfo> filesInfo;
	/** File manager listeners */
	protected Set<ManagerListener> allListeners;
	/** Order */
	protected Ordering orderingCompare;
	//protected Ordering orderingAscDesc;
	

	
	public BasicFileManager() {
		currentDir = null;
		rootDir = null;
		connection = false;
		filesInfo = null;
		allListeners = new HashSet<ManagerListener>();
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
	
	public List<FileInfo> getFiles() {
		return filesInfo;
	}
	
	
	
	public void connect() {
		new ManagerTaskHandler(ManagerTask.CONNECT).execute();
	}
	
	public void disconnect() {
		new ManagerTaskHandler(ManagerTask.DISCONNECT).execute();
	}
	
	/**
	 * 
	 */
	public void delFiles(final List<FileInfo> files) {

		FileInfo[] aFiles = new FileInfo[files.size()];
		files.toArray(aFiles);

		new ManagerTaskHandler(ManagerTask.DELETE).execute(aFiles);
	}
	
	/**
	 * registering a new listener
	 * @param new listener for this manager
	 */
	public void addListener(ManagerListener listener) {
		if (!allListeners.contains(listener)) {
			allListeners.add(listener);
			
			ManagerEvent newManagerEvent = new ManagerEvent(EventTypes.FILES_LIST_CHANGE);
			newManagerEvent.setManager(this);
			listener.managerEvent(newManagerEvent);
		}
	}

	/**
	 * notifying listeners of this manager that event happened
	 * @param event
	 */
	protected void notifyListeners(ManagerEvent event) {
		if (!allListeners.isEmpty()) {
			event.setManager(this);
			for (ManagerListener listener : allListeners) {
				listener.managerEvent(event);
			}
		}
	}

	/** 
	 * notifying listeners of this manager that more than one events happened 
	 * @param list of events
	 */
	protected void notifyListeners(List<ManagerEvent> events) {
		for (ManagerEvent event : events) {
			notifyListeners(event);
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
		for (FileInfo file : filesInfo) {
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
		if ((filesInfo != null) && !filesInfo.isEmpty()) {
			new ManagerTaskHandler(ManagerTask.CHNG_ORDER).execute();
		}
	}

	/**
	 * change order ASC/DESC
	 */
	public void chngOrderingAscDesc(final Order order){
		orderingCompare.setOrder(order);
		if ((filesInfo != null) && !filesInfo.isEmpty()) {
			new ManagerTaskHandler(ManagerTask.CHNG_ORDER).execute();
		}
	}
	/**
	 * 
	 */
	public void newFolder(final String dirname) {

		//check if file already exist
		if ((filesInfo != null) && !filesInfo.isEmpty()) {
			//nemusi byt lowerCase !!!
			for (FileInfo fileEntry : filesInfo) {
				if (dirname.equals(fileEntry.getName())) {
					notifyListeners(new ManagerEvent(EventTypes.NEW_FOLDER_ERR));
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
		for (FileInfo fileEntry : filesInfo) {

			if ((file == null) && fileName.equals(fileEntry.getName())) {
				file = fileEntry;
			}

			if (newFileName.equals(fileEntry.getName())) {
				notifyListeners(new ManagerEvent(EventTypes.RENAME_ERR));
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
	 * @see net.abachar.androftp.filelist.manager.FileManager#refresh()
	 */
	public void refresh() {
		new ManagerTaskHandler(ManagerTask.REFRESH).execute();
	}

	/**
	 * second thread class for running manager operations
	 */
	private class ManagerTaskHandler extends AsyncTask<FileInfo, ManagerEvent, HandlerResult> implements ManagerTaskListener {

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
			notifyListeners(handlerTask.getStartEvents());
		}

		/**
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected HandlerResult doInBackground(FileInfo... params) {
			HandlerResult result = new HandlerResult();

			try {
				switch (handlerTask.getTask()) {

					case CONNECT:
						execConnect(this);
						break;	
					case DISCONNECT:
						execDisconnect(this);
					case CHNG_DIR:
						execChngWorkDir(this, params[0]);
						break;			
					case GO_PARENT:
						execToParDir(this);
						break;
					case GO_ROOT:
						execToHomeDir(this);
					case REFRESH:
						execRefresh(this);
						break;
					case RENAME:
						execRename(this,params[0],params[1]);
						break;
					case DELETE:
						execDelete(this,params);
						break;
					case NEW_FOLDER:
						execNewFolder(this,params[0]);
						break;
					case CHNG_ORDER:
						Collections.sort(filesInfo, orderingCompare);
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
		 * publishing about progress of this task
		 */
		public void onPublishProgress(ManagerEvent... values) {
			publishProgress(values);
		}

		/**
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(ManagerEvent... values) {
			super.onProgressUpdate(values);
			//notifying listeners
			for (ManagerEvent value : values) {
				notifyListeners(value);
			}
		}

		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(HandlerResult result) {
			super.onPostExecute(result);

			//sending ending events after successful task
			if (result.getResult()) {
				notifyListeners(handlerTask.getEndEvents());
			}
			
			
			// additional events if error will occur
			if (!result.getEvents().isEmpty()) {
				notifyListeners(result.getEvents());
			}
			
		}
	}

	/**
	 * method for connecting to local disk or to server disk
	 * @param listener - listener for this operation
	 */
	protected abstract void execConnect(ManagerTaskListener listener) throws ManagerException;
	
	/**
	 * method for disconnecting remote manager
	 */
	protected abstract void execDisconnect(ManagerTaskListener listener) throws ManagerException;
	
	/**
	 * method for change of directory operation
	 * @param listener - listener for this operation
	 * @param dir - target directory
	 */
	protected abstract void execChngWorkDir(ManagerTaskListener listener, FileInfo dir) throws ManagerException;
	
	/**
	 * method for jump to parent operation
	 * @param listener - listener for this operation
	 */
	protected abstract void execToParDir(ManagerTaskListener listener) throws ManagerException;
	
	/**
	 * method for jump to root directory
	 */
	
	protected abstract void execToHomeDir(ManagerTaskListener listener) throws ManagerException;
	/**
	 * method for refreshing list of files
	 * @param listener - listener for this operation
	 */
	protected abstract void execRefresh(ManagerTaskListener listener) throws ManagerException;

	/**
	 * method for renaming renaming file
	 * @param listener - listener for this operation
	 * @param file - file to rename
	 * @param newFile - file with new name
	 */
	protected abstract void execRename(ManagerTaskListener listener, FileInfo file, FileInfo newFile) throws ManagerException;
	
	/**
	 * method for deleting file
	 * @param listener - listener for this operation
	 * @param files - list with files to delete
	 */
	protected abstract void execDelete(ManagerTaskListener listener, FileInfo[] files) throws ManagerException;

	/**
	 * method for creating new folder
	 * @param listener - listener for this operation
	 * @param dir - new folder 
	 */
	protected abstract void execNewFolder(ManagerTaskListener listener, FileInfo dir) throws ManagerException;

	

}
