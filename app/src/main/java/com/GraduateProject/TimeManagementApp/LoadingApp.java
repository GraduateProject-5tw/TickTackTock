package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadingApp extends AppCompatActivity {

    private static List<String> apps = new ArrayList<>();
    private static final List<AppInfo> appsList = new ArrayList<>();
    //private final String[] bannedCat = {"artdesign", "shopping", "games"};
    private final String[] bannedCat = {"artdesign", "business", "communication", "education", "photography", "productivity", "tools", "error", "musicaudio"};
    private final List<String> banned = Arrays.asList(bannedCat);
    private final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_loadingapp);

        //先列出所有禁用App
        Thread loadingThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    apps = startLoading();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    Intent main = new Intent(LoadingApp.this, GeneralTimerActivity.class);
                    startActivity(main);
                    finish();
                }
            }
        };
        loadingThread.start();
    }

    protected List<String> startLoading(){
        Log.e("CATEGORY", "start checking");
        String category;
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
            Log.e("CATEGORY", category);
            if(banned.contains(category)){
                appInfo.setAppStatus(true);
                Log.e("check",appInfo.getPackageName() + "is added");
                apps.add(appInfo.getPackageName());
            }
            appsList.add(appInfo);

        }

        return apps;
    }


    private String getCategory(String query_url) {

        try {
            Document doc = Jsoup.connect(query_url).get();

            if (doc != null) {
                Element link = doc.select("a[itemprop=genre]").first();
                return link.text();
            } else{
                Log.e("category", "null doc");
                return "null doc";
            }
        } catch (Exception e) {
            Log.e("DOc", e.toString());
            return "error";
        }
    }

    public static List<String> getAllowedApps(){
        return apps;
    }

    public static List<AppInfo> getAllowedAppInfos(){
        return appsList;
    }
}

