package com.hanzeli.fragments;


import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.values.Order;

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

public class LocalFragment extends ManagerFragment{

	
	public LocalFragment(){
		super();
		fragmentId= R.layout.local_manager_fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TAG = "Local Fragment";
        Log.d(TAG, "Creating fragment");
		//get instance of local and transfer manager
		fileManager = MainApplication.getInstance().getLocalManager();
		transfManager = MainApplication.getInstance().getTransferManager();
		//initialize the rest of fragment creation in abstract class
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(fragmentId, container, false);
		//initialize user interface
		initBrowser(view);
		fileManager.attachFragment(this); //pripojenie manager listenera k managerovy
		checkedAll=false;
		orderAscDesc=Order.ASC;
		//return created view
		return view;
	}
	
	private void initBrowser(View view) {
		//setup go parent button
		goParentImgButton = (ImageButton) view.findViewById(R.id.LOCimageButtonGoParent);
		goParentImgButton.setOnClickListener(this);
		goParentImgButton.setEnabled(fileManager.existParent());
		//setup go home button
		goHomeImgButton = (ImageButton) view.findViewById(R.id.LOCimageButtonGoHome);
		goHomeImgButton.setOnClickListener(this);
		goHomeImgButton.setEnabled(true);
		//setup current working directory text
		currentDirTextView = (TextView) view.findViewById(R.id.LOCtextViewWorkinDirectory);
        if (fileManager.getCurrDir() != null) {
            currentDirTextView.setText(fileManager.getCurrDir());
        }
		//setup file list
		filesListView = (ListView) view.findViewById(R.id.listViewLocal);
		filesListView.setOnItemClickListener(this);
		//setup list adapter
		fileAdapter = new FileAdapter(getActivity(), "Local", fileManager.getFiles());
        fileAdapter.setCheckBoxListener(this);
		filesListView.setAdapter(fileAdapter);
		//setup fragment buttons + add onClickListener
		allButton = (Button)  view.findViewById(R.id.LOCButtonAll);
		allButton.setOnClickListener(this);
		newButton = (Button)  view.findViewById(R.id.LOCButtonNew);
		newButton.setOnClickListener(this);
		sortButton = (Button)  view.findViewById(R.id.LOCButtonSort);
		sortButton.setOnClickListener(this);
		deleteButton = (Button)  view.findViewById(R.id.LOCButtonDelete);
		deleteButton.setOnClickListener(this);
		renameButton = (Button)  view.findViewById(R.id.LOCButtonRename);
		renameButton.setOnClickListener(this);
		uploadButton = (Button) view.findViewById(R.id.LOCButtonUpload);
		uploadButton.setOnClickListener(this);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            detailIconImageView = (ImageView) view.findViewById(R.id.LOCDetail_icon);
            detailNameTextView = (TextView) view.findViewById(R.id.LOCDetail_name);
            detailSizeTextView = (TextView) view.findViewById(R.id.LOCDetail_size);
            detailTimestampTextView = (TextView) view.findViewById(R.id.LOCDetail_timestamp);
            detailLocationTextView = (TextView) view.findViewById(R.id.LOCDetail_location);
        }
	}
	
	@Override
	protected void doTransfer(){
		FileInfo[] transferFiles = fileManager.getSelectedFiles();
		transfManager.addNewTransfer(transferFiles,1); //1 is upload
        /*for (FileInfo infoTransf : transferFiles){
			transfManager.addNewTransfer(infoTransf, 1);
		}
		transfManager.processTransfers();*/
	}

}
