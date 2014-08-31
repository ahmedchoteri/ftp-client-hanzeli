package com.hanzeli.karlftp;

import com.hanzeli.managers.LocalManager;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.RemoteManager;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.resources.Order;
import com.hanzeli.resources.Values;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;


/**
 * implementation of Application class to maintain global state of managers
 * and preferences 
 */
public class MainApplication extends Application{
	
	/** instances of managers  */
	private Manager localManager;
	private Manager remoteManager;
	private TransferManager transferManager;
	
	/** shared preferences for application options */
	public SharedPreferences preferences;
	
	/** instance of this class*/
	private static MainApplication thisApp;	//because I need to get instance of application and return statement don't allow this value

    public static LocalBroadcastManager broadcastManager;

    public boolean first;
    public boolean copyLocal;
    public boolean copyRemote;
    public boolean syncBrowse;
    public Order order;
    public ArrayList<String> log;


    /**
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// load preferences for application
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		thisApp=this;
        broadcastManager = LocalBroadcastManager.getInstance(this);
        first = true;
        copyLocal = false;
        copyRemote = false;
        log = new ArrayList<String>();
        order = Order.NAME;
	}
	
	/**
	 * Metoda pre ziskanie instancie Main Application
	 * @return MainApplication
	 */
	public static MainApplication getInstance() {
		return thisApp;
	}

	/**
     * Metoda pre ziskanie instancie Local Manager
	 * @return LocalManager
	 */
	public Manager getLocalManager() {
		return localManager;
	}

	/**
     * Metoda pre ziskanie instancie Remote Manager
	 * @return RemoteManager
	 */
	public Manager getRemoteManager() {
		return remoteManager;
	}

    /**
     * Metoda pre ziskanie instancie Transfer Manager
     * @return TransferManager
     */
	public TransferManager getTransferManager(){
		return transferManager;
	}
	/**
	 * Inicializacia managerov
	 * @param context main activity
	 * @param bundle information about server
	 */
	public void initManagers(MainActivity context, Bundle bundle) {
		if(first) {
            // Load preferences
            loadPreferences(bundle);
            // Local manager
            localManager = new LocalManager();
            localManager.init(bundle);

            // Server manager
            remoteManager = new RemoteManager();
            remoteManager.init(bundle);

            // Transfer manager
            transferManager = new TransferManager(bundle);

        }
        localManager.attachActivity(context);
        remoteManager.attachActivity(context);
        transferManager.attachActivity(context);
	}

	/**
	 * Nacitanie preferencii podla Bundle alebo cez Defaultne hodnoty
	 * @param bundle preference bundle
	 */
	private void loadPreferences(Bundle bundle) {

		// Timeout
		String timeout = preferences.getString("timeout", null);
		if (timeout != null) {
			bundle.putInt(Values.PREF_DEF_TIMEOUT, Integer.parseInt(timeout));
		} else {
			bundle.putInt(Values.PREF_DEF_TIMEOUT, Values.DEF_TIMEOUT);
		}

		// maximum of retries to connect
		String maxRetries = preferences.getString("max_retries", null);
		if (maxRetries != null) {
			bundle.putInt(Values.PREF_DEF_CONNECTION_TRIES, Integer.parseInt(maxRetries));
		} else {
			bundle.putInt(Values.PREF_DEF_CONNECTION_TRIES, Values.DEF_CONNECTION_TRIES);
		}

		// value for time between tries to login
		String delay = preferences.getString("delay", null);
		if (delay != null) {
			bundle.putInt(Values.PREF_DEF_TIME_LOGIN_FAILED, Integer.parseInt(delay));
		} else {
			bundle.putInt(Values.PREF_DEF_TIME_LOGIN_FAILED, Values.DEF_TIME_LOGIN_FAILED);
		}

		// Transfer mode
		String transferMode = preferences.getString("transfer_mode", null);
		if (transferMode != null) {
			bundle.putString(Values.PREF_DEF_TRANSFER_MODE, transferMode);
		} else {
			bundle.putString(Values.PREF_DEF_TRANSFER_MODE, Values.DEF_TRANSFER_MODE);
		}
	}

	/**
	 * Pripojenie vsetkych managerov
	 * 
	 */
	public void connect() {
		if(first) {
            localManager.connect();
            remoteManager.connect();
            transferManager.connect();
            first = false;
        }
	}



    public void reconnect(){
        remoteManager.connect();
        transferManager.connect();
    }
	
	/**
	 * Odpojenie managerov
	 */
	public void disconnect(){
		remoteManager.disconnect();
        transferManager.disconnect();
	}

	/**
	 * Do zdielanych preferencii prida aktualny server
	 * @param lastServerID ID aktualneho serveru ktory uzivatel pouziva
	 */
	public void saveLastSelectedServer(long lastServerID) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("LAST_SERVER", lastServerID);
		editor.commit();
	}

    /**
     * adding to my log
     * @param msg message
     */
    public void addToLog(String msg){
        if (log.size()>25){
            log.remove(0);
        }
        log.add(msg);
    }

}
