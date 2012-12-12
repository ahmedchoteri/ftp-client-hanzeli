package com.hanzeli.ftpdroid;

import com.hanzeli.fragments.LocalFragment;
import com.hanzeli.fragments.RemoteFragment;
import com.hanzeli.fragments.TransferFragment;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerListener;

import android.os.Bundle;
import android.view.MenuItem;
import android.app.Activity;
import android.app.ActionBar;
//import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class MainActivity extends Activity implements ActionBar.TabListener, ManagerListener{
	
	private TabId tabSelected;
	
	protected MenuItem settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//use main view
    	setContentView(R.layout.activity_main);
    	//setup actionbar
    	ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		// Create local tab
		for (TabId tabId : TabId.values()) {
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(getString(tabId.textId));
			tab.setTag(new TabTag(tabId));
			tab.setTabListener(this);
			actionBar.addTab(tab);
		}
		// Set selected tab
		actionBar.setSelectedNavigationItem(tabSelected.ordinal());
    	
    	Bundle intentExtras = getIntent().getExtras();
		// Create map properties
		final Bundle bundle = new Bundle();
		bundle.putString("server_host", intentExtras.getString("host"));
		bundle.putInt("server_port", intentExtras.getInt("port"));
		bundle.putBoolean("server_anonym", intentExtras.getBoolean("anonym"));
		bundle.putString("local_dir", intentExtras.getString("local"));
		bundle.putString("remote_dir", intentExtras.getString("remote"));
		if (!intentExtras.getBoolean("anonym")) {
			bundle.putString("username", intentExtras.getString("uname"));
			bundle.putString("password", intentExtras.getString("pass"));
		}
		//selected tab after creation
		tabSelected = TabId.LOCAL_MANAGER;
		bundle.putInt("server_timeout", 20 * 1000);

		MainApplication.getInstance().initManagers(this, bundle);

		
		//connect both file managers
		MainApplication.getInstance().connect();
    }

	public void managerEvent(ManagerEvent type) {
		switch (type.getEvent()){
		case CONNECTION_ERROR:
		case CONNECTION_LOGIN_ERR:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Connection error!");
			builder.setTitle("Warning");
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.cancel(); 			           }
	       });
			AlertDialog warning = builder.create();
			warning.show();
			break;
		default: break;
	}
		
	}
	
	@Override
	public void onStop(){
		//MainApplication.getInstance().disconnect();
		super.onStop();
	}
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		TabTag tag = (TabTag) tab.getTag();
		Fragment fragment = getFragmentManager().findFragmentByTag(tag.key);
		if (fragment == null) {
			fragment = Fragment.instantiate(this, tag.className);
			ft.add(android.R.id.content, fragment, tag.key);
		} else {
			ft.show(fragment);
		}

		// Set selected tab
		tabSelected = tag.tabId;
	}

	
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		TabTag tag = (TabTag) tab.getTag();
		Fragment fragment = getFragmentManager().findFragmentByTag(tag.key);
		if (fragment != null) {
			ft.hide(fragment);
		}
	}

	
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	/**
	 * Tab tag
	 */
	private class TabTag {
		TabId tabId;
		String key;
		String className;

		TabTag(TabId tabId) {
			this.tabId = tabId;
			this.key = "tab_index_" + tabId.ordinal();
			this.className = tabId.fragm.getName();
		}
	}

	
	enum TabId {
		LOCAL_MANAGER(LocalFragment.class, R.string.tab_local),
		SERVER_MANAGER(RemoteFragment.class, R.string.tab_remote),
		TRANSFER_MANAGER(TransferFragment.class, R.string.tab_transfer);
		//fragment assigned to tab
		final Class<? extends Fragment> fragm;
		//id of tab
		final int textId;

		/**
		 * 
		 * @param fragment
		 * @param textId
		 */
		private TabId(Class<? extends Fragment> fragm, int textId) {
			this.fragm = fragm;
			this.textId = textId;
		}
	}
	
}
