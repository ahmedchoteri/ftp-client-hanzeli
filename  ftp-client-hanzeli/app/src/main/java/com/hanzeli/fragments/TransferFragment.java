package com.hanzeli.fragments;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerListener;
import com.hanzeli.transfer.TransferManager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Button;

public class TransferFragment extends Fragment implements  OnClickListener, ManagerListener {
	
	private final String TAG = "TransferFragment";

    protected Button stopButton;
	protected Button clearButton;
	protected ListView trfListView;
	protected TransferAdapter trfAdapter;
	protected TransferManager trfManager;


	public void onAttach(Activity a){
        super.onAttach(a);

    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(R.layout.transfer_fragment, container, false);
		//initialize user interface
		initTRFBrowser(view);
		
		trfManager = MainApplication.getInstance().getTransferManager();
		//trfManager.setFragList(this);
		trfManager.attachResultListener(this);

		//return created view
		return view;
	}
	
	private void initTRFBrowser(View view){
		stopButton = (Button) view.findViewById(R.id.TRFButtonStop);
		clearButton = (Button) view.findViewById(R.id.TRFButtonClear);
		clearButton.setOnClickListener(this);
		trfListView = (ListView) view.findViewById(R.id.listViewTransfer);
		trfAdapter = new TransferAdapter(getActivity(), R.layout.list_view_transfer, this, null);
		trfListView.setAdapter(trfAdapter);
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.TRFButtonClear:
			trfAdapter.clearSelected();
			break;
		case R.id.TRFButtonStop:
			//throw new UnsupportedOperationException();
		}
		
	}

	public void onProcessUpdate() {
		trfAdapter.update(trfManager.getTransfers());
	}

	public void managerEvent(ManagerEvent event) {
		switch(event.getEvent()){
			case TRANSFER_LIST_CHANGE:
				trfAdapter.setTransferList(trfManager.getTransfers());
				break;
			default:
				break;
		}
		
	}

}
