package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class CheckFrontApp extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    Timer timer ;
    TimerTask timerTask ;
    String TAG = "Timers" ;
    private List<String> apps = new ArrayList<>();
    private final List<AppInfo> appsList = new ArrayList<>();

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override //一旦離開app，建立server服務
    public void onCreate () {
        Log. e ( TAG , "onCreate" );
        apps = LoadingApp.getAllowedApps();
    }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
        startTimer();
        super.onStartCommand(intent , flags , startId) ;
        return START_STICKY ;
    }

    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        super.onDestroy() ;
        this.stopSelf();
    }

    //建立計時器
    public void startTimer () {
        timer = new Timer() ;
        initializeTimerTask ();
        timer .schedule( timerTask , 10 , 5000 ) ; //每5秒執行一次task
    }

    //時間內，任務要做的任務
    public void initializeTimerTask () {
        timerTask = new TimerTask() {
            public void run () {
                String frontApp = getForegroundTask().replaceAll("\\s+","");
                if(frontApp.contains("camera")){
                    Log.e("check", "Detect App Press");
                    startActivity(new Intent(CheckFrontApp.this, PopupMessage.class));
                    cancel();
                }
                else if(!apps.contains(frontApp)){
                    if(frontApp.contains("launcher") || frontApp.contains("recent") || frontApp.contains("system") || frontApp.contains("category")){

                    }else {
                        Log.e("check", "Detect App Press");
                        startActivity(new Intent(CheckFrontApp.this, PopupMessage.class));
                        cancel();
                    }
                }
            }
        } ;
    }

    private String getForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant") UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        Log.e("adapter", "Current App in foreground is: " + currentApp + "Category is:"  + getCategory("https://play.google.com/store/apps/details?id="+currentApp));
        return currentApp;
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
}