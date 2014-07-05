package com.hanzeli.fragments;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.EventListener;
import com.hanzeli.managers.ManagerEvent;
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

public class TransferFragment extends Fragment implements  OnClickListener, EventListener {
	
	private final String TAG = "TransferFragment";

    private Button stopButton;
	protected Button clearButton;
	protected ListView trfListView;

    private TransferAdapter trfAdapter;
    private TransferManager trfManager;






	public void onAttach(Activity a){
        super.onAttach(a);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);     //TODO problem ked nekliknem na tento tab tak nemam vytvorene komponenty
        trfManager = MainApplication.getInstance().getTransferManager();
        trfManager.attachFragment(this);


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

    public TransferAdapter getAdapter(){
        return trfAdapter;
    }

    @Override
    public void onDestroy(){
        //tu bol unregister broadcast listener
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



	public void onEvent(ManagerEvent event) {
		switch(event.getEvent()){
			case TRANSFER_LIST_CHANGE:
				trfAdapter.setTransferList(trfManager.getTransfers());
				break;
            case START_TRANSFER:
                trfManager.processTransfers();
                break;
			default:
				break;
		}
		
	}



}
