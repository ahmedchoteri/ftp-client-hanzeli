package com.hanzeli.fragments;

import com.hanzeli.ftpdroid.MainApplication;
import com.hanzeli.ftpdroid.R;
import com.hanzeli.values.Order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class LocalFragment extends ManagerFragment{

	
	public LocalFragment(){
		super();
		fragmentId= R.layout.local_manager_fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//get instance of local manager
		fileManager = MainApplication.getInstance().getLocalManager();
		//initialize the rest of fragment creation in abstract class
		//inflation of browser to parent ViewGroup
		View view = inflater.inflate(fragmentId, container, false);
		//initialize user interface
		initBrowser(view);
		fileManager.addListener(this);
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
		//setup file list
		filesListView = (ListView) view.findViewById(R.id.listViewLocal);
		filesListView.setOnItemClickListener(this);
		//setup list adapter
		fileAdapter = new FileAdapter(getActivity(), this);
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
	}

}
