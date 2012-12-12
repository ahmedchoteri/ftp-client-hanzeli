package com.hanzeli.fragments;

import java.util.List;

import com.hanzeli.ftpdroid.R;

import com.hanzeli.managers.FileInfo;
import com.hanzeli.managers.Manager;
import com.hanzeli.managers.ManagerEvent;
import com.hanzeli.managers.ManagerListener;
import com.hanzeli.values.Order;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ManagerFragment extends Fragment implements OnClickListener, OnItemClickListener, ManagerListener{

	/** manager */
	protected Manager fileManager;
	/** id of this fragment */
	protected int fragmentId;
	
	protected boolean multSelect;
	protected boolean checkedAll;
	protected Order orderAscDesc;

	/** fragment parts */
	protected ImageButton goParentImgButton;
	protected ImageButton goHomeImgButton;
	protected TextView currentDirTextView;
	protected ListView filesListView;
	protected FileAdapter fileAdapter;

	/** buttons */
	protected Button uploadButton;
	protected Button downloadButton;
	protected Button allButton;
	protected Button newButton;
	protected Button sortButton;
	protected Button deleteButton;
	protected Button renameButton;

	/**
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}


	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View view) {
		
		
		switch (view.getId()) {

			case R.id.LOCimageButtonGoParent:
				fileManager.toParDir();
				break;
			case R.id.LOCimageButtonGoHome:
				fileManager.toHomeDir();
				break;
			case R.id.LOCButtonAll:
				checkedAll = checkedAll?  false :  true;
				fileAdapter.selectAll(checkedAll);	
				break;
			case R.id.LOCButtonUpload:
				break;
			case R.id.LOCButtonNew:
				createNewFolder();
				break;
			case R.id.LOCButtonSort:
				orderAscDesc = orderAscDesc == Order.ASC? Order.DESC : Order.ASC;
				changeOrder();
				break;
			case R.id.LOCButtonDelete:
				deleteFiles();
				break;
			case R.id.LOCButtonRename:
				renameFile();
				break;
			case R.id.REMimageButtonGoParent:
				fileManager.toParDir();
				break;
			case R.id.REMimageButtonGoHome:
				fileManager.toHomeDir();
			case R.id.REMButtonAll:
				checkedAll = checkedAll?  false :  true;
				fileAdapter.selectAll(checkedAll);
				break;
			case R.id.REMButtonSort:
				orderAscDesc = orderAscDesc == Order.ASC? Order.DESC : Order.ASC;
				changeOrder();
				break;
			case R.id.REMButtonDelete:
				deleteFiles();
				break;
			case R.id.REMButtonRename:
				renameFile();
				break;
			case R.id.REMButtonNew:
				createNewFolder();
				break;
			
		}
	}

	/**
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		FileInfo file = (FileInfo) fileAdapter.getItem(position);
		if (file.isFolder()) {
			fileManager.chngWorkDir(file.getName());
		}
	}

	
	public void managerEvent(ManagerEvent event) {
		Manager sourceManager = event.getManager();
		AlertDialog warning;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Warning");
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.cancel(); 			           }
	       });
		switch (event.getEvent()) {
			case FILES_LOAD:
				currentDirTextView.setText("Loading");
				fileAdapter.fillList(null);
				break;
			case FILES_LOADED:
				currentDirTextView.setText(sourceManager.getCurrDir());
				fileAdapter.fillList(sourceManager.getFiles());
				goParentImgButton.setEnabled(fileManager.existParent());
				break;
			case FILES_LIST_CHANGE:
				currentDirTextView.setText(sourceManager.getCurrDir());
				fileAdapter.fillList(sourceManager.getFiles());
				goParentImgButton.setEnabled(fileManager.existParent());
				break;
			case NEW_FOLDER_ERR:
				builder.setMessage("File or folder with this name already exist!");
				warning = builder.create();
				warning.show();
				break;
			case RENAME_ERR:
				builder.setMessage("File or folder with this name already exist!");
				warning = builder.create();
				warning.show();
				break;
			case DEL_FOLDER_ERR:
				builder.setMessage("File delete error!");
				warning = builder.create();
				warning.show();
				break;
			case DEL_FILE_ERR:
				builder.setMessage("Folder delete error!");
				warning = builder.create();
				warning.show();
				break;				
			default:
				break;
		
		}
	}

	/**
	 * 
	 * @param directory
	 *            directory path to create the new folder in.
	 */
	public void createNewFolder() {

		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		//title and message
		alert.setTitle("Create a new folder");
		//add an EditText view to AlertDialog
		final EditText inputText = new EditText(getActivity());
		alert.setView(inputText);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = inputText.getText().toString();
				if (name.length() > 0) {
					fileManager.newFolder(name);
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//nothing, operation canceled
			}
		});

		alert.show();
	}
	/**
	 * 
	 * @param orderBy
	 */
	protected void changeOrder() {
		fileManager.chngOrderingAscDesc(orderAscDesc);
	}

	/**
	 * 
	 */
	protected void deleteFiles() {

		final List<FileInfo> selectedFiles = fileAdapter.getSelected();
		int count = selectedFiles.size();
		//alert dialog with Yes/No answer
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Delete files");
		if (count == 1) {
			alert.setMessage("This item will be removed");
		} else {
			alert.setMessage(count + " items will be removed");
		}

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				fileManager.delFiles(selectedFiles);
			}
		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//nothing, operation canceled
			}
		});

		alert.show();
	}

	/**
	 * 
	 */
	protected void renameFile() {

		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Rename file");
		// Set an EditText view to get user input
		FileInfo fileInfo = fileAdapter.getFirstSelectedFile();
		final String fileName = fileInfo.getName();
		final int lastIndexOf = fileName.lastIndexOf(".");
		final String oldName = (lastIndexOf != -1) ? fileName.substring(0, lastIndexOf) : fileName;
		final String fileExt = (lastIndexOf != -1) ? fileName.substring(lastIndexOf) : "";
		final EditText input = new EditText(getActivity());
		input.setText(oldName);
		input.selectAll();
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String newName = input.getText().toString();
				if ((newName.length() > 0) && !newName.equals(oldName)) {
					fileManager.renameFile(fileName, newName + fileExt);
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//nothing, operation canceled
			}
		});

		alert.show();
	}

	

	
}
