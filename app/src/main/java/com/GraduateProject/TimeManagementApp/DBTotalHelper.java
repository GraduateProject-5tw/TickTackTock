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
    private final static String _TableNameforCourse = "Courses"; //<-- table name
    private final static String _TableNameforBG = "Background";

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
                "_BANNEDCOMMU INTEGER NOT NULL," +
                "_ALL VARCHAR(2048) NOT NULL, " +
                "_COMMUNICATE VARCHAR(1024) NOT NULL, " +
                "_DEFAULT VARCHAR(1024) NOT NULL, " +
                "_CUSTOM VARCHAR(1024) NOT NULL"+
                ");";
        //db.execSql(SQL3);  for todolist
        String SQL4 = "CREATE TABLE IF NOT EXISTS " + _TableNameforCourse + "( " +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_COURSE TEXT NOT NULL, " +
                "_COLOR INTEGER NOT NULL, " +
                "_TEXT INTEGER NOT NULL " +
                ");";
        String SQL5 = "CREATE TABLE IF NOT EXISTS " + _TableNameforBG + "( " +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_GENERAL INTEGER NOT NULL, " +
                "_TOMATO INTEGER NOT NULL, " +
                "_TIMEBLOCK INTEGER NOT NULL, " +
                "_TODO INTEGER NOT NULL " +
                ");";
        db.execSQL(SQL2);
        db.execSQL(SQL1);
        db.execSQL(SQL4);
        db.execSQL(SQL5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String SQL1 = "DROP TABLE " + _TableNameforTime;
        String SQL2 = "DROP TABLE " + _TableNameforBanned;
        String SQL4 = "DROP TABLE " + _TableNameforCourse;
        db.execSQL(SQL1);
        db.execSQL(SQL2);
        db.execSQL(SQL4);
        onCreate(db);
    }


    public void onDelete(SQLiteDatabase db, int oldVersion,int newVersion){
    //
    }

    public Cursor ViewData(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from "+_TableNameforTime,null);
        cursor.moveToFirst();
        return cursor;
    }

}

