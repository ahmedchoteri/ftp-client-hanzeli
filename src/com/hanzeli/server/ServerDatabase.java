package com.hanzeli.server;

import java.util.*;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.content.ContentValues;
import android.content.Context;

public class ServerDatabase{
	
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { 
			MySQLiteHelper.ID,
		    MySQLiteHelper.NAME, 
		    MySQLiteHelper.HOST,
		    MySQLiteHelper.USERNAME,
		    MySQLiteHelper.PASSWORD,
		    MySQLiteHelper.PORT,
		    MySQLiteHelper.ANONYM,
		    MySQLiteHelper.LOCAL_DIR,
		    MySQLiteHelper.REMOTE_DIR};
	
	public ServerDatabase(Context context){
		dbHelper= new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Updating database with new entry or changing old entry
	 * @param server
	 * @return update result
	 */
	public long update(Server server){
		
		//New database entry
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.NAME, server.getName());
		values.put(MySQLiteHelper.HOST, server.getHost());
		values.put(MySQLiteHelper.USERNAME, server.getUsername());
		values.put(MySQLiteHelper.PASSWORD, server.getPassword());
		values.put(MySQLiteHelper.PORT, server.getPort());
		values.put(MySQLiteHelper.ANONYM, server.getAnonym());
		values.put(MySQLiteHelper.LOCAL_DIR, server.getLocalDir());
		values.put(MySQLiteHelper.REMOTE_DIR, server.getRemoteDir());
		
		//If new insert else update
		long id;
		if (server.getId() == 0) {
			id = database.insert(MySQLiteHelper.TABLE_SERVERS, null, values);
			server.setId(id);
		} 
		else {
			values.put(MySQLiteHelper.ID, safeLongToInt(server.getId()));
			id=database.update(MySQLiteHelper.TABLE_SERVERS, values, MySQLiteHelper.ID + " = " + server.getId(), null);	
		}
		return id; //return result of database update, -1 or 0 means error in operation
	}
	
	/**
	 * Removing existing server
	 * @param id
	 * @return
	 */
	public boolean remove(long id) {
		return database.delete(MySQLiteHelper.TABLE_SERVERS, MySQLiteHelper.ID + " = " + id, null) == 1;
	}

	public List<Server> allServers() {
		List<Server> all = new ArrayList<Server>();

		//Open cursor
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS, allColumns, null, null, null, null, MySQLiteHelper.ID + " ASC");
		//Fetching all entries from database 
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Server server = new Server();
			server.setId(cursor.getInt(0));
			server.setName(cursor.getString(1));
			server.setHost(cursor.getString(2));
			server.setUsername(cursor.getString(3));
			server.setPassword(cursor.getString(4));
			server.setPort(cursor.getInt(5));
			server.setAnonym((cursor.getInt(6) == 1) ? true : false);
			server.setLocalDir(cursor.getString(7));
			server.setRemoteDir(cursor.getString(8));
			all.add(server);
			cursor.moveToNext();
		}
		cursor.close();
		return all;
	}
	
	public static int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}

}
