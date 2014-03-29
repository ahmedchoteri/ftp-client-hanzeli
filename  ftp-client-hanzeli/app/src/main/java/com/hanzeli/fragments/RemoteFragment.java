package com.hanzeli.fragments;

import java.util.ArrayList;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;
import com.hanzeli.managers.FileInfo;
import com.hanzeli.values.Order;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


public class RemoteFragment extends ManagerFragment{

	
	public RemoteFragment(){
		super();
		fragmentId= R.layout.remote_manager_fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//get instance of local and transfer manager
		fileManager = MainApplication.getInstance().getRemoteManager();
		transfManager = MainApplication.getInstance().getTransferManager();
		//initialize the rest of fragment creation in abstract class
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(fragmentId, container, false);
		//initialize user interface
		initBrowser(view);
		fileManager.attach(this);
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
		//setup file list
		filesListView = (ListView) view.findViewById(R.id.listViewRemote);
		filesListView.setOnItemClickListener(this);
		//setup list adapter
		fileAdapter = new FileAdapter(getActivity(), this, fileManager.getFiles());
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
		downloadButton = (Button) view.findViewById(R.id.REMButtonDonload);
		downloadButton.setOnClickListener(this);
	}
	
	@Override
	protected void doTransfer(){
		ArrayList<FileInfo> transferFiles = fileManager.getSelectedFiles();
		for (FileInfo infoTransf : transferFiles){
			transfManager.addNewTransfer(infoTransf, 0); //0 is download
		}
		transfManager.processTransfers();
	}

}
