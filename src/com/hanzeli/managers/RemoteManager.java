package com.hanzeli.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Order;

import android.os.Bundle;


public class RemoteManager extends BasicFileManager{
	
	private FTPClient client = null;	//client for connection to server
	private String hostnameS;	//host name of server
	private int portS;			//port of server
	private boolean anonymousU;	//anonymous user
	private String nameU = null;		//user name	
	private String passwordU = null;	//user password
	
	
	public void init(Bundle bundle) {
		//ordering initialization
		if (bundle.containsKey("remote_order")) {
			orderingCompare = new Ordering((Order) bundle.get("remote.orderBy"),Order.ASC);
		}
		
		hostnameS = bundle.getString("server_host");
		portS=bundle.getInt("server_port");
		anonymousU = bundle.getBoolean("server_anonym");
		if (!anonymousU){
			nameU = bundle.getString("username");
			passwordU = bundle.getString("password");
		}
		currentDir = bundle.getString("remote_dir");
		rootDir="";
	}
	
	private void loadFilesInfo(){
		//remove old list
		filesInfo = null;
		//load new list of files
		try{
			
			FTPFile[] list = client.listFiles(currentDir, new FTPFileFilter() {
				public boolean accept(FTPFile file){
					return !file.getName().startsWith(".") && !file.isSymbolicLink() && (file.isDirectory() || file.isFile()); 
				}
			});
			if ((list != null) && (list.length > 0)) {
				filesInfo = new ArrayList<FileInfo>();
				for (FTPFile file : list) {
					FileInfo fi = new FileInfo(file.getName());
					fi.setSize(file.getSize());
					fi.setLastModif(file.getTimestamp().getTimeInMillis());
					if (currentDir.endsWith(File.separator)) fi.setAbsPath(currentDir + file.getName());
					else fi.setAbsPath(currentDir + File.separatorChar + file.getName());
					fi.setParentPath(currentDir);	
					fi.setType(FileTypes.getType(file));
					filesInfo.add(fi);
				}
				//sorting of filesInfo
				Collections.sort(filesInfo, orderingCompare);
			}
		}
		catch(IOException e){
			
		}
	}
	
	@Override
	protected void execConnect(ManagerTaskListener listener)
			throws ManagerException {
		client = new FTPClient();
		try{
			int reply;
			client.connect(hostnameS, portS);
			reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				throw new ManagerException(EventTypes.CONNECTION_ERROR);
			}
			if (!anonymousU){
				if(!client.login(nameU, passwordU)){
					client.disconnect();
					throw new ManagerException(EventTypes.CONNECTION_LOGIN_ERR);
				}
				
			}
			client.enterLocalPassiveMode();
			currentDir=rootDir=client.printWorkingDirectory();
			loadFilesInfo();
			
		
		} catch(IOException e){
			try {
				if (client != null) {
					client.disconnect();
				}
			} catch (IOException e1) {
			}
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
			
		}
		connection=true;
		
	}
	
	@Override
	protected void execDisconnect(ManagerTaskListener listener) throws ManagerException{
		try{
			client.disconnect();
		}
		catch(IOException e){
			e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
	}
	
	@Override
	protected void execChngWorkDir(ManagerTaskListener listener, FileInfo file)
			throws ManagerException {
		try{
			if(client.changeWorkingDirectory(file.getAbsPath())){
				currentDir=client.printWorkingDirectory();
				loadFilesInfo();
			}
			else{
				throw new ManagerException(EventTypes.CONNECTION_ERROR);
			}
		}catch(IOException  e){
			e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}

	@Override
	protected void execToParDir(ManagerTaskListener listener) throws ManagerException {
		try{
			if(client.changeToParentDirectory()){
				currentDir=client.printWorkingDirectory();
				loadFilesInfo();
			}
		}
		catch(IOException e){
			e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}
	
	@Override
	protected void execToHomeDir(ManagerTaskListener listener) throws ManagerException {
		try{
			if (client.changeWorkingDirectory(rootDir)){
				currentDir=client.printWorkingDirectory();
				loadFilesInfo();
			}
		}
		catch(IOException e){
			e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
	}
	
	@Override
	protected void execRefresh(ManagerTaskListener listener)
			throws ManagerException {
		loadFilesInfo();
		
	}

	@Override
	protected void execRename(ManagerTaskListener listener, FileInfo file,
			FileInfo newFile) throws ManagerException {
		try{
			if(client.rename(file.getName(), newFile.getName())){
				loadFilesInfo();
			}
			else throw new ManagerException(EventTypes.RENAME_ERR);
		}
		catch(IOException e){
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}

	@Override
	protected void execDelete(ManagerTaskListener listener, FileInfo[] files)
			throws ManagerException {
		boolean result = false;
		try{
			for (FileInfo file : files){
				if (file.isFolder()){
					result = client.removeDirectory(file.getAbsPath());
				}
				else result = client.deleteFile(file.getAbsPath());
				if (result) loadFilesInfo();
				else throw new ManagerException(EventTypes.DEL_FILE_ERR);
			}
		}
		catch(IOException e){
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}

	@Override
	protected void execNewFolder(ManagerTaskListener listener, FileInfo dir)
			throws ManagerException {
		try{
			if(!client.makeDirectory(dir.getName())){
				throw new ManagerException(EventTypes.NEW_FOLDER_ERR);   
			}
			else{
				loadFilesInfo();
			}
		}
		catch(IOException e){
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
	}
	
}
