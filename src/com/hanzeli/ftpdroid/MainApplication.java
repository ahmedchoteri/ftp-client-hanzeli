package com.hanzeli.ftpdroid;

import com.hanzeli.managers.LocalManager;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.RemoteManager;
import com.hanzeli.values.Values;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


/**
 * implementation of Application class to maintain global state of managers
 * and preferences 
 */
public class MainApplication extends Application{
	
	/** instances of managers  */
	private Manager localMan;
	private Manager remoteMan;
	
	/** shared preferences for application options */
	private SharedPreferences preferences;
	
	/** instance of this class*/
	private static MainApplication thisApp;	//because I need to get instance of application and return statement don't allow this value
	/**
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// load preferences for application
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		thisApp=this;
	}
	
	/**
	 * 
	 * @return
	 */
	public static MainApplication getInstance() {
		return thisApp;
	}

	/**
	 * @return the localManager
	 */
	public Manager getLocalManager() {
		return localMan;
	}

	/**
	 * @return the remoteManager
	 */
	public Manager getRemoteManager() {
		return remoteMan;
	}
	
	/**
	 * initialization of managers
	 * @param context
	 * @param bundle
	 */
	public void initManagers(MainActivity context, Bundle bundle) {
		// Load preferences
		loadPreferences(bundle);
		// Local manager
		localMan = new LocalManager();
		localMan.addListener(context);
		localMan.init(bundle);

		// Server manager
		remoteMan = new RemoteManager();
		remoteMan.addListener(context);
		remoteMan.init(bundle);	
	}

	/**
	 * 
	 * @param bundle
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
	 * connection of managers
	 * 
	 */
	public void connect() {
		localMan.connect();
		remoteMan.connect();
	}
	
	/**
	 * disconnection of remote manager
	 */
	public void disconnect(){
		remoteMan.disconnect();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAllConnected() {
		return localMan.isConnected() ;
		//&& remoteMan.isConnected()
	}

	/**
	 * 
	 * @return
	 */
	public void saveLastSelectedServer(long lastServerID) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("lastServer", lastServerID);
		editor.commit();
	}

	/**
	 * 
	 * @return
	 */
	public long getLastSelectedServer() {
		return preferences.getLong("lastSelectedServer", -1);
	}
}
