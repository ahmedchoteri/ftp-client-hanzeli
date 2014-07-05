package com.hanzeli.managers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.hanzeli.fragments.TransferFragment;
import com.hanzeli.karlftp.MainActivity;
import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Values;

public class TransferManager {

	private final String TAG = "TransferManager";

    private TransferFragment fragment;    //listener ktory reaguje na vysledok
    private MainActivity activity;         //listener ktory reaguje na chybu
    public int transferNum;
    private ArrayList<Transfer> allTransfers;
    private FTPClient client = null;
    private boolean busy;
    private Bundle bundle;
    private FileInfo[] filesToCopy;
    private boolean cut;
    private boolean remote;
    private String copyFrom;
    private TransferService trfService;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter filter;



	public TransferManager(Bundle bundle){
		this.bundle = bundle;
        allTransfers = new ArrayList<Transfer>();
		transferNum=0;
        broadcastManager = MainApplication.broadcastManager;
        filter = new IntentFilter(Values.TRANSFER_PROGRESS);
        filter.addAction(Values.TRANSFER_WAITING);
        filter.addAction(Values.TRANSFER_DONE);
        broadcastManager.registerReceiver(broadcastReceiver,filter);

	}

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Broadcast received");
            String action = intent.getAction();
            int id = intent.getIntExtra(Values.TRANSFER_ID,0);
            Transfer t = findTransfer(allTransfers, id);
            if (t != null) {
                if (action != null) {
                    if (action.equals(Values.TRANSFER_WAITING)) {
                        t.setDone(true);    // transfer sa zacal vykonavat a je v stave working
                    } else if (action.equals(Values.TRANSFER_DONE)) {
                        t.setWaiting(false);    // transfer je dokonceny a je v stave done
                        setBusy(false);
                        processTransfers();
                    } else if (action.equals(Values.TRANSFER_PROGRESS)) {
                        t.setProgress(intent.getIntExtra(Values.TRANSFER_PROGRESS, 0));
                    }
                    if (fragment!=null) {
                        fragment.getAdapter().setTransferList(allTransfers);
                    }
                } else {
                    Log.d(TAG, "Received broadcast with null action");
                }
            }
            else {
                Log.d(TAG,"No transfer found during onReceive");
            }
        }

        private Transfer findTransfer(ArrayList<Transfer> list, int id){
            Transfer transfer = null;
            for (Transfer t : list){
                if (t.getId() == id) transfer = t;
            }
            return transfer;
        }
    };

	public void attachFragment(TransferFragment listener){
		fragment =listener;
	}

    public void detachFragment(){ fragment = null; }

    public void attachActivity(MainActivity listener){
		activity = listener;
	}

    public void detachActivity() { activity = null; }

    public FTPClient getClient(){
        if(!client.isConnected()){
            connect();
        }
        return client;
    }

	public void connect(){
        new JobHandler(ManagerTask.CONNECT_T).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void disconnect(){
        new JobHandler(ManagerTask.DISCONNECT).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setBusy(boolean b){
        busy = b;
    }

	public ArrayList<Transfer> getTransfers(){
		return allTransfers;
	}

    public Transfer getTransfer(int ID){
        if (allTransfers != null && !allTransfers.isEmpty()) {
            for (Transfer t : allTransfers) {
                if (t.getId() == ID) {
                    return t;
                }
            }
        }
        Log.d(TAG, "Transfer ID not found");
        return null;
    }

    public void addNewTransfer(FileInfo[] files, int direction){
        Log.d(TAG,"Adding new transfer");
        Transfer tr = new Transfer();
        tr.setId(transferNum);
        transferNum++;
        String pathLM = MainApplication.getInstance().getLocalManager().getCurrDir();
        String pathRM = MainApplication.getInstance().getRemoteManager().getCurrDir();
        tr.setDirection(direction);
        if (direction == 1){    //posielanie na server
            tr.setToPath(pathRM);
            tr.setFromPath(pathLM);
        }
        else {  // stahovanie zo serveru
            tr.setToPath(pathLM);
            tr.setFromPath(pathRM);
        }
        tr.setDone(false);
        tr.setWaiting(false);
        tr.transferFiles = files;
        // spocitanie velkosti suborov pre prenos
        Log.d(TAG,"Running counting files size operation");
        new JobHandler(ManagerTask.COUNT_FILES).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tr);
        allTransfers.add(tr);
        if (fragment != null) {
            fragment.onEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
    }

    /**
     * Pridanie suborov na kopirovanie
     * @param files subory ktore sa maju prekopirovat
     * @param cut priznak ci sa maju subory vystrihnut alebo nie
     * @param path cesta odkial sa bude kopirovat
     * @param remote priznak ci sa bude kopirovat na lokale alebo remote
     */
    public void addFilesToCopy(FileInfo[] files, boolean cut, String path, boolean remote){
        Log.d(TAG,"Getting file for copy/cut");
        filesToCopy=files;
        this.cut = cut;
        this.remote = remote;
        copyFrom=path;
    }

    public void addCopyTransfer(String path){
        Log.d(TAG,"Adding new transfer for copy/cut");
        Transfer tr = new Transfer();
        tr.setId(transferNum);
        transferNum++;
        tr.setFromPath(copyFrom);
        tr.setToPath(path);
        tr.cut = cut;
        int direction = (remote) ? 0 : 1;
        tr.setDirection(direction);
        tr.setDone(false);
        tr.setWaiting(false);
        tr.copyOp = true;
        tr.transferFiles = filesToCopy;
        // spocitanie velkosti suborov pre prenos
        Log.d(TAG,"Running counting files size operation");
        new JobHandler(ManagerTask.COUNT_FILES).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tr);
        allTransfers.add(tr);
        if (fragment != null) {
            fragment.onEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
    }

	/*public void addNewTransfer(FileInfo file, int direction) {
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
        if (fragment != null) {
            fragment.onEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
	}*/

	public void processTransfers() {

		if (allTransfers != null && !allTransfers.isEmpty() && !busy) {

            for (Transfer t: allTransfers) {
                if (t.isWaiting() && !t.isDone()) { //transfer je v stave waiting
                    Log.d(TAG,"Starting processTransfer");
                    //error listener je context cize mainactivity
                    Intent intent = new Intent(activity, TransferService.class);
                    //intent.putExtra(TransferService.TRANSFER, t); //pridanie transfera s ktorym sa ma spravit prenos
                    intent.putExtra(TransferService.TRANSFER, t.getId());
                    //zahajenie prenosu
                    activity.startService(intent); //spustenie service od MainActivity
                    activity.getApplicationContext().bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
                    busy = true;
                    break;
                }
            }
		}
		else{
            Log.d(TAG,"ProcessTransfer started with empty queue");
		}
	}

    public ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TransferService.TransferBinder b = (TransferService.TransferBinder) iBinder;
            trfService = b.getService();
            Log.d(TAG,"Service connected");
        }

        public void onServiceDisconnected(ComponentName componentName) {
            trfService = null;
        }
    };


    private class JobHandler extends AsyncTask<Transfer, Void, AsyncTaskResult>{

        private ManagerTask handlerTask;

        public JobHandler(ManagerTask task){
            handlerTask = task;
        }

        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected AsyncTaskResult doInBackground(Transfer... transfers) {
            AsyncTaskResult result = new AsyncTaskResult();
            try {
                switch (handlerTask.getTask()) {
                    case CONNECT_T:
                        execConnect();
                        break;
                    case DISCONNECT:
                        execDisconnect();
                        break;
                    case COUNT:
                        execCountFiles(transfers[0]);
                        transfers[0].setWaiting(true);
                        break;
                    default:
                        break;
                }

                result.setResult(true);
            }catch (ManagerException e){
                result.setResult(false);
                result.addmEvent(new ManagerEvent(e.getEvent()));
                busy=false;
            }
            return result;
        }

        protected void onPostExecute(AsyncTaskResult result){
            super.onPostExecute(result);
            for(ManagerEvent event: result.getmEvents()) {
                activity.onEvent(event);
            }
            if(result.getResult()) {
                for (ManagerEvent event : handlerTask.getEndEventsActivity()) {
                    event.setManager(TAG);
                    activity.onEvent(event);
                }

                for (ManagerEvent event : handlerTask.getEndEventsFragment()) {
                    if (event.getEvent()==EventTypes.START_TRANSFER) {
                        processTransfers();
                    }else{
                        if (fragment != null) {
                            fragment.onEvent(event);
                        }
                    }
                }
            }
        }
    }

    private void execConnect() throws ManagerException{
        Log.d(TAG,"Connecting client");
        client = new FTPClient();
        Utils.connectClient(client,bundle);
    }

    private void execDisconnect() throws ManagerException{
        Log.d(TAG,"Disconnecting client");
        Utils.disconnectClient(client);
    }

    private void execCountFiles(Transfer t) throws ManagerException{
        if (t.transferFiles != null && t.transferFiles.length > 0){
            if (t.getDirection() == 1){
                long count = countLocal(t.transferFiles);
                t.setSize(count);
            }
            else {
                try {
                    long count =countRemote(t.transferFiles);
                    t.setSize(count);
                } catch (IOException e){
                    Log.d(TAG,"Error during counting remote files size");
                    throw new ManagerException(EventTypes.CONNECTION_ERROR);
                }
            }
        }
        Log.d(TAG,"Transfer size is: " + t.getSize());
    }

    private long countLocal(FileInfo[] infos){
        long size = 0;
        for (FileInfo info : infos){
            //File file = new File(info.getAbsPath()+"/"+info.getName());
            if (info.isFolder()){
                File[] list = (new File(info.getAbsPath())).listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        return !file.isHidden() && (file.isDirectory() || file.isFile());
                    }
                });
                FileInfo[] files = new FileInfo[list.length];
                int i = 0;
                for (File f : list){
                    FileInfo fi = new FileInfo(f.getName());
                    fi.setAbsPath(f.getAbsolutePath());
                    fi.setSize(f.length());
                    fi.setType(FileTypes.getType(f));
                    files[i]=fi;
                    i++;
                }
                size += countLocal(files);
            }
            else{
                size += info.getSize();
            }

        }
        return size;
    }

    private long countRemote(FileInfo[] infos) throws IOException{
        long size = 0;
        for (FileInfo info : infos){
            //FTPFile file = new FTPFile(info.getAbsPath()+"/"+info.getName());
            if (info.isFolder()){
                FTPFile[] list = client.listFiles(info.getAbsPath(), new FTPFileFilter() {
                    public boolean accept(FTPFile file) {
                        return !file.getName().startsWith(".") && !file.isSymbolicLink() && (file.isDirectory() || file.isFile());
                    }
                });
                FileInfo[] files = new FileInfo[list.length];
                int i = 0;
                for (FTPFile f : list){
                    FileInfo fi = new FileInfo(f.getName());
                    fi.setAbsPath(info.getAbsPath() + File.separatorChar + f.getName());
                    fi.setSize(f.getSize());
                    fi.setType(FileTypes.getType(f));
                    files[i]=fi;
                    i++;
                }
                size += countRemote(files);
            }
            else{
                size += info.getSize();
            }

        }
        return size;
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
//			resultListener.onEvent(event);
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
//                resultListener.onEvent(event);
//            }else{
//                //nastala chyba a error listener musi reagovat
//                for (ManagerEvent event : result.getmEvents()) {
//                    errorListener.onEvent(event);
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
