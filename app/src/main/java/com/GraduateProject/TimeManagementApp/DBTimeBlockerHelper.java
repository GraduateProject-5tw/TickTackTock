package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBTimeBlockerHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "TimeManagementApp.db";  //<-- db name
    private final static String _TableName = "TimeBlocker"; //<-- table name
    public DBTimeBlockerHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_DATE TEXT, " +
                "_STARTTIME INTEGER," +
                "_STOPTIME INTEGER," +
                "_TOTAL INTEGER" +
                ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
    }
}
