package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadingApp extends AppCompatActivity {

    private static List<String> customApps = new ArrayList<>();
    private static List<String> defaultApps = new ArrayList<>();
    private static List<String> bannedApps = new ArrayList<>();
    private static List<String> commuApps = new ArrayList<>();
    private static List<AppInfo> customAppsList = new ArrayList<>();
    private static List<AppInfo> defaultAppsList = new ArrayList<>();
    private static List<AppInfo> bannedAppsList = new ArrayList<>();
    private static List<String> commuAppsList = new ArrayList<>();
    private static int isCustom;
    private final String[] bannedCat = {"artdesign", "shopping", "games", "social", "entertainment", "videoplayerseditors", "comics"};
    private final List<String> banned = Arrays.asList(bannedCat);
    private final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    private String userName;
    private DBBannedAppHelper dbBannedAppsHelper = null;
    private final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static final String COL_CHECK = "_ISCUSTOM";
    private SQLiteDatabase db = null;
    private Gson gson = new Gson();
    private Thread loadingThread;
    private Thread loadingThreadCustom;
    private Thread loadingThreadDefault;
    private boolean exist;


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadingapp);

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        //先列出所有禁用App(新用戶)
        loadingThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    setDefaultAllowedApps(startLoading());
                    setAllowedApps(getDefaultAllowedApps());
                    String inputString= gson.toJson(getDefaultAllowedApps());
                    Log.e("INSERT", inputString);
                    insertDB(userName, 0, inputString, inputString);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setIsCustom(checkIfCustom(userName));
                    Intent main = new Intent(LoadingApp.this, GeneralTimerActivity.class);
                    startActivity(main);
                    closeDB();
                    finish();
                }
            }
        };

        //先列出所有禁用App(custom)
        loadingThreadCustom = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    setCustomAllowedApps(getCustomApps());
                    startLoadingCustom(getCustomAllowedApps());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setIsCustom(checkIfCustom(userName));
                    Intent main = new Intent(LoadingApp.this, GeneralTimerActivity.class);
                    startActivity(main);
                    closeDB();
                    finish();
                }
            }
        };

        //先列出所有禁用App(default)
        loadingThreadDefault = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    setDefaultAllowedApps(startLoading());
                    setAllowedApps(getDefaultAllowedApps()); //只要友直就是預設？
                    String updateString= gson.toJson(getDefaultAllowedApps());
                    Log.e("UPDATE", updateString);
                    defaultAppsUpdateDB(updateString);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setIsCustom(checkIfCustom(userName));
                    Intent main = new Intent(LoadingApp.this, GeneralTimerActivity.class);
                    startActivity(main);
                    closeDB();
                    finish();
                }
            }
        };

        if(isFirstRun){
            openDB();
            userName = Build.USER;
            getApps(loadingThread, loadingThreadCustom, loadingThreadDefault, exist);
            exist = checkIfUserExists(userName);
            Log.e("START", "LOAD APP, exist = "+ exist);
        }
    }

    private void getApps(Thread thread1, Thread thread2, Thread thread3, boolean exist){
        if(exist == false) {
            Log.e("GET", "user not exist, add USER");
            thread1.start();
        }
        else if(checkIfCustom(userName) == 1){
            Log.e("GET", "user app custom");
            thread2.start();
        }
        else{
            Log.e("GET", "user app default, need update");
            thread3.start();
        }
    }

    //取得預設禁用APP
    protected List<String> startLoading(){
        Log.e("CATEGORY", "start checking");
        String category;
        String communication = "communication";
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info : homeApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLogo(info.activityInfo.loadIcon(packageManager));
            appInfo.setPackageName(info.activityInfo.packageName);
            appInfo.setAppName((String) info.activityInfo.loadLabel(packageManager));
            String query_url = GOOGLE_URL + info.activityInfo.packageName + "&hl=en";
            category = getCategory(query_url).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            Log.e("CATEGORY",category);
            if(banned.contains(category)){
                appInfo.setAppStatus(true);
                Log.e("check",appInfo.getPackageName() + "is added");
                defaultApps.add(appInfo.getPackageName());
            }else if(category.equals(communication)){    //社交APP禁用開始操作
                appInfo.setAppStatus(true);
                Log.e("check",appInfo.getPackageName() + "is added");
                commuApps.add(appInfo.getPackageName());
            }else{
                appInfo.setAppStatus(false);
            }
            defaultAppsList.add(appInfo);
        }
        setAllowedAppInfos(defaultAppsList);
        return defaultApps;
    }


    //取得自訂禁用APP
    protected void startLoadingCustom(List<String> customs){
        Log.e("CATEGORY", "change status");
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info : homeApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLogo(info.activityInfo.loadIcon(packageManager));
            appInfo.setPackageName(info.activityInfo.packageName);
            appInfo.setAppName((String) info.activityInfo.loadLabel(packageManager));
            if(customs.contains(appInfo.getPackageName())){
                appInfo.setAppStatus(true);
                Log.e("check",appInfo.getPackageName() + "change to banned");
            }
            else{
                appInfo.setAppStatus(false);
            }
            customAppsList.add(appInfo);
        }
    }

    private String getCategory(String query_url) {

        try {
            Document doc = Jsoup.connect(query_url).get();

            if (doc != null) {
                Element link = doc.select("a[itemprop=genre]").first();
                return link.text();
            } else{
                return "null doc";
            }
        } catch (Exception e) {
            return "error";
        }
    }


    //資料庫相關
    //打開database
    private void openDB() {
        dbBannedAppsHelper = new DBBannedAppHelper(this);
        db = dbBannedAppsHelper.getWritableDatabase();
    }

    public boolean checkIfUserExists(String user) {
        /**String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + user + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;**/
        String count = "SELECT count(*) FROM " + TABLE_APPS;
        Cursor cursor = db.rawQuery(count, null);
        cursor.moveToFirst();
        int icount = cursor.getInt(0);
        if(icount == 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public int checkIfCustom(String user) {
        String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + user + "'";
        Cursor cursor = db.rawQuery(Query, null);
        int isCustom = cursor.getColumnIndex(COL_CHECK);
        cursor.close();
        return isCustom;
    }

    private void insertDB(String user ,int isCustom, String defaults, String customs){
        ContentValues values = new ContentValues();
        values.put("_USER ",user);
        values.put("_ISCUSTOM",isCustom);
        values.put("_DEFAULT", defaults);
        values.put("_CUSTOM", customs);
        db.insert(TABLE_APPS,null,values);
    }

    public void defaultAppsUpdateDB(String defaults){
        ContentValues values = new ContentValues();
        values.put("_DEFAULT", defaults);
        db.update(TABLE_APPS,values,COL_USER + " = " + "'" + userName + "'", null);
    }

    public void customAppsUpdateDB(String customs){
        ContentValues values = new ContentValues();
        values.put("_CUSTOM", customs);
        db.update(TABLE_APPS,values,COL_USER + " = " + "'" + userName + "'", null);
    }

    private ArrayList<String> getCustomApps(){
        String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + userName + "'";
        Cursor cursor = db.rawQuery(Query, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String app = cursor.getString(4);
        ArrayList<String>  apps = gson.fromJson(app, type);
        cursor.close();
        return apps;
    }

    private void closeDB() {
        dbBannedAppsHelper.close();
    }


    //更改
    public static void setIsCustom(int custom){
        isCustom = custom;
    }
    public static void setCustomAllowedApps(List<String> app){
        customApps = app;
    }
    public static void setAllowedApps(List<String> app){
        bannedApps = app;
    }
    private static void setDefaultAllowedApps(List<String> app){
        defaultApps = app;
    }
    public static void setAllowedAppInfos(List<AppInfo> appList){
        bannedAppsList = appList;
    }
    public static void setAllowedCommuApps(List<String> Commuapp){ commuAppsList = Commuapp; }

    //取得
    public static List<String> getCustomAllowedApps(){
        return customApps;
    }
    public static List<String> getAllowedApps(){
        return bannedApps;
    }
    public static List<String> getDefaultAllowedApps(){
        return defaultApps;
    }
    public static List<AppInfo> getAllowedAppInfos(){
        return bannedAppsList;
    }
    public static int getIsCustom(){
        return isCustom;
    }
    public static List<String> getAllowedCommuApps(){
        return commuApps;
    }
}

