package com.hanzeli.fragments;

//import com.hanzeli.ftpdroid.MainApplication;
import com.hanzeli.ftpdroid.R;	

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;

public class TransferFragment extends Fragment implements OnItemSelectedListener, OnClickListener, OnItemClickListener{
	
	protected Button stopButton;
	protected Button clearButton;
	protected ListView transferListView; 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(R.layout.transfer_fragment, container, false);
		//initialize user interface
		initTRFBrowser(view);
		
		//return created view
		return view;
	}
	
	private void initTRFBrowser(View view){
		stopButton = (Button) view.findViewById(R.id.TRFButtonStop);
		clearButton = (Button) view.findViewById(R.id.TRFButtonClear);
		transferListView = (ListView) view.findViewById(R.id.listViewTransfer);
	}
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
