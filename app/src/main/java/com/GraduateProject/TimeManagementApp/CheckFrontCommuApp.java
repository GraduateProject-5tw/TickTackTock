package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CheckFrontCommuApp extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    private String TAG = "Timers社交" ;
    private List<String> commuapps = new ArrayList<>();
    private ScheduledThreadPoolExecutor executor;
    private int i=0;
    private CountDownTimer CommuTimer = new CountDownTimer(600000, 10000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Countdown", "開始倒數計時10分鐘");
            String frontCommuApp = getForegroundTask().replaceAll("\\s+","");
            synchronized (DetectFrontCommuApp) {
                if (!commuapps.contains(frontCommuApp)){
                    Log.e("Countdown", "restart countdown");
                    i = 0;
                    DetectFrontCommuApp.notifyAll();
                    CommuTimer.cancel();
                }
            }
        }

        @Override
        public void onFinish() {
            Log.e("Timer", "finish");
            startService(new Intent(CheckFrontCommuApp.this,DialogShowCommu.class));
            executor.shutdown();
            stopSelf();
        }
    };

    private final Thread DetectFrontCommuApp = new Thread(new Runnable() {
        @Override
        public void run() {
            String frontCommuApp = getForegroundTask().replaceAll("\\s+","");
            if(commuapps.contains(frontCommuApp)){
                Log.e("checkCommu", "Detect Communication App Press");
                try {
                    if(i==0){
                        CommuTimer.start();
                        i+=1;
                    }else{
                        CommuTimer.cancel();
                    }
                    synchronized (DetectFrontCommuApp) {
                        if (commuapps.contains(frontCommuApp))
                            DetectFrontCommuApp.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override //一旦離開app，建立server服務
    public void onCreate () {
        Log. e ( TAG , "onCreate" );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
        commuapps = LoadingApp.getAllowedCommuApps();
        long period = 1000;
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(DetectFrontCommuApp, 0, period, TimeUnit.MILLISECONDS);
        super.onStartCommand(intent , flags , startId) ;
        return START_STICKY ;
    }

    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        super.onDestroy() ;
        executor.shutdown();
        this.stopSelf();
    }

    private String getForegroundTask() {
        String currentApp = "NULL";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
            if(tasks.isEmpty() || tasks == null){
            }
            currentApp = tasks.get(0).processName;
        }
        return currentApp;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("偵測中")
                .setContentText("正在偵測使用中的社交APP")

                // this is important, otherwise the notification will show the way
                // you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}