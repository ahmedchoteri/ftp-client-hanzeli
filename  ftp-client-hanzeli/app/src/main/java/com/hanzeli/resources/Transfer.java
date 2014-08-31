package com.hanzeli.resources;

import android.os.Parcel;
import android.os.Parcelable;

import com.hanzeli.managers.TransferManager;

public class Transfer implements Parcelable{
	private int id;
	private String fileName;
	private String fromPath;
	private String toPath;
    public TransferType type;
	private long size;
    private long tmpSize;
    private int tmpProgress;
	private int direction;	//1= upload, 0=download
	private boolean waiting;
	private boolean done;
	private int progress;
	private boolean checked;
    public FileInfo[] transferFiles;
    public boolean cut;
    public boolean fail = false;
    public boolean stopped = false;
    public TransferManager.SyncItem[] syncItems;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFromPath() {
		return fromPath;
	}
	
	public void setFromPath(String fromPath) {
		this.fromPath = fromPath;
	}
	
	public String getToPath() {
		return toPath;
	}
	
	public void setToPath(String toPath) {
		this.toPath = toPath;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

    public long getTmpSize(){
        return tmpSize;
    }

    public void setTmpSize(long tmpSize){
        this.tmpSize=tmpSize;
    }

    public int getTmpProgress(){
        return tmpProgress;
    }

    public void setTmpProgress(int tmpProgress){
        this.tmpProgress=tmpProgress;
    }

	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public boolean isWaiting(){
        return waiting && !done;
    }

    public boolean isDone() {
        return !waiting && done;
    }

    public boolean isWorking(){
        return waiting && done;
    }

    public boolean isCounting(){
        return !waiting && !done;
    }

	public void setWaiting(boolean waiting){
		this.waiting = waiting;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeInt(id);
        out.writeString(fileName);
        out.writeString(fromPath);
        out.writeString(toPath);
        out.writeLong(size);
        out.writeInt(direction);
        out.writeInt(progress);
        out.writeByte((byte)(waiting ? 1 : 0));
        out.writeByte((byte)(done ? 1 : 0));
        out.writeByte((byte)(checked ? 1 : 0));
    }

    public static final Creator<Transfer> CREATOR = new Creator<Transfer>() {
        public Transfer createFromParcel(Parcel in){
            return new Transfer(in);
        }
        public Transfer[] newArray(int size){
            return new Transfer[size];
        }
    };

    public Transfer(Parcel in){

        id = in.readInt();
        fileName = in.readString();
        fromPath = in.readString();
        toPath = in.readString();
        size = in.readLong();
        direction = in.readInt();
        progress = in.readInt();
        waiting = in.readByte() != 0;
        done = in.readByte() != 0;
        checked = in.readByte() != 0;

    }

    public Transfer(){}
}
