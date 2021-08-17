package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CheckFrontApp extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    private String TAG = "Timers" ;
    private Timer timer ;
    private TimerTask timerTask ;
    private List<String> apps = new ArrayList<>();
    private ScheduledThreadPoolExecutor executor;
    private String frontApp;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private NotificationManager mNotificationManager ;
    private NotificationCompat.Builder mBuilder;
    private boolean normal = true;


    private Thread DetectFrontApp = new Thread(new Runnable() {
        @Override
        public void run() {
            frontApp = getForegroundTask().replaceAll("\\s+","");

            if(apps.contains(frontApp)){
                Log.e("check", "Detect App Press");
                executor.shutdown();
                createNotification();
                /**Intent intent = new Intent("com.GraduateProject.TimeManagementApp.FOO");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("FrontApp", frontApp);
                startActivity(intent);
                /**try {
                    createNotification();
                    executor.wait(60000);
                    executor.notify();
                    //vibration();
                    String newFront = getForegroundTask().replaceAll("\\s+","");
                    Log.e("check", "new front app is :"+ newFront);
                    if(frontApp == newFront){
                        Log.e("check","still in app");
                        //GeneralTimerActivity.getActivity().finishCounting();
                        sendMessage();
                    }
                    else{
                        executor = new ScheduledThreadPoolExecutor(1);
                        executor.scheduleAtFixedRate(DetectFrontApp, 0, 1000, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }**/
            }

            //else if()
        }
    });

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override //一旦離開app，建立server服務
    public void onCreate () {
        Log. e ( TAG , "onCreate" );
        mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
    }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
        apps = LoadingApp.getAllowedApps();
        long period = 1000;
        executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(DetectFrontApp, 0, period, TimeUnit.MILLISECONDS);
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
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant") UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if(usm == null){
                Log.e("CHECK", "high version, not get");
            }
            else if (appList != null && appList.size() > 0) {
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

    //跳出通知
    public void createNotification(){
        Intent fullScreenIntent = new Intent(this, PopupMessage.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notifyIntent = new Intent(this, PopupMessage.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentTitle( "讀書期間APP禁用" ) ;
        mBuilder.setContentText( "通知消失後前次紀錄將作廢，請點選通知進行選擇。" ) ;
        mBuilder.setContentIntent(notifyPendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_lock) ;
        mBuilder.setAutoCancel(false);
        mBuilder.setColor(Color.RED) ;
        mBuilder.setTimeoutAfter(60000);
        /**if(GeneralTimerActivity.getIsCounting()){
            mBuilder.addAction(R.drawable.ic_lock_open, "繼續使用", other);
            mBuilder.addAction(R.drawable.ic_lock, "放棄使用", general);
        } else{
            mBuilder.addAction(R.drawable.ic_lock_open, "繼續使用", other);
            mBuilder.addAction(R.drawable.ic_lock, "放棄使用", tomato);
        }**/
        mBuilder.setCategory(NotificationCompat.CATEGORY_CALL);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setFullScreenIntent(fullScreenPendingIntent, true);

        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager.IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }

        assert mNotificationManager != null;

        int id = (int)System.currentTimeMillis ();
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(id, notification);
        startForeground(id, notification);

        new Handler(Looper.getMainLooper()).postDelayed (() -> {
            mNotificationManager.cancel(id);
        }, 60000);
    }

    public Vibrator vibration() {

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        long[] pattern = { 1000, 1000, 2000, 2000};

        v.vibrate(pattern, 1);
        return v;

    }

    // Send an Intent with an action named "my-event".
    private void sendMessage() {
        Intent intent = new Intent("my-event");
        // add data
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}