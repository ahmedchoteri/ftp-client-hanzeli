package com.hanzeli.karlftp;

import com.hanzeli.fragments.LocalFragment;
import com.hanzeli.fragments.RemoteFragment;
import com.hanzeli.fragments.TransferFragment;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerListener;
import com.hanzeli.values.EventTypes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.app.ActionBar;
//import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, ManagerListener{
	
	//private TabId tabSelected;
    private ViewPager viewPager;
    private TabPagerAdapter pagerAdaper;
	private BroadcastReceiver broadcastReceiver;

    private int[] tabs = {R.string.tab_local,R.string.tab_remote,R.string.tab_transfer};

	protected MenuItem settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//nastavenie layoutu
    	setContentView(R.layout.activity_main);

        //intializacia swipe pager
        viewPager = (ViewPager) findViewById(R.id.pager);
       	final ActionBar actionBar = getActionBar();
        pagerAdaper = new TabPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pagerAdaper);
        actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// vytvorenie tabov
        for (int tab: tabs){
            actionBar.addTab(actionBar.newTab().setText(getString(tab)).setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            public void onPageScrollStateChanged(int state) {

            }
        });

        /*
		for (TabId tabId : TabId.values()) {
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(getString(tabId.textId));
			tab.setTag(new TabTag(tabId));
			tab.setTabListener(this);   //this class responds to focus on tab
			actionBar.addTab(tab);
		}*/
        //selected tab after creation
        //tabSelected = TabId.LOCAL_MANAGER;
		// Set selected tab

		actionBar.setSelectedNavigationItem(0);
    	
    	Bundle intentExtras = getIntent().getExtras();
		// Create map properties
		final Bundle bundle = new Bundle();
		bundle.putString("server_host", intentExtras.getString("host"));
		bundle.putInt("server_port", intentExtras.getInt("port"));
		bundle.putBoolean("server_anonym", intentExtras.getBoolean("anonym"));
		bundle.putString("local_dir", intentExtras.getString("local"));
		bundle.putString("remote_dir", intentExtras.getString("remote"));
		bundle.putInt("server_timeout", 20000);
		if (!intentExtras.getBoolean("anonym")) {
			bundle.putString("username", intentExtras.getString("uname"));
			bundle.putString("password", intentExtras.getString("pass"));
		}

		
        //inicializacia manazerov a
		MainApplication.getInstance().initManagers(this, bundle);

		//connect both file managers
		MainApplication.getInstance().connect();

        /*broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ManagerEvent))
            }
        };*/

    }



	public void managerEvent(ManagerEvent type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        switch (type.getEvent()){
		case CONNECTION_ERROR:
            builder.setMessage("Client connection error!");
            break;
		case CONNECTION_LOGIN_ERR:
			builder.setMessage("Login error!");
			break;
        case CONNECTED:

            if(type.getManager().equals("TransferManager")) {
                builder.setTitle("Info");
                builder.setMessage("Transfer manager client connected.");
            }
            else if (type.getManager().equals("RemoteManager")){
                builder.setTitle("Info");
                builder.setMessage("Remote manager client connected.");
            }
            else return;
            break;
        case DISCONNECTION_ERROR:
            builder.setMessage("Logout error");
            break;
		default: return;
	    }
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); 			           }
        });
        AlertDialog warning = builder.create();
        warning.show();

	}


	
	@Override
	public void onStop(){
		MainApplication.getInstance().disconnect();
		super.onStop();
	}



    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }
	/*public void onTabSelected(Tab tab, FragmentTransaction ft) {
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
	}*/

	
	/*public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		TabTag tag = (TabTag) tab.getTag();
		Fragment fragment = getFragmentManager().findFragmentByTag(tag.key);
		if (fragment != null) {
			ft.hide(fragment);
		}
	}*/

	/**
	 * Tab tag
	 */
    /*
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
    */

	/*enum TabId {
		LOCAL_MANAGER(LocalFragment.class, R.string.tab_local),
		SERVER_MANAGER(RemoteFragment.class, R.string.tab_remote),
		TRANSFER_MANAGER(TransferFragment.class, R.string.tab_transfer);
		//fragment assigned to tab
		final Class<? extends Fragment> fragm;
		//id of tab
		final int textId;

		/**
		 * 
		 * @param fragm
		 * @param textId
		 */
		/*private TabId(Class<? extends Fragment> fragm, int textId) {
			this.fragm = fragm;
			this.textId = textId;
		}
	}
	*/
}
