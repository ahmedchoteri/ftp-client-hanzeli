package com.hanzeli.fragments;

import com.hanzeli.karlftp.R;

import com.hanzeli.managers.EventListener;
import com.hanzeli.resources.FileInfo;
import com.hanzeli.managers.Manager;
import com.hanzeli.resources.ManagerEvent;
import com.hanzeli.managers.TransferManager;
import com.hanzeli.resources.Order;


import android.app.AlertDialog;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class ManagerFragment extends Fragment implements OnClickListener, OnItemClickListener, EventListener {

    protected String TAG;
    /** manager & adapter */
	protected Manager fileManager;
	protected FileAdapter fileAdapter;
	protected TransferManager transferManager;
	
	/** id fragmentu */
	protected int fragmentId;

	protected boolean checkedAll;
	protected Order orderAscDesc;

	/** casti fragmentu */
	protected ImageButton goParentImgButton;
	protected ImageButton goHomeImgButton;
	protected TextView currentDirTextView;

    /** lava cast fragmentu */
    protected ListView filesListView;

    /** prava cast fragmentu */
    protected TextView detailNameTextView;
    protected TextView detailLocationTextView;
    protected TextView detailSizeTextView;
    protected TextView detailTimestampTextView;
    protected ImageView detailIconImageView;


	/** tlacidla */
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
	 * spracovnanie udalosti po interakcii s GUI komponentami
	 */
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.LOCimageButtonGoParent:
			case R.id.REMimageButtonGoParent:
				fileManager.toParrentDir();
				break;
			case R.id.LOCimageButtonGoHome:
			case R.id.REMimageButtonGoHome:
				fileManager.toHomeDir();
				break;
			case R.id.LOCButtonAll:
			case R.id.REMButtonAll:
				checkedAll = !checkedAll;
                fileManager.selectAllFiles(checkedAll);
				fileAdapter.update(fileManager.getFiles());
				break;
			case R.id.LOCButtonUpload:	
			case R.id.REMButtonDownload:
				doTransfer();
				break;
			case R.id.LOCButtonNew:
			case R.id.REMButtonNew:
				createNewFolder();
				break;
			case R.id.LOCButtonSort:
			case R.id.REMButtonSort:
				orderAscDesc = orderAscDesc == Order.ASC? Order.DESC : Order.ASC;
				changeOrder();
				break;
			case R.id.LOCButtonDelete:
			case R.id.REMButtonDelete:
				deleteFiles();
				break;
			case R.id.LOCButtonRename:
			case R.id.REMButtonRename:
				renameFile();
				break;
            case R.id.chk_file:
                int i = (Integer) view.getTag();
                fileManager.selectFile(i);
                break;
            case R.id.detail_image:
                int j = (Integer) view.getTag();
                onDetail(j);
                break;

		}
	}

	/**
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		FileInfo file = fileAdapter.getItem(position);
		if (file.isFolder()) {
			fileManager.changeWorkingDir(file.getName(),true);
		}

	}

    private void onDetail(int position){
        FileInfo file = fileAdapter.getItem(position);
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
            Log.d(TAG,"Detail image clicked, orientation is LANDSCAPE");
            detailNameTextView.setText(file.getName());
            detailLocationTextView.setText(file.getAbsPath());
            if (file.isFolder()){
                detailSizeTextView.setText("");
            }
            else{
                long fileSize = file.getSize();
                if (fileSize < 1000) {
                    detailSizeTextView.setText(fileSize + " b");
                } else {
                    fileSize /= 1000;
                    if (fileSize > 1000) {
                        detailSizeTextView.setText((fileSize / 1000) + " Mb");
                    } else {
                        detailSizeTextView.setText(fileSize + " Kb");
                    }
                }
            }
            detailTimestampTextView.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.US).format(file.getLastModif()));
            detailIconImageView.setImageDrawable(file.getType().getIcon());
        }
        else {
            Log.d(TAG,"Detail image clicked, orientation is PORTRAIT");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.detail_file));
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_detail, null);
            if (view != null) {
                TextView text = (TextView) view.findViewById(R.id.DLGDetail_name);
                text.setText(file.getName());
                text = (TextView) view.findViewById(R.id.DLGDetail_location);
                text.setText(file.getAbsPath());
                text = (TextView) view.findViewById(R.id.DLGDetail_size);
                if (file.isFolder()){
                    text.setText("");
                }
                else{
                    long fileSize = file.getSize();
                    if (fileSize < 1000) {
                        text.setText(fileSize + " b");
                    } else {
                        fileSize /= 1000;
                        if (fileSize > 1000) {
                            text.setText((fileSize / 1000) + " Mb");
                        } else {
                            text.setText(fileSize + " Kb");
                        }
                    }
                }
                text = (TextView) view.findViewById(R.id.DLGDetail_timestamp);
                text.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.US).format(file.getLastModif()));
                ImageView image = (ImageView) view.findViewById(R.id.DLGDetail_icon);
                image.setImageDrawable(file.getType().getIcon());
            }
            builder.setView(view)
                    // pridanie buttonov
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
    }
    /**
     * reakcia na event ktory vyvolal manager
     * @param event
     */
	public void onEvent(ManagerEvent event) {
		String sourceManager = event.getManager();
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
				fileAdapter.update(null);
				break;
			case FILES_LOADED:
				currentDirTextView.setText(fileManager.getCurrDir());
				fileAdapter.update(fileManager.getFiles());
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
				builder.setMessage("Folder delete error!");
				warning = builder.create();
				warning.show();
				break;
			case DEL_FILE_ERR:
				builder.setMessage("File delete error!");
				warning = builder.create();
				warning.show();
				break;				
			default:
				break;
		
		}
	}

	/**
	 * Vytvorenie noveho priecinku
	 *
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
				if (name != null && name.length() > 0) {
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
	 * Zmena poradia suborov z zozname
	 */
	protected void changeOrder() {
		fileManager.changeOrderingAscDesc(orderAscDesc);
	}

    /**
     * Metoda ktora je prepisana v Local a Remote managery
     */
	protected abstract void doTransfer();

    /**
	 * Zmazanie vyznacenych suborov
	 */
	protected void deleteFiles() {

        int count = fileManager.getSelectedFiles().length;
		//final ArrayList<FileInfo> selectedFiles = fileAdapter.getSelected();
		//int count = selectedFiles.size();
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
				fileManager.delFiles();
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
        //zobrat iba jedno
        FileInfo fi = fileManager.getSelectedFiles()[0];

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle("Rename file");
		final String fileName = fi.getName();
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
