package com.hanzeli.fragments;

import java.util.ArrayList;
import java.util.List;

import com.hanzeli.karlftp.R;
import com.hanzeli.resources.FileInfo;

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
	
	class FileHolder {
		private TextView name = null;
		private CheckBox checkbox = null;
		private ImageView icon = null;
        private ImageView detail = null;

        FileHolder(View row){
            name = (TextView)row.findViewById(R.id.file_name);
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
        }
	}
	
	FileAdapter(Context context, ArrayList<FileInfo> files){
        super(context,R.layout.list_view_file,files);
        this.files=files;
        //ziskanie systemoveho inflatera z contextu kedze som mimo activity
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        else files = null;
        //refreshnutie gui pretoze bol zmeneny data set
        notifyDataSetChanged();

    }
}
