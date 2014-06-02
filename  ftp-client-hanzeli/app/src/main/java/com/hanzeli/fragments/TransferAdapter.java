package com.hanzeli.fragments;

import java.util.ArrayList;
import java.util.List;

import com.hanzeli.karlftp.R;
import com.hanzeli.managers.Transfer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TransferAdapter extends ArrayAdapter<Transfer>{

    private final String TAG = "TransferAdapter";

    private LayoutInflater inflater;
	private List<Transfer> allTransfers;

	private OnClickListener checkBoxListener;
	
	private Drawable upIcon;
	private Drawable downIcon;
	
	public class TransferHolder {
		TextView source;
		TextView destination;
		TextView size;
		TextView status;
		ImageView direction;
		CheckBox checkbox;
		ProgressBar progress;
	}
	
	
	public TransferAdapter(Context context, int textViewResourceID, OnClickListener listener, List<Transfer> objects){
        super(context,textViewResourceID,objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		checkBoxListener = listener;
		allTransfers = objects;
		upIcon=context.getResources().getDrawable(R.drawable.upload);
		downIcon=context.getResources().getDrawable(R.drawable.download);
	}


    @Override
    public int getCount() {
		if (allTransfers != null && !allTransfers.isEmpty()) return allTransfers.size();
		return 0;
	}

	@Override
    public Transfer getItem(int position) {
		if (allTransfers != null && !allTransfers.isEmpty()) return allTransfers.get(position);
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		TransferHolder holder;

		if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_view_transfer, parent, false);

			holder = new TransferHolder();

			holder.source = (TextView) convertView.findViewById(R.id.trf_source);
			holder.destination = (TextView) convertView.findViewById(R.id.trf_destination);
			holder.size = (TextView) convertView.findViewById(R.id.trf_fsize);
			holder.status = (TextView) convertView.findViewById(R.id.trf_status);
			holder.direction = (ImageView) convertView.findViewById(R.id.trf_icon);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.trf_checkbox);
			holder.progress = (ProgressBar) convertView.findViewById(R.id.trf_progress);

			convertView.setTag(holder);
		} else {
			holder = (TransferHolder) convertView.getTag();
		}

		final Transfer transfer = getItem(position);
		holder.source.setText(transfer.getFromPath());
		holder.destination.setText(transfer.getToPath());
        holder.progress.setMax(100);
        holder.progress.setProgress(0);
		holder.checkbox.setChecked(transfer.getChecked());
		holder.checkbox.setTag(position);

        holder.checkbox.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                boolean b = transfer.getChecked();
                transfer.setChecked(!b);
            }
        });
		
		if (transfer.getDirection()==0) {
			holder.direction.setImageDrawable(downIcon);
		} else {
			holder.direction.setImageDrawable(upIcon);
		}

		// File size
		long size = transfer.getSize();
		if (size < 1024) {
			holder.size.setText(size + " b");
		} else {
			size = (size / 1024) + 1;
			if (size > 1024) {
				holder.size.setText((size / 1024) + " Mb");
			} else {
				holder.size.setText(size + " Kb");
			}
		}
		holder.status.setText(R.string.status_waiting);
		// nastavenie progress baru pocas progresu ... ocekovat este ten status
		if (!transfer.getDone()) {
			holder.status.setVisibility(View.GONE);
			holder.progress.setVisibility(View.VISIBLE);
			holder.progress.setProgress(transfer.getProgress());
		} else {
            holder.status.setText(R.string.status_done);
			holder.status.setVisibility(View.VISIBLE);
			holder.progress.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	public void selectAll(boolean isChecked) {
		if (allTransfers != null) {
			for (Transfer transfer : allTransfers) {
				transfer.setChecked(isChecked);
			}
			//refresh
			notifyDataSetChanged();
		}
	}
	
	public void setTransferList(List<Transfer> transferList) {

		if ((transferList != null) && !transferList.isEmpty()) {
			allTransfers = transferList;
		} else {
			allTransfers = null;
		}
		//refresh
		notifyDataSetChanged();
	}
	
	public List<Transfer> getSelected() {
		List<Transfer> list = null;
		if (allTransfers != null && !allTransfers.isEmpty()) {
			//search the transfers and return list of selected files
			list = new ArrayList<Transfer>();
			for (Transfer tr : allTransfers) {
				if (tr.getChecked()) {
					list.add(tr);
				}
			}
		}

		return list;
	}
	
	/**
	 * remove all selected items from adapter
	 */
	public void clearSelected() {

		List<Transfer> oldTransferList = allTransfers;
		allTransfers = new ArrayList<Transfer>();

		//make a copy of list with unchecked items
		for (Transfer transfer : oldTransferList) {
			if (!transfer.getChecked()) {
				allTransfers.add(transfer);
			}
		}
		if (allTransfers.isEmpty()) {
			allTransfers = null;
		}

		//refresh
		notifyDataSetChanged();
	}

	/**
	 * 
	 * @return number of selected items
	 */
	public int getSelectedCount() {
		int count = 0;
		if (allTransfers != null) {
			for (Transfer transfer : allTransfers) {
				if (transfer.getChecked()) {
					count++;
				}
			}
		}

		return count;
	}

    public void update(List<Transfer> list){
        if (list != null && !list.isEmpty()) {
            allTransfers = list;
        }
        else allTransfers = null;
        notifyDataSetChanged();
    }
	public void updateProgress(int position, int value){
		Transfer tr = allTransfers.get(position);
		tr.setProgress(value);
		allTransfers.remove(position);
		allTransfers.add(position, tr);
		notifyDataSetChanged();
	}
	

	

}
