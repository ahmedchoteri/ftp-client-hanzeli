package com.hanzeli.transfer;

public class Transfer {
	private int id;
	private String fileName;
	private String fromPath;
	private String toPath;
	private long size;
	private int direction;	//1= upload, 0=download
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
	
}
