package com.hanzeli.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

  public static final String TABLE_SERVERS = "severdb";
  public static final String ID = "_id";
  public static final String NAME = "name";
  public static final String HOST = "host";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String PORT = "port";
  public static final String ANONYM = "anonym";
  public static final String LOCAL_DIR = "local_dir";
  public static final String REMOTE_DIR = "remote_dir";

  private static final String DATABASE_NAME = "servers.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table "
      + TABLE_SERVERS + "(" 
      + ID + " integer primary key autoincrement, " 
      + NAME + " text not null, " 
      + HOST + " text not null, "
      + USERNAME + " text, "
      + PASSWORD + " text, "
      + PORT + " integer not null, "
      + ANONYM + " integer not null, " 
      + LOCAL_DIR + " text, "
      + REMOTE_DIR + " text);";

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVERS);
    onCreate(db);
  }

} 
