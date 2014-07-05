package com.hanzeli.karlftp;

import com.hanzeli.managers.EventListener;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.values.EventTypes;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
//import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, EventListener {

    private String TAG = "MainActivity";
	//private TabId tabSelected;
    private ViewPager viewPager;
    private TabPagerAdapter pagerAdaper;
	private BroadcastReceiver broadcastReceiver;

    private int[] tabs = {R.string.tab_local,R.string.tab_remote,R.string.tab_transfer};
    private Manager localManager;
    private Manager remoteManager;
    private TransferManager transferManager;
    private FileInfo[] copyFiles;
    private Menu activityMenu;

	protected MenuItem settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//nastavenie layoutu
    	setContentView(R.layout.activity_main);

        //intializacia swipe pager
        viewPager = (ViewPager) findViewById(R.id.pager);
       	final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        pagerAdaper = new TabPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdaper);
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
                switch (position){
                    case 0:
                        if(MainApplication.getInstance().copyLocal){
                            activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                        }
                        break;
                    case 1:
                        if(MainApplication.getInstance().copyRemote){
                            activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                        }
                    default:
                        activityMenu.findItem(R.id.menu_paste).setEnabled(false);
                }
            }

            public void onPageScrollStateChanged(int state) {

            }
        });

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
        localManager = MainApplication.getInstance().getLocalManager();
        remoteManager = MainApplication.getInstance().getRemoteManager();
        transferManager = MainApplication.getInstance().getTransferManager();
        /*broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ManagerEvent))
            }
        };*/

    }

    public void onResume(){
        super.onResume();
    }



	public void onEvent(ManagerEvent type) {
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
        case EMPTY_LIST:
            builder.setMessage("Nothing selected");
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
	public void onDestroy(){

		super.onDestroy();
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainApplication.getInstance().disconnect();
        MainApplication.getInstance().first = true;
        MainApplication.getInstance().copyRemote = false;
        MainApplication.getInstance().copyLocal = false;
        this.finish();
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }


    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_main,menu);
        activityMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int i = viewPager.getCurrentItem();
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this,ServerScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.menu_refresh:
                Log.d(TAG,"make refresh on tab number " + i);
                switch (i){
                    case 0:
                        localManager.refresh();
                        break;
                    case 1:
                        remoteManager.refresh();
                        break;
                    default:
                        break;
                }
                break;
            case R.id.menu_copy:
                Log.d(TAG,"starting COPY on tab number " + i);
                copyFiles = getCopyFiles(i);
                if(copyFiles==null || copyFiles.length==0){
                    onEvent(new ManagerEvent(EventTypes.EMPTY_LIST));
                } else {
                    String path = "";
                    boolean remote = false;
                    switch (i) {
                        case 0:
                            path = localManager.getCurrDir();
                            MainApplication.getInstance().copyLocal = true;
                            MainApplication.getInstance().copyRemote = false;
                            break;
                        case 1:
                            path = remoteManager.getCurrDir();
                            remote = true;
                            MainApplication.getInstance().copyLocal = false;
                            MainApplication.getInstance().copyRemote = true;
                            break;
                        default:
                            break;
                    }
                    transferManager.addFilesToCopy(copyFiles, false, path, remote);
                }
                if(activityMenu!=null) {
                    activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                }
                break;
            case R.id.menu_cut:
                Log.d(TAG,"starting CUT on tab number " + i);
                copyFiles = getCopyFiles(i);
                if(copyFiles==null || copyFiles.length==0){
                    onEvent(new ManagerEvent(EventTypes.EMPTY_LIST));
                } else {
                    String path = "";
                    boolean remote = false;
                    switch (i) {
                        case 0:
                            path = localManager.getCurrDir();
                            MainApplication.getInstance().copyLocal = true;
                            MainApplication.getInstance().copyRemote = false;
                            break;
                        case 1:
                            path = remoteManager.getCurrDir();
                            remote = true;
                            MainApplication.getInstance().copyLocal = false;
                            MainApplication.getInstance().copyRemote = true;
                            break;
                        default:
                            break;
                    }
                    transferManager.addFilesToCopy(copyFiles, true, path, remote);
                }
                if(activityMenu!=null) {
                    activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                }
                break;
            case R.id.menu_paste:
                Log.d(TAG,"starting PASTE on tab number " + i);
                String path = "";
                switch (i){
                    case 0:
                        path = localManager.getCurrDir();
                        break;
                    case 1:
                        path = remoteManager.getCurrDir();
                        break;
                }
                transferManager.addCopyTransfer(path);
                break;
            case R.id.menu_reconnect:
                break;
            case R.id.menu_select:
                break;
            case R.id.menu_synchronize:
                break;
            case R.id.menu_browsing:
                break;
            case R.id.menu_option:
                break;
            case R.id.menu_about:
                break;
        }
        return true;
    }

    private FileInfo[] getCopyFiles(int i){
        FileInfo[] files = null;
        switch (i){
            case 0:
                files = localManager.getSelectedFiles();
                break;
            case 1:
                files = remoteManager.getSelectedFiles();
                break;
            default:
                break;
        }
        return files;
    }
}
