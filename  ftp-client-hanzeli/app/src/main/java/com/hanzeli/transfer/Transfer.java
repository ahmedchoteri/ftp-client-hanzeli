package com.hanzeli.transfer;

import java.io.File;

public class Transfer {
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
    public boolean dirty = false;
	
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
}
