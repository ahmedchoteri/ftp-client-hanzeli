package com.hanzeli.fragments;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerListener;
import com.hanzeli.managers.Transfer;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.managers.TransferService;
import com.hanzeli.values.Values;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Button;

import java.util.ArrayList;

public class TransferFragment extends Fragment implements  OnClickListener, ManagerListener {
	
	private final String TAG = "TransferFragment";

    private Button stopButton;
	protected Button clearButton;
	protected ListView trfListView;

    private TransferAdapter trfAdapter;
    private TransferManager trfManager;
    private TransferService trfService;

    private IntentFilter filter;
    private LocalBroadcastManager broadcastManager;


	public void onAttach(Activity a){
        super.onAttach(a);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);     //TODO problem ked nekliknem na tento tab tak nemam vytvorene komponenty
        trfManager = MainApplication.getInstance().getTransferManager();
        trfManager.attachResultListener(this);
        broadcastManager = MainApplication.broadcastManager;
        filter = new IntentFilter(Values.TRANSFER_PROGRESS);
        filter.addAction(Values.TRANSFER_WAITING);
        filter.addAction(Values.TRANSFER_DONE);
        broadcastManager.registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public void onResume(){
        super.onResume();
        trfAdapter.notifyDataSetChanged();
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(R.layout.transfer_fragment, container, false);
		//initialize user interface
		initTRFBrowser(view);
		//return created view
		return view;
	}

    @Override
    public void onDestroy(){
        if (broadcastReceiver != null) {
            broadcastManager.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onDestroy();
    }
	private void initTRFBrowser(View view){
		stopButton = (Button) view.findViewById(R.id.TRFButtonStop);
		clearButton = (Button) view.findViewById(R.id.TRFButtonClear);
		clearButton.setOnClickListener(this);
		trfListView = (ListView) view.findViewById(R.id.listViewTransfer);
		trfAdapter = new TransferAdapter(getActivity(), R.layout.list_view_transfer, this, trfManager.getTransfers());
		trfListView.setAdapter(trfAdapter);
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.TRFButtonClear:
			Log.d(TAG,"Clear button pressed");
            trfAdapter.clearSelected();
			break;
		case R.id.TRFButtonStop:
			Log.d(TAG,"Stop button pressed");
            throw new UnsupportedOperationException();
		}
		
	}

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Broadcast received");
            String action = intent.getAction();
            ArrayList<Transfer> transferList = trfManager.getTransfers();
            int id = intent.getIntExtra(Values.TRANSFER_ID,0);
            Transfer t = findTransfer(transferList, id);
            if (t != null) {
                if (action != null) {
                    if (action.equals(Values.TRANSFER_WAITING)) {
                        t.setWaiting(false);
                    } else if (action.equals(Values.TRANSFER_DONE)) {
                        t.setDone(true);
                        trfManager.setBusy(false);
                        trfManager.processTransfers();
                    } else if (action.equals(Values.TRANSFER_PROGRESS)) {
                        t.setProgress(intent.getIntExtra(Values.TRANSFER_PROGRESS, 0));
                    }
                    trfAdapter.setTransferList(trfManager.getTransfers());
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

	public void managerEvent(ManagerEvent event) {
		switch(event.getEvent()){
			case TRANSFER_LIST_CHANGE:
				trfAdapter.setTransferList(trfManager.getTransfers());
				break;
			default:
				break;
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


}
