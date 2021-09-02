package com.GraduateProject.TimeManagementApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DBTotalHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "AllDataHere.db";  //<-- db name
    private final static String _TableNameforTime = "TimeBlocker"; //<-- table name
    private final static String _TableNameforBanned = "BannedApps"; //<-- table name

    public DBTotalHelper(Context context) {

        super(context, _DBName, null, _DBVersion);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String SQL1 = "CREATE TABLE IF NOT EXISTS " + _TableNameforTime + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_DATE TEXT, " +
                "_COURSE TEXT," +
                "_STARTTIME TEXT," +
                "_STOPTIME TEXT," +
                "_TOTAL TEXT" +
                ");";
        String SQL2 = "CREATE TABLE IF NOT EXISTS " + _TableNameforBanned + "( " +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_USER VARCHAR(255) NOT NULL, " +
                "_ISCUSTOM INTEGER NOT NULL, " +
                "_ALL VARCHAR(2048) NOT NULL, " +
                "_DEFAULT VARCHAR(1024) NOT NULL, " +
                "_CUSTOM VARCHAR(1024) NOT NULL"+
                ");";
        //db.execSql(SQL3);  for todolist
        db.execSQL(SQL2);
        db.execSQL(SQL1);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String SQL1 = "DROP TABLE " + _TableNameforTime;
        String SQL2 = "DROP TABLE " + _TableNameforBanned;
        db.execSQL(SQL1);
        db.execSQL(SQL2);
        onCreate(db);
    }

    public void onDelete(SQLiteDatabase db, int oldVersion,int newVersion){
    //
    }

}
