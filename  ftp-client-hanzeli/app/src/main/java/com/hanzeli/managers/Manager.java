package com.hanzeli.managers;

import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPClient;

import com.hanzeli.karlftp.MainActivity;
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
	 * set client for manager
	 * @param client
	 */
	public void setClient(FTPClient client);
	
	public FTPClient getClient();
	/**
	 * list of all displayed files
	 * @return files list
	 */
	public ArrayList<FileInfo> getFiles();

    public FileInfo[] getSelectedFiles();
	
	/**
	 * deleting selected files from list
	 */
	public void delFiles();
	
	/**
	 * changing to parent directory 
	 */
	public void toParDir();

    /**
     * zmena oznacenia suboru podla stavu checkboxu
     * @param position
     */
	public void selectFile(int position);

    /**
     * zmena oznacenia pre vsetky prave nacitane subory
     */
    public void selectAllFiles(boolean checked);

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
	public void attachFragment(EventListener listener);

    /**
     * remove manager listener
     */
    public void detachFragment();

    public void attachActivity(MainActivity listener);

    public void detachActivity();

}
