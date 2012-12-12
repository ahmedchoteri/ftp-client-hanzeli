package com.hanzeli.managers;

import java.util.List;

import com.hanzeli.values.Order;

import android.os.Bundle;

/**
 * Interface with basic operation for file managers
 * @author Michal
 *
 */

public interface Manager {
	
	/**
	 * initialization of manager
	 * @param bundle
	 */
	public void init(Bundle bundle);

	/**
	 * connecting manager operation
	 */
	public void connect();
	
	/**
	 * disconnecting manager operation
	 */
	public void disconnect();
	
	/**
	 * test for connection
	 * @return
	 */
	public boolean isConnected();
	
	/**
	 * list of all displayed files
	 * @return files list
	 */
	public List<FileInfo> getFiles();
	
	/**
	 * deleting files from list
	 * @param files
	 */
	public void delFiles(List<FileInfo> files);
	
	/**
	 * changing to parent directory 
	 */
	public void toParDir();
	
	
	/**
	 * changing to home folder
	 */
	public void toHomeDir();
	
	/**
	 * testing existence of parent
	 * @return true if can go up
	 */
	public boolean existParent();
	
	/**
	 * change currently working directory
	 */
	public void chngWorkDir(String dirname);
	
	/**
	 * create new folder
	 * @param name
	 */
	public void newFolder(String name);
	
	/**
	 * @return the current path
	 */
	public String getCurrDir();
	
	/**
	 * renaming file name
	 * @param fileName
	 * @param newFileName
	 */
	public void renameFile(String fileName, String newFileName);
	
	/**
	 * change type of ordering
	 */
	public void chngOrdering(final Order order);
	/**
	 * change asc/desc ordering
	 * @param order
	 */
	public void chngOrderingAscDesc(final Order order);
	/**
	 * refreshing manager
	 */
	public void refresh();
	
	/**
	 * assigning manager listener for sending massages
	 * @param listener
	 */
	public void addListener(ManagerListener listener);
}
