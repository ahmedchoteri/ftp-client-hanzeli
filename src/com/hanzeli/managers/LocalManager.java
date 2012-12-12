package com.hanzeli.managers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.os.Environment;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Order;

public class LocalManager extends BasicFileManager{
	
	
	public void init(Bundle bundle) {

		//ordering initialization
		if (bundle.containsKey("local_order")) {
			orderingCompare = new Ordering((Order) bundle.get("local.orderBy"),Order.ASC);
		}
		//paths initialization
		if (bundle.containsKey("local_dir")) {
			rootDir = bundle.getString("local_dir");
		} else {
			//the root will be external storage - SD card
			rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		if (bundle.containsKey("local_dir")) {
			currentDir = bundle.getString("local_dir");
		} else {
			currentDir = rootDir;
		}
		
	}
	
	private void loadFilesInfo() {
		//remove old list
		filesInfo = null;
		//list of files in current directory without hidden files
		File[] list = (new File(currentDir)).listFiles(new FileFilter() {

			public boolean accept(File file) {
				return !file.isHidden() && (file.isDirectory() ||  file.isFile());
			}
		});
		//load information about each file and put it in the manager's filesInfo
		if ((list != null) && (list.length > 0)) {
			filesInfo = new ArrayList<FileInfo>();
			for (File file : list) {
				FileInfo fi = new FileInfo(file.getName());
				fi.setSize(file.length());
				fi.setLastModif(file.lastModified());
				fi.setAbsPath(file.getAbsolutePath());
				fi.setParentPath(currentDir);	
				fi.setType(FileTypes.getType(file));
				filesInfo.add(fi);
			}
			//sorting of filesInfo
			Collections.sort(filesInfo, orderingCompare);
		}
	}
	
	@Override
	protected void execConnect(ManagerTaskListener listener) throws ManagerException{

		File directory = new File(currentDir);
		//testing if directory is accessible
		if (directory.canRead() && directory.isDirectory()) {
			loadFilesInfo();	//load the list of files in current directory
		}
		connection = true;
	}
	
	@Override
	protected void execDisconnect(ManagerTaskListener listener) throws ManagerException{
		//nothing in local manager
	}
	
	@Override
	protected void execChngWorkDir(ManagerTaskListener listener, FileInfo file) throws ManagerException{
		File directory = new File(file.getAbsPath());
		//testing if directory is accessible
		if (directory.canRead() && directory.isDirectory()) {
			currentDir = directory.getAbsolutePath();
			loadFilesInfo();	//load the list of files in current directory
		}
	}
	
	@Override
	protected void execToParDir(ManagerTaskListener listener) throws ManagerException{
		File directory = new File(currentDir);		
		currentDir = directory.getParent();
		loadFilesInfo();	//load the list of files in current directory
	}
	
	@Override
	protected void execToHomeDir(ManagerTaskListener listener) throws ManagerException{
		currentDir = rootDir;
		loadFilesInfo();
	}
	
	@Override
	protected void execRefresh(ManagerTaskListener listener){
		loadFilesInfo();	//load the list of files in current directory
	}
	
	@Override
	protected void execRename(ManagerTaskListener listener, FileInfo oldFile, FileInfo newFile) throws ManagerException{
		File fo = new File(oldFile.getAbsPath());
		File fn = new File(newFile.getAbsPath());
		//try to rename old file
		//spravit exception aj na ine chyby
		if (fo.renameTo(fn)) {
			loadFilesInfo();
		} else {
			throw new ManagerException(EventTypes.RENAME_ERR);
		}
	}
	
	@Override
	protected void execDelete(ManagerTaskListener listener, FileInfo[] files) throws ManagerException{
		for (FileInfo info : files) {
			File file = new File(info.getAbsPath());
			if (!file.delete()) {
				if (info.isFolder()) {
					throw new ManagerException(EventTypes.DEL_FOLDER_ERR);
				} else {
					throw new ManagerException(EventTypes.DEL_FILE_ERR);
				}
			}
		}
		loadFilesInfo();	//reload after deleting files
	}

	
	@Override
	protected void execNewFolder(ManagerTaskListener listener, FileInfo file) throws ManagerException{
		String newFolder = file.getAbsPath();
		File folder = new File(newFolder);
		if (folder.mkdir()) {
			loadFilesInfo();
		} else {
			throw new ManagerException(EventTypes.NEW_FOLDER_ERR);
		}
	}

	
	

}	
	