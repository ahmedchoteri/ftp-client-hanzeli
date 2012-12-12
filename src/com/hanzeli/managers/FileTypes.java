package com.hanzeli.managers;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;

import android.graphics.drawable.Drawable;

import com.hanzeli.ftpdroid.MainApplication;
import com.hanzeli.ftpdroid.R;

public class FileTypes {
	private String code;
	private String format;
	private String extension;
	private Drawable icon;
	private int ordNum;
	private boolean ascii;
	
	private FileTypes(String code, int formatId, String extension, int iconId, int order, boolean ascii) {

		this.code = code;
		//this.format = MainApplication.getInstance().getString(formatId);
		this.extension = extension;
		this.icon = MainApplication.getInstance().getResources().getDrawable(iconId);
		this.ordNum = order;
		this.ascii = ascii;
		
		
	}
	
	//constructors for default used file types
	public final static FileTypes FOLDER = new FileTypes("FOLDER", 0, null, R.drawable.image_folder, R.string.file_folder, false);
	public final static FileTypes UNKNOWN = new FileTypes("UNKNOWN", 3000, null, R.drawable.image_unknown, R.string.file_unknown, false);

	public String getCode(){
		return code;
	}
	
	public String getFormat(){
		return format;
	}
	
	public String getExtension(){
		return extension;
	}
	
	public Drawable getIcon(){
		return icon;
	}
	
	public int getOrdNum(){
		return ordNum;
	}
	
	public boolean getAscii(){
		return ascii;
	}
	
	public static FileTypes getType(File file) {
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return UNKNOWN;
	}
	
	public static FileTypes getType(FTPFile file){
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return UNKNOWN;
	}
}
