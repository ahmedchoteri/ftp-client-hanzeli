package com.hanzeli.resources;

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
	
	private FileTypes(String name, int id, int iconId) {

		this.formatName = name;
        this.formatId = id;
		this.icon = MainApplication.getInstance().getResources().getDrawable(iconId);
		
		
	}
	//constructors for default used file types
	public final static FileTypes FOLDER = new FileTypes("FOLDER", 0, R.drawable.image_folder);
	public final static FileTypes UNKNOWN = new FileTypes("UNKNOWN", 30, R.drawable.image_unknown);
    public final static FileTypes TEXT = new FileTypes("TXT",1, R.drawable.image_txt);
    public final static FileTypes MUSIC = new FileTypes("MUSIC",2, R.drawable.music);
    public final static FileTypes DOC = new FileTypes("DOC",3, R.drawable.image_doc);
    public final static FileTypes PDF = new FileTypes("PDF",4, R.drawable.image_pdf);
    public final static FileTypes JPG = new FileTypes("JPG",5, R.drawable.image_jpg);
    public final static FileTypes PNG = new FileTypes("PNG",6, R.drawable.image_png);
    public final static FileTypes AVI = new FileTypes("AVI",6, R.drawable.image_avi);

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
	

	
	public static FileTypes getType(File file) {
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return fromExt1(file);

	}
	
	public static FileTypes getType(FTPFile file){
		if (file.isDirectory()) {
			return FOLDER;
		}
		else return fromExt(file);
	}

    private static FileTypes fromExt(FTPFile file){
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot < 0) {
            return UNKNOWN;
        }
        String ext = file.getName().substring(lastDot + 1).toUpperCase();
        if (ext.equals("TXT")) return TEXT;
        if (ext.equals("MP3")) return MUSIC;
        if (ext.equals("PDF")) return PDF;
        if (ext.equals("DOC")) return DOC;
        if (ext.equals("JPG")) return JPG;
        if (ext.equals("PNG")) return PNG;
        if (ext.equals("AVI")) return AVI;
        return UNKNOWN;
    }

    private static FileTypes fromExt1(File file){
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot < 0) {
            return UNKNOWN;
        }
        String ext = file.getName().substring(lastDot + 1).toUpperCase();
        if (ext.equals("TXT")) return TEXT;
        if (ext.equals("MP3")) return MUSIC;
        if (ext.equals("PDF")) return PDF;
        if (ext.equals("DOC")) return DOC;
        if (ext.equals("JPG")) return JPG;
        if (ext.equals("PNG")) return PNG;
        if (ext.equals("AVI")) return AVI;
        return UNKNOWN;
    }
}
