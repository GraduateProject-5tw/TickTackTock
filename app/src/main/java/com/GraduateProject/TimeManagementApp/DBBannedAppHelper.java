package com.GraduateProject.TimeManagementApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DBBannedAppHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "BannedApp.db";  //<-- db name
    private final static String _TableNameforBanned = "BannedApps"; //<-- table name

    public DBBannedAppHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String SQL2 = "CREATE TABLE IF NOT EXISTS " + _TableNameforBanned + "( " +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_USER VARCHAR(255) NOT NULL, " +
                "_ISCUSTOM INTEGER NOT NULL, " +
                "_DEFAULT VARCHAR(1024) NOT NULL, " +
                "_CUSTOM VARCHAR(1024) NOT NULL"+
                ");";
        db.execSQL(SQL2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String SQL2 = "DROP TABLE " + _TableNameforBanned;
        db.execSQL(SQL2);
        onCreate(db);
    }
}

