package com.hanzeli.karlftp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hanzeli.managers.EventListener;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.resources.EventTypes;
import com.hanzeli.resources.FileInfo;
import com.hanzeli.resources.ManagerEvent;
import com.hanzeli.resources.Order;
import com.hanzeli.resources.SyncSettings;

//import android.app.AlertDialog;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, EventListener {

    private String TAG = "MainActivity";
    private String PREF = "MyPreference";
	//private TabId tabSelected;
    private ViewPager viewPager;
    private TabPagerAdapter pagerAdaper;
	private BroadcastReceiver broadcastReceiver;
    private int tabNumber;

    private int[] tabs = {R.string.tab_local,R.string.tab_remote,R.string.tab_transfer};
    private Manager localManager;
    private Manager remoteManager;
    private TransferManager transferManager;
    private FileInfo[] selectedFiles;
    private Menu activityMenu;
    private FragmentManager fragmentManager;
    private FileInfo local_sync_fld;
    private FileInfo remote_sync_fld;
    private String patternText;
    private boolean syncBrowse;

    /**
     * Vytvorenie aktivity a nastavenie zobrazenia pre menu
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//nastavenie layoutu
    	setContentView(R.layout.activity_main);

        //intializacia swipe pager
        viewPager = (ViewPager) findViewById(R.id.pager);
       	final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        fragmentManager = getSupportFragmentManager();
        pagerAdaper = new TabPagerAdapter(fragmentManager);
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
                tabNumber = position;
                if(activityMenu != null){
                    adjustMenu();
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
        syncBrowse = MainApplication.getInstance().syncBrowse;

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
            break;
        case SELECT_ONE:
            builder.setMessage("Select only one item");
            break;
        case NOT_FOLDER:
            builder.setMessage("Selected item is not folder");
            break;
        case NOT_SYNC_REM:
            builder.setMessage("Select remote folder for synchronization");
            break;
        case NOT_SYNC_LOC:
            builder.setMessage("Select local folder for synchronization");
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
        adjustMenu();
        return true;
    }

    private void adjustMenu(){
        MenuItem itemPaste = activityMenu.findItem(R.id.menu_paste);
        MenuItem itemCopy = activityMenu.findItem(R.id.menu_copy);
        MenuItem itemCut = activityMenu.findItem(R.id.menu_cut);
        MenuItem itemSyncSel = activityMenu.findItem(R.id.menu_sync_select);
        MenuItem itemSync = activityMenu.findItem(R.id.menu_synchronize);
        MenuItem itemSelect = activityMenu.findItem(R.id.menu_select);
        MenuItem itemRefresh = activityMenu.findItem(R.id.menu_refresh);
        MenuItem itemBrowse = activityMenu.findItem(R.id.menu_browsing);
        MenuItem itemOrder = activityMenu.findItem(R.id.menu_ordering);
        switch (tabNumber) {
            case 0:
                itemPaste.setVisible(true);
                itemCopy.setVisible(true);
                itemCut.setVisible(true);
                itemSyncSel.setVisible(true);
                itemSync.setVisible(true);
                itemSelect.setVisible(true);
                itemRefresh.setVisible(true);
                itemBrowse.setVisible(true);
                itemBrowse.setChecked(syncBrowse);
                itemOrder.setVisible(true);
                if (MainApplication.getInstance().copyLocal) {
                    itemPaste.setEnabled(true);
                }
                break;
            case 1:
                itemPaste.setVisible(false);
                itemCopy.setVisible(false);
                itemCut.setVisible(false);
                itemSyncSel.setVisible(true);
                itemSync.setVisible(true);
                itemSelect.setVisible(true);
                itemRefresh.setVisible(true);
                itemBrowse.setVisible(true);
                itemBrowse.setChecked(syncBrowse);
                itemOrder.setVisible(true);
                break;
            default:
                itemPaste.setVisible(false);
                itemCopy.setVisible(false);
                itemCut.setVisible(false);
                itemSyncSel.setVisible(false);
                itemSync.setVisible(false);
                itemSelect.setVisible(false);
                itemRefresh.setVisible(false);
                itemBrowse.setVisible(false);
                itemOrder.setVisible(false);
                break;
        }
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
                selectedFiles = getSelectedFiles(i);
                if(selectedFiles ==null || selectedFiles.length==0){
                    onEvent(new ManagerEvent(EventTypes.EMPTY_LIST));
                }
                else {
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
                    transferManager.addFilesToCopy(selectedFiles, false, path, remote);
                }
                if(activityMenu!=null) {
                    activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                }
                Toast.makeText(getApplicationContext(), "Items selected for copy", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_cut:
                Log.d(TAG,"starting CUT on tab number " + i);
                selectedFiles = getSelectedFiles(i);
                if(selectedFiles ==null || selectedFiles.length==0){
                    onEvent(new ManagerEvent(EventTypes.EMPTY_LIST));
                }
                else {
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
                    transferManager.addFilesToCopy(selectedFiles, true, path, remote);
                }
                if(activityMenu!=null) {
                    activityMenu.findItem(R.id.menu_paste).setEnabled(true);
                }
                Toast.makeText(getApplicationContext(), "Items selected for cut", Toast.LENGTH_SHORT).show();
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
                MainApplication.getInstance().reconnect();
                break;
            case R.id.menu_select:
                Dialog patternDialog = createPatternDialog();
                patternDialog.show();
                break;
            case R.id.menu_sync_select:
                Log.d(TAG,"Folder selected for sync " + i);
                selectedFiles = getSelectedFiles(i);
                if(selectedFiles ==null || selectedFiles.length==0){
                    onEvent(new ManagerEvent(EventTypes.EMPTY_LIST));
                    break;
                }
                else if (selectedFiles.length > 1){
                    onEvent(new ManagerEvent(EventTypes.SELECT_ONE));
                    break;
                }
                else if (!selectedFiles[0].isFolder()){
                    onEvent(new ManagerEvent(EventTypes.NOT_FOLDER));
                    break;
                }
                else {
                    switch (i){
                        case 0:
                            local_sync_fld = selectedFiles[0];
                            break;
                        case 1:
                            remote_sync_fld = selectedFiles[0];
                            break;
                    }
                }
                Toast.makeText(getApplicationContext(), "Folder selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_synchronize:
                if(remote_sync_fld == null){
                    onEvent(new ManagerEvent(EventTypes.NOT_SYNC_REM));
                    break;
                } else if (local_sync_fld == null){
                    onEvent(new ManagerEvent(EventTypes.NOT_SYNC_LOC));
                    break;
                } else {
                    Log.d(TAG, "Starting synchronization dialog");
                    Dialog syncDialog = createSyncDialog();
                    syncDialog.show();
                }
                break;
            case R.id.menu_browsing:
                boolean check = item.isChecked();
                MainApplication.getInstance().syncBrowse = !check;
                syncBrowse = !check;
                item.setChecked(!check);
                break;
            case R.id.menu_ordering:
                Log.d(TAG,"Starting order dialog");
                Dialog orderDialog = createOrderDialog();
                orderDialog.show();
                break;
            case R.id.menu_log:
                Log.d(TAG,"Starting Log dialog");
                Dialog logDialog = createLogDialog();
                logDialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    private FileInfo[] getSelectedFiles(int i){
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

    public Dialog createSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Synchronize options");
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_sync, null);
        if (view!=null) {
            // pridanie textu s oznacenymi priecinkami pre lokal a remote
            TextView textL = (TextView) view.findViewById(R.id.SYNCTextFld1);
            textL.setText(local_sync_fld.getAbsPath());
            TextView textR = (TextView) view.findViewById(R.id.SYNCTextFld2);
            textR.setText(remote_sync_fld.getAbsPath());
            builder.setView(view)
                    // pridanie buttonov
                    .setPositiveButton(R.string.sync_start, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // prevzatie informacii o synchronizacii
                            SyncSettings ss = new SyncSettings();

                            // urcenie smeru synchronizacie
                            RadioGroup rGroup = (RadioGroup) view.findViewById(R.id.SYNCRadioGrpFolder);
                            int selectedID = rGroup.getCheckedRadioButtonId();
                            RadioButton selected = (RadioButton) view.findViewById(selectedID);
                            String selectedText = selected.getText().toString();
                            if (selectedText.equals(getString(R.string.sync_fld1))) {
                                // zdroj je lokal
                                ss.direction = SyncSettings.SyncDrc.LOCREM;
                            } else {
                                // zdroj je remote
                                ss.direction = SyncSettings.SyncDrc.REMLOC;
                            }
                            // urcenie typu synchronizacie
                            rGroup = (RadioGroup) view.findViewById(R.id.SYNCRadioGrpOption);
                            selectedID = rGroup.getCheckedRadioButtonId();
                            selected = (RadioButton) view.findViewById(selectedID);
                            selectedText = selected.getText().toString();
                            if (selectedText.equals(getString(R.string.sync_update))) {
                                ss.option = SyncSettings.SyncOpt.UPDATE;
                            } else if (selectedText.equals(getString(R.string.sync_overwrite))) {
                                ss.option = SyncSettings.SyncOpt.OVERWRITE;
                            } else {
                                ss.option = SyncSettings.SyncOpt.SYNCHRONIZE;
                            }
                            transferManager.addSyncTransfer(local_sync_fld, remote_sync_fld, ss);
                            Toast.makeText(getApplicationContext(), "Synchronization started", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(R.string.sync_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }
        return builder.create();
    }

    private Dialog createPatternDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter pattern");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                patternText = input.getText().toString();
                switch (tabNumber){
                    case 0:
                        localManager.patternSelect(patternText);
                        localManager.getFileAdapter().update(localManager.getFiles());
                        break;
                    case 1:
                        remoteManager.patternSelect(patternText);
                        remoteManager.getFileAdapter().update(remoteManager.getFiles());
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog createOrderDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle("Ordering type");
        final View view = inflater.inflate(R.layout.dialog_order, null);

        if (view != null){
            final RadioGroup rGroup = (RadioGroup) view.findViewById(R.id.ORDERRadioGrp);
            Order orderOld = MainApplication.getInstance().order;
            if(orderOld == Order.NAME){
                rGroup.check(R.id.ORDERRadioBtnName);
            }
            else if (orderOld ==  Order.TIME){
                rGroup.check(R.id.ORDERRadioBtnTime);
            } else rGroup.check(R.id.ORDERRadioBtnSize);
            builder.setView(view)
                    // pridanie buttonov
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Order o;
                            // urcenie smeru synchronizacie
                            int selectedID = rGroup.getCheckedRadioButtonId();
                            RadioButton selected = (RadioButton) view.findViewById(selectedID);
                            String selectedText = selected.getText().toString();
                            if (selectedText.equals(getString(R.string.order_name))) {
                                // zdroj je lokal
                                o = Order.NAME;
                            }
                            else if (selectedText.equals(getString(R.string.order_time))) {
                                o = Order.TIME;
                            } else {
                                o = Order.SIZE;
                            }
                            MainApplication.getInstance().order = o;
                            localManager.changeOrdering(o);
                            remoteManager.changeOrdering(o);
                        }
                    })
                    .setNegativeButton(R.string.sync_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }
        return builder.create();
    }

    private Dialog createLogDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_log, null);
        builder.setTitle("Connection log");
        if (view != null){
            TextView text = (TextView) view.findViewById(R.id.LOGTextView);
            String s = "";
            for (String line:MainApplication.getInstance().log){
                s += line;
            }
            text.setMovementMethod(new ScrollingMovementMethod());
            text.setText(s);
        }
        builder.setView(view)
                // pridanie buttonov
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
