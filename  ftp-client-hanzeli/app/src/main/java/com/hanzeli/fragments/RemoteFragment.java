package com.hanzeli.fragments;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.resources.FileInfo;
import com.hanzeli.resources.Order;


import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class RemoteFragment extends ManagerFragment{

	
	public RemoteFragment(){
		super();
		fragmentId= R.layout.remote_manager_fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TAG = "Remote Fragment";
        Log.d(TAG, "Creating fragment");
		//get instance of local and transfer manager
		fileManager = MainApplication.getInstance().getRemoteManager();
		transferManager = MainApplication.getInstance().getTransferManager();
		//initialize the rest of fragment creation in abstract class
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(fragmentId, container, false);
		//initialize user interface
		initBrowser(view);
		fileManager.attachFragment(this);
		checkedAll=false;
		orderAscDesc=Order.ASC;
		//return created view
		return view;
	}


	private void initBrowser(View view) {
		//setup go parent button
		goParentImgButton = (ImageButton) view.findViewById(R.id.REMimageButtonGoParent);
		goParentImgButton.setOnClickListener(this);
		goParentImgButton.setEnabled(fileManager.existParent());
		//setup go home button
		goHomeImgButton = (ImageButton) view.findViewById(R.id.REMimageButtonGoHome);
		goHomeImgButton.setOnClickListener(this);
		goHomeImgButton.setEnabled(true);
		//setup current working directory text
		currentDirTextView = (TextView) view.findViewById(R.id.REMtextViewWorkinDirectory);
        if (fileManager.getCurrDir() != null) {
            currentDirTextView.setText(fileManager.getCurrDir());
        }
		//setup file list
		filesListView = (ListView) view.findViewById(R.id.listViewRemote);
		filesListView.setOnItemClickListener(this);
		//setup list adapter
		fileAdapter = new FileAdapter(getActivity(), fileManager.getFiles());
        fileAdapter.setCheckBoxListener(this);
        fileManager.setFileAdapter(fileAdapter);
		filesListView.setAdapter(fileAdapter);
		//setup fragment buttons + add onClickListener
		allButton = (Button)  view.findViewById(R.id.REMButtonAll);
		allButton.setOnClickListener(this);
		newButton = (Button)  view.findViewById(R.id.REMButtonNew);
		newButton.setOnClickListener(this);
		sortButton = (Button)  view.findViewById(R.id.REMButtonSort);
		sortButton.setOnClickListener(this);
		deleteButton = (Button)  view.findViewById(R.id.REMButtonDelete);
		deleteButton.setOnClickListener(this);
		renameButton = (Button)  view.findViewById(R.id.REMButtonRename);
		renameButton.setOnClickListener(this);
		downloadButton = (Button) view.findViewById(R.id.REMButtonDownload);
		downloadButton.setOnClickListener(this);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            detailIconImageView = (ImageView) view.findViewById(R.id.REMDetail_icon);
            detailNameTextView = (TextView) view.findViewById(R.id.REMDetail_name);
            detailSizeTextView = (TextView) view.findViewById(R.id.REMDetail_size);
            detailTimestampTextView = (TextView) view.findViewById(R.id.REMDetail_timestamp);
            detailLocationTextView = (TextView) view.findViewById(R.id.REMDetail_location);
        }
	}
	
	@Override
	protected void doTransfer(){
		FileInfo[] transferFiles = fileManager.getSelectedFiles();
        transferManager.addNewTransfer(transferFiles,0); //0 is download
	}

}
