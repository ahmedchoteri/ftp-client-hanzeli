package com.hanzeli.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.hanzeli.ftpdroid.R;
import com.hanzeli.managers.FileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Class for passing data to ListView
 * @author Michal
 *
 */
public class FileAdapter extends BaseAdapter implements ListAdapter, OnClickListener{
	
	private List<FileInfo> files;
	protected LayoutInflater inflater;	
	private OnClickListener checkBoxListener;
	
	protected class FileView{
		TextView name;
		TextView size;
		TextView lastModif;
		CheckBox checkbox;
		ImageView icon;
		
	}
	
	public FileAdapter(Context context, OnClickListener listener){
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		checkBoxListener = listener;
	}
	
	public int getCount() {
		if (files != null && !files.isEmpty()) return files.size();
		return 0;
	}

	public Object getItem(int index) {
		if (files != null && !files.isEmpty()) return files.get(index);
		return null;
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int position, View view, ViewGroup parent) {
		FileView fv;
		if (view == null) {
			//construction of new view component
			fv = new FileView();
			view = inflater.inflate(R.layout.list_view_file, parent, false);
			fv.name = (TextView) view.findViewById(R.id.file_name);
			fv.size = (TextView) view.findViewById(R.id.file_size);
			fv.lastModif = (TextView) view.findViewById(R.id.file_lastModif);
			fv.checkbox = (CheckBox) view.findViewById(R.id.chk_file);
			fv.checkbox.setOnClickListener(this);
			fv.icon = (ImageView) view.findViewById(R.id.file_icon);
			view.setTag(fv);
		} else {
			fv = (FileView) view.getTag();
		}
		//get data from file and set view on screen
		FileInfo file = files.get(position);
		fv.name.setText(file.getName());
		fv.checkbox.setChecked(file.getChecked());
		fv.icon.setImageDrawable(file.getType().getIcon());
		fv.checkbox.setTag(Integer.valueOf(position));
		fv.lastModif.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(file.getLastModif()));
		if (file.isFolder()){
			fv.size.setText("");
		}
		else{
			long fileSize = file.getSize();
			if (fileSize < 1000) {
				fv.size.setText(fileSize + " b");
			} else {
				fileSize /= 1000;
				if (fileSize > 1000) {
					fv.size.setText((fileSize / 1000) + " Mb");
				} else {
					fv.size.setText(fileSize + " Kb");
				}
			}
		}
		
		return view;
	}
	
	public static int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}
	
	public void onClick(View v) {

		//save state to selected file
		Integer tag = (Integer) v.getTag();
		FileInfo file = files.get(tag.intValue());
		file.setChecked(((CheckBox) v).isChecked());

		//notify listener to change view
		checkBoxListener.onClick(v);
	}
	
	public void fillList(List<FileInfo> list){
		if (list != null && !list.isEmpty()){
			files = list;
		}
		else files = null;
		
		notifyDataSetChanged();
	}
	
	public FileInfo getFirstSelectedFile() {
		if (files != null && !files.isEmpty()) {
			for (FileInfo file : files) {
				if (file.getChecked()) {
					return file;
				}
			}
		}

		return null;
	}
	
	public void selectAll(boolean checked) {
		if (files != null && !files.isEmpty()) {
			for (FileInfo file : files) {
				file.setChecked(checked);
			}

			//refresh screen
			notifyDataSetChanged();
		}
	}
	
	public List<FileInfo> getSelected() {
		List<FileInfo> list = null;
		if (files != null && !files.isEmpty()) {
			//search the actual files and return list of selected files
			list = new ArrayList<FileInfo>();
			for (FileInfo file : files) {
				if (file.getChecked()) {
					list.add(file);
				}
			}
		}

		return list;
	}

	public int getSelectedCount() {
		int count = 0;
		if (files != null) {
			for (FileInfo file : files) {
				if (file.getChecked()) {
					count++;
				}
			}
		}

		return count;
	}

	

}
