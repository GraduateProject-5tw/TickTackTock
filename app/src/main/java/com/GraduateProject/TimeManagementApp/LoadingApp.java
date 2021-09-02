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
    private static List<String> realDefaultApps = new ArrayList<>();
    private static List<String> bannedApps = new ArrayList<>();
    private static final List<String> allApps = new ArrayList<>();
    private static final List<String> commuApps = new ArrayList<>();
    private static List<AppInfo> customAppsList = new ArrayList<>();
    private static final List<AppInfo> defaultAppsList = new ArrayList<>();
    private static List<AppInfo> bannedAppsList = new ArrayList<>();
    private static int isCustom;
    private final String[] bannedCat = {"artdesign", "shopping", "casual","puzzle", "social", "adventure", "casino", "sports", "card", "simulation", "music", "board", "strategy", "action", "entertainment", "videoplayerseditors", "comics"};
    private final List<String> banned = Arrays.asList(bannedCat);
    private final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    private String userName;
    private DBTotalHelper dbBannedAppsHelper = null;
    private final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static final String COL_CHECK = "_ISCUSTOM";
    private SQLiteDatabase db = null;
    private static final Gson gson = new Gson();


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadingapp);

        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        //先列出所有禁用App(新用戶)
        Thread loadingThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    defaultApps = startLoading();
                    bannedApps = defaultApps;
                    customApps = defaultApps;
                    String inputString1 = gson.toJson(defaultApps);
                    String inputString2 = gson.toJson(allApps);
                    Log.e("INSERT", inputString1);
                    Log.e("INSERT", inputString2);
                    insertDB(userName, inputString2, inputString1, inputString1);
                    realDefaultApps = getDefaultApps();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isCustom = checkIfCustom(userName);
                    Intent main = new Intent(LoadingApp.this, GeneralTimerActivity.class);
                    startActivity(main);
                    closeDB();
                    finish();
                }
            }
        };

        //先列出所有禁用App(custom)
        Thread loadingThreadCustom = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    customApps = getCustomApps();
                    if(checkNewApps()){
                        realDefaultApps = startLoadingDefault();
                    }
                    else{
                        realDefaultApps = getDefaultApps();
                    }
                    bannedApps = customApps;
                    startLoadingCustom(customApps);
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
        Thread loadingThreadDefault = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    if(checkNewApps()){
                        realDefaultApps = startLoadingDefault();
                        defaultApps = startLoadingDefault();
                        String updateString = gson.toJson(getDefaultAllowedApps());
                        Log.e("UPDATE", updateString);
                        defaultAppsUpdateDB(updateString);
                    }
                    else{
                        defaultApps = getDefaultApps();
                        realDefaultApps = getDefaultApps();
                    }
                    customApps = getCustomApps();
                    bannedApps = defaultApps;
                    startLoadingCustom(customApps);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isCustom = checkIfCustom(userName);
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
            boolean exist = checkIfUserExists();
            Log.e("START", "LOAD APP, exist = "+ exist);
            getApps(loadingThread, loadingThreadCustom, loadingThreadDefault, exist);
        }
    }

    private void getApps(Thread thread1, Thread thread2, Thread thread3, boolean exist){
        if(!exist) {
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
    protected boolean checkNewApps(){
        Log.e("NEW", "start checking");
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);

        List<String> databaseDefaults = getAllApps();
        if(homeApps.size() != databaseDefaults.size()){
            Log.e("NEW", "new apps found? true "+homeApps.size()+" database "+databaseDefaults.size());
            return true;
        }
        else{
            Log.e("NEW", "new apps found? false");
            return false;
        }
    }


    //取得預設禁用APP
    protected List<String> startLoading(){
        List<String> defaultApps = new ArrayList<>();
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
            allApps.add(appInfo.getPackageName());
            defaultAppsList.add(appInfo);
        }
        setAllowedAppInfos(defaultAppsList);
        customAppsList = defaultAppsList;
        return defaultApps;
    }


    //取得預設禁用APP
    protected List<String> startLoadingDefault(){
        List<String> defaultApps = new ArrayList<>();
        Log.e("CATEGORY DEFAULT", "start checking");
        String category;
        String communication = "communication";
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info : homeApps) {
            String query_url = GOOGLE_URL + info.activityInfo.packageName + "&hl=en";
            category = getCategory(query_url).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            Log.e("CATEGORY",category);
            if(banned.contains(category)){
                Log.e("check",info.activityInfo.packageName + "is added to main");
                defaultApps.add(info.activityInfo.packageName);
            }else if(category.equals(communication)){    //社交APP禁用開始操作
                Log.e("check",info.activityInfo.packageName + "is added to communication");
                commuApps.add(info.activityInfo.packageName);
            }
        }
        return defaultApps;
    }


    //取得自訂禁用APP
    protected void startLoadingCustom(List<String> customs){
        Log.e("CATEGORY CUSTOM", "change status");
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        String communication = "communication";

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info : homeApps) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLogo(info.activityInfo.loadIcon(packageManager));
            appInfo.setPackageName(info.activityInfo.packageName);
            appInfo.setAppName((String) info.activityInfo.loadLabel(packageManager));
            String query_url = GOOGLE_URL + info.activityInfo.packageName + "&hl=en";
            String category = getCategory(query_url).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if(customs.contains(appInfo.getPackageName())){
                appInfo.setAppStatus(true);
                Log.e("check",appInfo.getPackageName() + "change to banned");
            }
            else if(category.equals(communication)){    //社交APP禁用開始操作
                Log.e("check",info.activityInfo.packageName + "is added to communication");
                commuApps.add(info.activityInfo.packageName);
            }
            else{
                appInfo.setAppStatus(false);
            }
            customAppsList.add(appInfo);
        }
        setAllowedAppInfos(customAppsList);
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
        dbBannedAppsHelper = new DBTotalHelper(this);
        db = dbBannedAppsHelper.getWritableDatabase();
    }

    public boolean checkIfUserExists() {
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
        cursor.moveToFirst();
        int isCustom = cursor.getInt(cursor.getColumnIndex(COL_CHECK));
        setIsCustom(isCustom);
        cursor.close();
        return isCustom;
    }

    private void insertDB(String user, String all, String defaults, String customs){
        ContentValues values = new ContentValues();
        values.put("_USER ",user);
        values.put("_ISCUSTOM", 0);
        values.put("_ALL", all);
        values.put("_DEFAULT", defaults);
        values.put("_CUSTOM", customs);
        db.insert(TABLE_APPS,null,values);
    }

    public void defaultAppsUpdateDB(String defaults){
        ContentValues values = new ContentValues();
        values.put("_DEFAULT", defaults);
        db.update(TABLE_APPS,values,COL_USER + " = " + "'" + userName + "'", null);
    }

    private ArrayList<String> getAllApps(){
        String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + userName + "'";
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String app = cursor.getString(cursor.getColumnIndex("_ALL"));
        ArrayList<String>  apps = gson.fromJson(app, type);
        cursor.close();
        return apps;
    }

    private ArrayList<String> getCustomApps(){
        String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + userName + "'";
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String app = cursor.getString(cursor.getColumnIndex("_CUSTOM"));
        ArrayList<String>  apps = gson.fromJson(app, type);
        cursor.close();
        return apps;
    }

    private ArrayList<String> getDefaultApps(){
        String Query = "Select * from " + TABLE_APPS + " where " + COL_USER + " = " + "'" + userName + "'";
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String app = cursor.getString(cursor.getColumnIndex("_DEFAULT"));
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
    public static void setAllowedApps(){
        if(getIsCustom() == 1){
            bannedApps = getCustomAllowedApps();
        }
        else{
            bannedApps = getDefaultAllowedApps();
        }

    }
    public static void setAllowedAppInfos(List<AppInfo> appList){
        bannedAppsList = appList;
    }
    public static void setAllowedCommuApps(List<String> Commuapp){
    }

    //取得
    public static List<String> getCustomAllowedApps(){
        return customApps;
    }
    public static List<String> getAllowedApps(){
        Log.e("UPDATE DATA CHECK", gson.toJson(bannedApps));
        return bannedApps;
    }
    public static List<String> getDefaultAllowedApps(){
        return realDefaultApps;
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

