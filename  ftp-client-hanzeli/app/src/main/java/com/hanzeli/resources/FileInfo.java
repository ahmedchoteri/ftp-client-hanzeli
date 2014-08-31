package com.hanzeli.resources;


/**
 * class storing information about file that should be shown on screen
 * @author Michal
 *
 */
public class FileInfo {
	
	private String name;
	private boolean checked;
	private FileTypes type;
	private long size;
	private long lastModified;
	private String absolutePath;
	private String parentPath;
	
	public FileInfo(String name){
		this.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setChecked(boolean check){
		checked=check;
	}
	
	public boolean getChecked(){
		return checked;
	}
	
	public void setType(FileTypes type){
		this.type=type;
	}
	
	public FileTypes getType(){
		return type;
	}
	
	public void setSize(long size){
		this.size=size;
	}
	
	public long getSize(){
		return size;
	}
	
	public void setLastModif(long time){
		lastModified=time;
	}
	
	public long getLastModif(){
		return lastModified;
	}
	
	public void setAbsPath(String path){
		absolutePath=path;
	}
	
	public String getAbsPath(){
		return absolutePath;
	}
	
	public void setParentPath(String parent){
		parentPath=parent;
	}
	
	public String getParentPath(){
		return parentPath;
	}
	
	public boolean isFolder() {
		return type == FileTypes.FOLDER;
	}
	
}
