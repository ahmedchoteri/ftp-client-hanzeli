package com.hanzeli.transfer;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Transfer implements Parcelable{
	private int id;
	private String fileName;
	private String fromPath;
	private String toPath;
	private long size;
	private int direction;	//1= upload, 0=download
	private boolean waiting;
	private boolean done;
	private int progress;
	private boolean checked;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public boolean isWaiting(){
		return waiting;
	}
	
	public void setWaiting(boolean waiting){
		this.waiting = waiting;
	}

	public boolean getDone() {
		return done;
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
	
	public boolean getChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	/**
	 * building complete from path for transfer
	 * @return complete from path
	 */
	public String getCmplFromPath(){
		String path = fromPath;
		if (!fromPath.endsWith(File.separator)){
			path+=File.separator;
		}
		return path+fileName;
	}
	
	/**
	 * building complete to path for transfer
	 * @return complete to path
	 */
	public String getCmplToPath(){
		String path = toPath;
		if (!toPath.endsWith(File.separator)){
			path+=File.separator;
		}
		return path+fileName;
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
