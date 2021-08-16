package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBBannedAppsHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "TimeManagementApp.db";  //<-- db name
    private final static String _TableName = "BannedApps"; //<-- table name

    public DBBannedAppsHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DATABASE", "create banned app");
        // TODO Auto-generated method stub
        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "( " +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_USER VARCHAR(255), " +
                "_ISCUSTOM INTEGER, " +
                "_DEFAULT VARCHAR(1024), " +
                "_CUSTOM VARCHAR(1024) "+
                ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
        onCreate(db);
    }

}

