package com.hanzeli.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Order;

import android.os.Bundle;
import android.util.Log;


public class RemoteManager extends BasicFileManager{
	
	private FTPClient client = null;	//client for connection to server
    private Bundle bundle;
	
	
	public void init(Bundle bundle) {
		//ordering initialization
		this.bundle = bundle;
        if (bundle.containsKey("remote_order")) {
			orderingCompare = new Ordering((Order) bundle.get("remote.orderBy"),Order.ASC);
		}
		currentDir = bundle.getString("remote_dir");
		rootDir="";
        TAG = "RemoteManager";
    }
	
	@Override
	public FTPClient getClient(){
		return client;
	}
	
	
	private void loadFilesInfo() throws IOException {
        //remove old list
        fileList = null;
        //load new list of files

        FTPFile[] list = client.listFiles(currentDir, new FTPFileFilter() {
            public boolean accept(FTPFile file) {
                return !file.getName().startsWith(".") && !file.isSymbolicLink() && (file.isDirectory() || file.isFile());
            }
        });
        if ((list != null) && (list.length > 0)) {
            fileList = new ArrayList<FileInfo>();
            for (FTPFile file : list) {
                FileInfo fi = new FileInfo(file.getName());
                fi.setSize(file.getSize());
                fi.setLastModif(file.getTimestamp().getTimeInMillis());
                if (currentDir.endsWith(File.separator)) fi.setAbsPath(currentDir + file.getName());
                else fi.setAbsPath(currentDir + File.separatorChar + file.getName());
                fi.setParentPath(currentDir);
                fi.setType(FileTypes.getType(file));
                fileList.add(fi);
            }
            //sorting of fileList
            Collections.sort(fileList, orderingCompare);
        }

    }
	
	@Override
	protected void execConnect() throws ManagerException {
		client = new FTPClient();
        Utils.connectClient(client,bundle);
        client.addProtocolCommandListener(new ProtocolCommandListener() {
            public void protocolCommandSent(ProtocolCommandEvent event) {
                Log.d("RM Command sent",event.getMessage());
            }

            public void protocolReplyReceived(ProtocolCommandEvent event) {
                Log.d("RM Command received",event.getMessage());
            }
        });
        try {
            currentDir = rootDir = client.printWorkingDirectory();
            loadFilesInfo();
            connection = true;
        } catch (IOException e){
            throw new ManagerException(EventTypes.CONNECTION_ERROR);
        }
	}
	
	@Override
	protected void execDisconnect() throws ManagerException{
		Log.d(TAG,"Disconnecting client");
        Utils.disconnectClient(client);
	}
	
	@Override
	protected void execChngWorkDir(FileInfo file)
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
			Log.d(TAG,"change working directory error");
            e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}

	@Override
	protected void execToParDir() throws ManagerException {
		try{
			if(client.changeToParentDirectory()){
				currentDir=client.printWorkingDirectory();
				loadFilesInfo();
			}
		}
		catch(IOException e){
			Log.d(TAG,"Parent directory change error");
            e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
		
	}
	
	@Override
	protected void execToHomeDir() throws ManagerException {
		try{
			if (client.changeWorkingDirectory(rootDir)){
				currentDir=client.printWorkingDirectory();
				loadFilesInfo();
			}
		}
		catch(IOException e){
            Log.d(TAG,"Home directory change error");
            e.printStackTrace();
			throw new ManagerException(EventTypes.CONNECTION_ERROR);
		}
	}
	
	@Override
	protected void execRefresh() throws ManagerException {
		try {
            loadFilesInfo();
        }
        catch(IOException e){
            Log.d(TAG,"Refresh error");
            throw new ManagerException(EventTypes.CONNECTION_ERROR);
        }
		
	}

	@Override
	protected void execRename(FileInfo file,FileInfo newFile) throws ManagerException {
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
	protected void execDelete(FileInfo[] files) throws ManagerException {
		boolean result;
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
	protected void execNewFolder(FileInfo dir) throws ManagerException {
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
