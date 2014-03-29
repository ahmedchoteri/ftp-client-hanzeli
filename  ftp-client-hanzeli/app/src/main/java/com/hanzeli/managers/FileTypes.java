package com.hanzeli.managers;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;

import android.graphics.drawable.Drawable;

import com.hanzeli.karlftp.MainApplication;
import com.hanzeli.karlftp.R;

public class FileTypes {
	private String formatName;
    private int formatId;
	private Drawable icon;
	private boolean ascii;
	
	private FileTypes(String name, int id, int iconId, boolean ascii) {

		this.formatName = name;
		//this.format = MainApplication.getInstance().getString(formatId);
        this.formatId = id;
		this.icon = MainApplication.getInstance().getResources().getDrawable(iconId);
		this.ascii = ascii;
		
		
	}
	//constructors for default used file types
	public final static FileTypes FOLDER = new FileTypes("FOLDER", 0, R.drawable.image_folder, false);
	public final static FileTypes UNKNOWN = new FileTypes("UNKNOWN", 3000, R.drawable.image_unknown, false);
    //dorobit konstruktory na ostatne typy suborou
	public String getFormatName(){
		return formatName;
	}
	//vrati obrazok pre typ suboru
	public Drawable getIcon(){
		return icon;
	}
	
	public int getFormatId(){
		return formatId;
	}
	
	public boolean getAscii(){
		return ascii;
	}
	
	public static FileTypes getType(File file) {
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return UNKNOWN;
            //tuto treba dorobit testovanie suborou na zaklade ich pripony
	}
	
	public static FileTypes getType(FTPFile file){
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return UNKNOWN;
	}
}
