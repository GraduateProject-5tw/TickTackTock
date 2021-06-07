package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class NotificationService extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private static final String YES_ACTION = "YES_ACTION";
    private static final String NO_ACTION = "NO_ACTION";
    Timer timer ;
    TimerTask timerTask ;
    String TAG = "Timers" ;
    int Your_X_SECS = 4 ;

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override //一旦離開app，建立server服務
    public void onCreate () { Log. e ( TAG , "onCreate" ) ; }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
          //設定計時器
        super .onStartCommand(intent , flags , startId) ;
        needPermissionForBlocking(getApplicationContext());
        startTimer();
        return START_STICKY ;
        /**final String str = "";
        Timer timer  =  new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int applaunched = 0,appclosed =0;
            int applaunches = 1;
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

                for ( ActivityManager.RunningAppProcessInfo appProcess: runningAppProcessInfo ) {
                    Log.d(appProcess.processName.toString(),"is running");
                    if (appProcess.processName.equals("com.google.android.youtube")) {
                        if ( appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND  /*isForeground(getApplicationContext(),runningAppProcessInfo.get(i).processName)*//**){
            /**                if (applaunched == 0 ){
                                applaunched = 1;
                                Log.d(str,"app has been launched");
                                AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext()); //創建訊息方塊
                                alert.setTitle("已被禁用");
                                alert.setCancelable(false);
                                alert.setMessage("此app在讀書期間已被禁用。若要使用，讀書計時則會中斷，請問是要使用？");
                                alert.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int i) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                });

                                alert.setNegativeButton("堅持使用", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int i) {
                                        // modify here
                                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(NotificationService.this);
                                        localBroadcastManager.sendBroadcast(new Intent(
                                                "com.action.close"));
                                    }
                                });
                                AlertDialog a = alert.show();//顯示訊息視窗
                                a.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            }
                            else if (appclosed == 1){
                                applaunches++;
                                appclosed = 0;
                                Log.d(String.valueOf(applaunches),"that was counter");
                            }
                        }
                        else {
                            appclosed = 1;
                            Log.d(str,"app has been closed");

                        }
                    }
                }
            }
        },2000,3000);

        return START_STICKY;
**/
    }

    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        super .onDestroy() ;
    }

    //建立計時器
    public void startTimer () {
        timer = new Timer() ;
        initializeTimerTask ();
        timer .schedule( timerTask , 10 , 5 * 1000 ) ; //每5秒執行一次task
    }


    //時間內，任務要做的任務
    public void initializeTimerTask () {
        long t0 = System.currentTimeMillis();
        timerTask = new TimerTask() {
            public void run () {
                String foreGroundApp = getForegroundTask().replaceAll("\\s","");
                    if(foreGroundApp.equalsIgnoreCase("com.google.android.youtube")){
                        createNotification();
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
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return currentApp;
    }

    public static boolean needPermissionForBlocking(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }


    //跳出通知
    public void createNotification () {
        timerTask.cancel();
        timer.cancel();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
        //for notification back to app
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, GeneralTimerActivity.class), 0);
        PendingIntent pendingIntent2 =
                PendingIntent.getActivity(this, 0, new Intent(this, TomatoClockActivity.class), 0);

        mBuilder.setFullScreenIntent(pendingIntent, true);
        mBuilder.setFullScreenIntent(pendingIntent2, true);

        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction(YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 0, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(0, "堅持使用", pendingIntentYes);

        //No intent
        Intent noReceive = new Intent();
        noReceive.setAction(NO_ACTION);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 0, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(0, "取消", pendingIntentNo);

        mBuilder.setContentTitle( "已禁用app" ) ;
        mBuilder.setContentText( "此app在讀書期間已被禁用。若要使用，讀書計時則會中斷，請問是要使用？" ) ;
        mBuilder.setTicker( "禁用" ) ;
        mBuilder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        mBuilder.setAutoCancel(false) ;
        mBuilder.setContentIntent(pendingIntent) ; //設置intent
        mBuilder.setColor(Color.RED) ;

        //點通知回到主畫面??
        Intent its = new Intent();
        its.setAction(Intent.ACTION_MAIN);
        its.addCategory(Intent.CATEGORY_HOME);
        its.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(its);
        //到這裡

        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
    }
}