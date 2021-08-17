package com.GraduateProject.TimeManagementApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DBTimeBlockHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "TimeManagementApp.db";  //<-- db name
    private final static String _TableNameforTime = "TimeBlocker"; //<-- table name

    public DBTimeBlockHelper(Context context) {

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
        db.execSQL(SQL1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String SQL1 = "DROP TABLE " + _TableNameforTime;
        db.execSQL(SQL1);
    }
}

