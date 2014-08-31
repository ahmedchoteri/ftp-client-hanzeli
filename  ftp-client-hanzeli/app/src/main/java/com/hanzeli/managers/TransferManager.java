package com.hanzeli.managers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

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

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import com.hanzeli.fragments.TransferFragment;
import com.hanzeli.karlftp.MainActivity;
import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.resources.AsyncTaskResult;
import com.hanzeli.resources.EventTypes;
import com.hanzeli.resources.FileInfo;
import com.hanzeli.resources.FileTypes;
import com.hanzeli.resources.ManagerEvent;
import com.hanzeli.resources.ManagerException;
import com.hanzeli.resources.ManagerTask;
import com.hanzeli.resources.SyncSettings;
import com.hanzeli.resources.Transfer;
import com.hanzeli.resources.TransferType;
import com.hanzeli.resources.Values;

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
    private HashMap<String,SyncItem> map;
    private SyncSettings syncSettings;
    private Intent intentService;

	public TransferManager(Bundle bundle){
		this.bundle = bundle;
        allTransfers = new ArrayList<Transfer>();
		transferNum=0;
        broadcastManager = MainApplication.broadcastManager;
        filter = new IntentFilter(Values.TRANSFER_PROGRESS);
        filter.addAction(Values.TRANSFER_WAITING);
        filter.addAction(Values.TRANSFER_DONE);
        filter.addAction(Values.SERVICE_ERROR);
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
                    } else if (action.equals(Values.SERVICE_ERROR)){
                        setBusy(false);
                        t.fail = true;
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
            tr.type = TransferType.UPLOAD;
        }
        else {  // stahovanie zo serveru
            tr.setToPath(pathLM);
            tr.setFromPath(pathRM);
            tr.type = TransferType.DOWNLOAD;
        }
        tr.setDone(false);
        tr.setWaiting(false);
        tr.transferFiles = files;
        tr.setTmpSize(0);
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

    /**
     * Vytvorenie noveho transferu na prekopirovanie suboru po spusteni paste
     * @param path cesta kam sa maju subory nakopirovat
     */
    public void addCopyTransfer(String path){
        Log.d(TAG,"Adding new transfer for copy/cut");
        Transfer tr = new Transfer();
        tr.setId(transferNum);
        transferNum++;
        tr.setFromPath(copyFrom);
        tr.setToPath(path);
        tr.type = TransferType.COPY;
        tr.cut = cut;
        int direction = (remote) ? 0 : 1;
        tr.setDirection(direction);
        tr.setDone(false);
        tr.setWaiting(false);
        tr.transferFiles = filesToCopy;
        tr.setTmpSize(0);
        // spocitanie velkosti suborov pre prenos
        Log.d(TAG,"Running counting files size operation");
        new JobHandler(ManagerTask.COUNT_FILES).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tr);
        allTransfers.add(tr);
        if (fragment != null) {
            fragment.onEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
    }

    /**
     * Pridanie novej operacie pre synchronizaciu
     * @param local priecinok na lokale
     * @param remote priecinok na remote
     * @param settings nastavenia pre synchronizaciu
     */

	public void addSyncTransfer(FileInfo local,FileInfo remote, SyncSettings settings){
        Log.d(TAG,"Adding new transfer for copy/cut");
        Transfer tr = new Transfer();
        tr.setId(transferNum);
        transferNum++;
        tr.type = TransferType.SYNC;
        if(settings.direction == SyncSettings.SyncDrc.LOCREM){
            tr.setFromPath(local.getAbsPath());
            tr.setToPath(remote.getAbsPath());
            tr.setDirection(0);
        } else {
            tr.setFromPath(remote.getAbsPath());
            tr.setToPath(local.getAbsPath());
            tr.setDirection(1);
        }
        tr.setDone(false);
        tr.setWaiting(false);
        syncSettings = settings;
        tr.setTmpSize(0);
        Log.d(TAG,"Running counting files size operation");
        new JobHandler(ManagerTask.COUNT_FILES).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tr);
        allTransfers.add(tr);
        if (fragment != null) {
            fragment.onEvent(new ManagerEvent(EventTypes.TRANSFER_LIST_CHANGE));
        }
    }

    /**
     * Spustenie procesu ktory caka na vykonanie
     */
	public void processTransfers() {

		if (allTransfers != null && !allTransfers.isEmpty() && !busy) {

            for (Transfer t: allTransfers) {
                if ((t.isWaiting() && !t.isDone()) || t.stopped) { //transfer je v stave waiting
                    Log.d(TAG,"Starting processTransfer");
                    t.stopped = false;
                    //error listener je context cize mainactivity
                    intentService = new Intent(activity, TransferService.class);
                    intentService.putExtra(TransferService.TRANSFER, t.getId());
                    //zahajenie prenosu
                    activity.startService(intentService); //spustenie service od MainActivity
                    activity.bindService(intentService, this.connection, Context.BIND_AUTO_CREATE);
                    busy = true;
                    break;
                }
            }
		}
		else{
            Log.d(TAG,"ProcessTransfer started with empty queue");
		}
	}

    public void startOne(Transfer tr){
        if (allTransfers != null && !allTransfers.isEmpty() && !busy) {
            for (Transfer t: allTransfers) {
                if (((t.isWaiting() && !t.isDone()) || t.stopped) && t==tr) {
                    Log.d(TAG,"Starting processTransfer");
                    t.stopped = false;
                    intentService = new Intent(activity, TransferService.class);
                    intentService.putExtra(TransferService.TRANSFER, t.getId());
                    //zahajenie prenosu
                    activity.startService(intentService); //spustenie service od MainActivity
                    activity.bindService(intentService, connection, Context.BIND_AUTO_CREATE);
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

    public void selectTransfer(int position){
        if(allTransfers!=null) {
            boolean b = allTransfers.get(position).isChecked();
            allTransfers.get(position).setChecked(!b);
        }
    }

    public Transfer[] getSelected(){
        ArrayList<Transfer> trOut = new ArrayList<Transfer>();
        if (allTransfers!=null) {
            for (Transfer t : allTransfers) {
                if (t.isChecked()) {
                    trOut.add(t);
                }
            }
        }
        Transfer[] returnFiles = new Transfer[trOut.size()];
        trOut.toArray(returnFiles);
        return returnFiles;
    }

    public void clearSelected(){
        ArrayList<Transfer> toDelete = new ArrayList<Transfer>();
        if(allTransfers!=null){
            for(Transfer t : allTransfers){
                if (t.isChecked() && (t.isWaiting() || t.isDone() || t.fail || t.stopped)){
                    toDelete.add(t);
                }
            }
            for (Transfer t: toDelete){
                allTransfers.remove(t);
            }
            toDelete.clear();
        }
        if (fragment!=null) {
            fragment.getAdapter().setTransferList(allTransfers);
        }
    }

    public void stopProcess(){
        Log.d(TAG,"Stopping service");
        trfService.stop();
    }
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
                        Transfer t = transfers[0];
                        if (t.type == TransferType.SYNC){
                            execAnalyze(t);
                        }else {
                            execCountFiles(t);
                        }
                        t.setWaiting(true);
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

        if (client!=null && client.isConnected()){
            Log.d(TAG,"Reconnecting client");
            Utils.disconnectClient(client);
            Utils.connectClient(client,bundle);
        } else {
            Log.d(TAG,"Connecting client");
            client = new FTPClient();
            Utils.connectClient(client, bundle);
        }
        client.addProtocolCommandListener(new ProtocolCommandListener() {
            public void protocolCommandSent(ProtocolCommandEvent event) {
                Log.d("TM Command sent: ",event.getMessage());
                MainApplication.getInstance().addToLog("TM Command sent: " + event.getMessage() + '\n');
            }

            public void protocolReplyReceived(ProtocolCommandEvent event) {
                Log.d("TM Command received",event.getMessage());
                MainApplication.getInstance().addToLog("TM Command received: " + event.getMessage() + '\n');
            }
        });
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

    public enum Status{
        COPY,
        ADD,
        DELETE
    }

    public class SyncItem implements Comparable<SyncItem>{
        Status status;
        String key;
        boolean sourceExists;
        boolean targetExists;
        String sourceRelativePath;
        String targetRelativePath;
        String sourceName;
        String targetName;
        boolean sourceIsDirectory;
        boolean targetIsDirectory;
        int layer;
        long sourceFileSize;
        long targetFileSize;
        long sourceLastModifiedTime;       // in milliseconds
        long targetLastModifiedTime;       // in milliseconds

        public SyncItem(String key){
            this.key=key;
            sourceExists = false;
            targetExists = false;
        }
        public int compareTo (SyncItem item) {
            //spocitam si pocet / a podla toho porovnam ktora cesta je kratsia
            int numKey = Utils.countOccurrences(key,File.separatorChar);
            int numItemKey = Utils.countOccurrences(item.key,File.separatorChar);
            return numKey - numItemKey; }
        public String getSourcePath(String sourceBaseDir) {
            if(sourceRelativePath.equals("")){
                return sourceBaseDir;
            } else {
                return sourceBaseDir + sourceRelativePath;
            }
        }
        public String getOldTargetPath(String targetBaseDir) {
            if(targetRelativePath.equals("")){
                return targetBaseDir;
            }else {
                return targetBaseDir + targetRelativePath;
            }
        }
        public String getNewTargetPath(String targetBaseDir) {
            if(sourceRelativePath.equals("")){
                return targetBaseDir;
            } else {
                return targetBaseDir + sourceRelativePath;
            }
        }
    }

    private void execAnalyze(Transfer t) throws ManagerException{
        t.syncItems = analyzeFolders(t.getFromPath(), t.getToPath(), syncSettings);
        t.setSize(t.syncItems.length);
        Log.d(TAG,"Analyze is done.");
    }

    private SyncItem[] analyzeFolders(String localPath, String remotePath, SyncSettings settings) throws ManagerException{
        map = new HashMap<String, SyncItem>();
        if(settings.direction == SyncSettings.SyncDrc.LOCREM){
            readLocal(localPath,"",false,0);
            try {
                readRemote(remotePath,"",true,0);
            } catch (IOException e){
                throw new ManagerException(EventTypes.SYNC_ERR);
            }
        }
        else {
            readLocal(localPath,"",true,0);
            try {
                readRemote(remotePath,"",false,0);
            } catch (IOException e){
                throw new ManagerException(EventTypes.SYNC_ERR);
            }
        }
        SyncItem[] result = analyzeMap(settings.option);
        return result;
    }

    private void readLocal(String basePath, String relativePath, boolean target, int layer){
        File file;
        if(relativePath.equals("")){
            file = new File(basePath);
        } else {
            file = new File(basePath + File.separatorChar + relativePath);
        }
        if (file.isDirectory()){
            File[] filesList = file.listFiles();
            for(File f:filesList){
                if(target && map.containsKey(relativePath + File.separatorChar + f.getName())){
                    // subor sa na source vyskytuje tak si updatujem jeho target premenne
                    SyncItem item = map.get(relativePath + File.separatorChar + f.getName());
                    item.targetExists = true;
                    item.targetName = f.getName();
                    item.targetFileSize = f.length();
                    item.targetRelativePath = relativePath;
                    item.targetIsDirectory = f.isDirectory();
                    item.targetLastModifiedTime = f.lastModified();
                }
                else if (target){
                    //na sourci sa subor nenachadza tak sa vytvori nova polozka s target premennymi
                    SyncItem item = new SyncItem(relativePath + File.separatorChar + f.getName());
                    item.targetExists = true;
                    item.targetName = f.getName();
                    item.targetFileSize = f.length();
                    item.targetRelativePath = relativePath;
                    item.targetIsDirectory = f.isDirectory();
                    item.targetLastModifiedTime = f.lastModified();
                    item.layer = layer;
                    map.put(relativePath + File.separatorChar + f.getName(),item);
                }
                else {
                    // subor je pridavany zo source umiestnenia takze sa vytvori ako nova polozka
                    SyncItem item = new SyncItem(relativePath + File.separatorChar + f.getName());
                    item.sourceExists = true;
                    item.sourceName = f.getName();
                    item.sourceFileSize = f.length();
                    item.sourceRelativePath = relativePath;
                    item.sourceIsDirectory = f.isDirectory();
                    item.sourceLastModifiedTime = f.lastModified();
                    item.layer = layer;
                    map.put(relativePath + File.separatorChar + f.getName(),item);
                }
                if(f.isDirectory()){
                    readLocal(basePath, relativePath + File.separatorChar + f.getName(), target, layer++);
                }
            }
        }
    }

    private void readRemote(String basePath, String relativePath, boolean target, int layer) throws IOException{
        FTPFile file;
        String path;
        if(relativePath.equals("")){
            path = basePath;
        } else {
            path = basePath + File.separatorChar + relativePath;
        }
        file = client.mlistFile(path);
        if (file.isDirectory()){
            FTPFile[] filesList = client.listFiles(path, new FTPFileFilter() {
                public boolean accept(FTPFile file) {
                    return !file.getName().startsWith(".") && !file.isSymbolicLink() && (file.isDirectory() || file.isFile());
                }
            });
            for(FTPFile f:filesList){
                // subor sa na source vyskytuje tak si updatujem jeho target premenne
                if(target && map.containsKey(relativePath + File.separatorChar + f.getName())){
                    SyncItem item = map.get(relativePath + File.separatorChar + f.getName());
                    item.targetExists = true;
                    item.targetName = f.getName();
                    item.targetFileSize = f.getSize();
                    item.targetRelativePath = relativePath;
                    item.targetIsDirectory = f.isDirectory();
                    item.targetLastModifiedTime = f.getTimestamp().getTimeInMillis();
                }
                else if (target){
                    //na sourci sa subor nenachadza tak sa vytvori nova polozka s target premennymi
                    SyncItem item = new SyncItem(relativePath + File.separatorChar + f.getName());
                    item.targetExists = true;
                    item.targetName = f.getName();
                    item.targetFileSize = f.getSize();
                    item.targetRelativePath = relativePath;
                    item.targetIsDirectory = f.isDirectory();
                    item.targetLastModifiedTime = f.getTimestamp().getTimeInMillis();
                    item.layer = layer;
                    map.put(relativePath + File.separatorChar + f.getName(),item);
                }
                else {
                    // subor je pridavany zo source umiestnenia takze sa vytvori ako nova polozka
                    SyncItem item = new SyncItem(relativePath + File.separatorChar + f.getName());
                    item.sourceExists = true;
                    item.sourceName = f.getName();
                    item.sourceFileSize = f.getSize();
                    item.sourceRelativePath = relativePath;
                    item.sourceIsDirectory = f.isDirectory();
                    item.sourceLastModifiedTime = f.getTimestamp().getTimeInMillis();
                    item.layer = layer;
                    map.put(relativePath + File.separatorChar + f.getName(),item);
                }
                if(f.isDirectory()){
                    readRemote(basePath, relativePath + File.separatorChar + f.getName(), target, layer++);
                }
            }
        }
    }

    private SyncItem[] analyzeMap(SyncSettings.SyncOpt syncOpt){

        ArrayList<SyncItem> listCopy = new ArrayList<SyncItem>();
        ArrayList<SyncItem> listDelete = new ArrayList<SyncItem>();
        ArrayList<SyncItem> listMap = new ArrayList<SyncItem>(map.values());
        for (SyncItem item: listMap){
            switch (syncOpt){
                case UPDATE:
                    if(item.sourceExists && item.targetExists &&
                            item.sourceLastModifiedTime > item.targetLastModifiedTime &&
                            !item.sourceIsDirectory && !item.targetIsDirectory){
                        item.status=Status.COPY;
                        listCopy.add(item);
                    }
                    break;
                case OVERWRITE:
                    if(item.sourceExists && item.targetExists && !item.sourceIsDirectory && !item.targetIsDirectory){
                        item.status=Status.COPY;
                        listCopy.add(item);
                    }
                    break;
                case SYNCHRONIZE:
                    if(item.sourceExists && !item.targetExists){
                        item.status=Status.ADD;
                        listCopy.add(item);
                    }
                    else if(!item.sourceExists && item.targetExists){
                        item.status=Status.DELETE;
                        listDelete.add(item);
                    }
                    else if(item.sourceLastModifiedTime > item.targetLastModifiedTime){
                        item.status=Status.COPY;
                        listCopy.add(item);
                    }
                    break;
            }
        }
        SyncItem [] listC = listCopy.toArray(new SyncItem[listCopy.size()]);
        SyncItem [] listD = listDelete.toArray(new SyncItem[listDelete.size()]);
        Arrays.sort(listC);
        Arrays.sort(listD, Collections.reverseOrder());
        SyncItem[] listSI = Utils.concat(listC,listD);
        return listSI;
    }
}
