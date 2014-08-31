package com.hanzeli.fragments;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.EventListener;
import com.hanzeli.resources.EventTypes;
import com.hanzeli.resources.ManagerEvent;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.resources.Transfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Button;

public class TransferFragment extends Fragment implements  OnClickListener, EventListener {
	
	private final String TAG = "TransferFragment";

    private Button stopButton;
	private Button clearButton;
    private Button runButton;
	private ListView transferListView;

    private TransferAdapter transferAdapter;
    private TransferManager transferManager;

	public void onAttach(Activity a){
        super.onAttach(a);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        transferManager = MainApplication.getInstance().getTransferManager();
        transferManager.attachFragment(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        transferAdapter.notifyDataSetChanged();
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
        return transferAdapter;
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }
	private void initTRFBrowser(View view){
		stopButton = (Button) view.findViewById(R.id.TRFButtonStop);
		clearButton = (Button) view.findViewById(R.id.TRFButtonClear);
        runButton = (Button) view.findViewById(R.id.TRFButtonRun);
		clearButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        runButton.setOnClickListener(this);
		transferListView = (ListView) view.findViewById(R.id.listViewTransfer);
		transferAdapter = new TransferAdapter(getActivity(), R.layout.list_view_transfer, this, transferManager.getTransfers());
		transferListView.setAdapter(transferAdapter);
        transferAdapter.setCheckBoxListener(this);
	}

	public void onClick(View view) {
        Transfer[] transfers;
		switch(view.getId()){
		case R.id.TRFButtonClear:
			Log.d(TAG,"Clear button pressed");
            transferManager.clearSelected();
			break;
		case R.id.TRFButtonStop:
			Log.d(TAG,"Stop button pressed");
            transfers = transferManager.getSelected();
            if (transfers.length > 1){
                onEvent(new ManagerEvent(EventTypes.SELECT_ONE));
                break;
            } else {
                transferManager.stopProcess();
                transfers[0].stopped = true;
                transferAdapter.setTransferList(transferManager.getTransfers());
            }
            break;
        case R.id.trf_checkbox:
            int i = (Integer) view.getTag();
            transferManager.selectTransfer(i);
            break;
        case R.id.TRFButtonRun:
            Log.d(TAG,"Stop button pressed");
            transfers = transferManager.getSelected();
            if (transfers.length > 1){
                onEvent(new ManagerEvent(EventTypes.SELECT_ONE));
                break;
            } else {
                transferManager.startOne(transfers[0]);
            }
		}


    }

	public void onEvent(ManagerEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
		switch(event.getEvent()){
			case TRANSFER_LIST_CHANGE:
				transferAdapter.setTransferList(transferManager.getTransfers());
				break;
            case START_TRANSFER:
                transferManager.processTransfers();
                break;
            case SELECT_ONE:
                builder.setMessage("Select only one item");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); 			           }
                });
                AlertDialog warning = builder.create();
                warning.show();
                break;
			default:
				break;
		}
		
	}



}
