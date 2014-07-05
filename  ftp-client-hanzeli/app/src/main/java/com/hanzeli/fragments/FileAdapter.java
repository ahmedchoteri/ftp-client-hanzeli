package com.hanzeli.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hanzeli.karlftp.R;
import com.hanzeli.managers.FileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class for passing data to ListView
 * @author Michal
 *
 */
public class FileAdapter extends ArrayAdapter<FileInfo>{
	
	List<FileInfo> files = new ArrayList<FileInfo>();
	protected LayoutInflater inflater;	
	private OnClickListener checkBoxListener;
    private String whoAmI = "";
	
	class FileHolder {
		private TextView name = null;
		//private TextView size = null;
		//private TextView lastModified = null;
		private CheckBox checkbox = null;
		private ImageView icon = null;
        private ImageView detail = null;

        FileHolder(View row){
            name = (TextView)row.findViewById(R.id.file_name);
            //size = (TextView)row.findViewById(R.id.file_size);
            //lastModified = (TextView)row.findViewById(R.id.file_lastModif);
            checkbox = (CheckBox)row.findViewById(R.id.chk_file);
            icon = (ImageView)row.findViewById(R.id.file_icon);
            detail = (ImageView) row.findViewById(R.id.detail_image);
        }

        void populateFrom(FileInfo fi, int position){
            name.setText(fi.getName());
            checkbox.setChecked(fi.getChecked());
            checkbox.setOnClickListener(checkBoxListener);
            icon.setImageDrawable(fi.getType().getIcon());
            checkbox.setTag(position);
            detail.setTag(position);
            detail.setOnClickListener(checkBoxListener);
            /*lastModified.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.US).format(fi.getLastModif()));
            if (fi.isFolder()){
                size.setText("");
            }
            else{
                long fileSize = fi.getSize();
                if (fileSize < 1000) {
                    size.setText(fileSize + " b");
                } else {
                    fileSize /= 1000;
                    if (fileSize > 1000) {
                        size.setText((fileSize / 1000) + " Mb");
                    } else {
                        size.setText(fileSize + " Kb");
                    }
                }
            }*/
        }
	}
	
	FileAdapter(Context context, String s, ArrayList<FileInfo> files){
        super(context,R.layout.list_view_file,files);
        this.files=files;
        //ziskanie systemoveho inflatera z contextu kedze som mimo activity
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		whoAmI = s;
		//checkBoxListener = listener;
        notifyDataSetChanged();
	}

    public void setCheckBoxListener(OnClickListener listener){
        checkBoxListener = listener;
    }
	
	public int getCount() {
		if (files != null && !files.isEmpty()) return files.size();
		return 0;
	}

	public FileInfo getItem(int index) {
		if (files != null && !files.isEmpty()) return files.get(index);
		return null;
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
        FileHolder holder;
		if (row == null) {
			//construkcia noveho holdera pre fileInfo
			row = inflater.inflate(R.layout.list_view_file, parent, false);
            holder = new FileHolder(row);
			row.setTag(holder);
		} else {
			holder = (FileHolder)row.getTag();
		}
		//obsadenie holdera datami z fileInfo
        holder.populateFrom(files.get(position),position);
		return row;
	}

    public void update(ArrayList<FileInfo> list){
        if (list != null && !list.isEmpty()){
            files = list;
        }
        else files = null;  //tu by bolo dobre dat nejaku exception
        //refreshnutie gui pretoze bol zmeneny data set
        notifyDataSetChanged();

    }

	/*public void onClick(View v) {

		//save state to selected file
		Integer tag = (Integer) v.getTag();
		FileInfo file = files.get(tag.intValue());
		file.setChecked(((CheckBox) v).isChecked());

		//notify listener to change view
		checkBoxListener.onClick(v);
	}



	public void fillList(ArrayList<FileInfo> list){
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
	
	public ArrayList<FileInfo> getSelected() {
		ArrayList<FileInfo> list = null;
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
	}*/

	

}
