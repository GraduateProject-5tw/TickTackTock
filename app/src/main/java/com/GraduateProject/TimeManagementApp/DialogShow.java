package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DialogShow extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    private String TAG = "Timers" ;
    WindowBanned windowBanned;

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override //一旦離開app，建立server服務
    public void onCreate () {
        Log.e(TAG, "onCreate");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }
    }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
        AlertDialog.Builder banned = new AlertDialog.Builder(getApplicationContext());
        banned.setTitle("確 認");
        banned.setMessage("現在是讀書時間，確定要使用該APP嗎？\n\n若使用，計時將會停止。");
        banned.setCancelable(false);
        banned.setPositiveButton("確定使用", (dialog, which) -> {
            Log.v("shuffTest", "Pressed YES");
            if(GeneralTimerActivity.getIsCounting()){
                GeneralTimerActivity.getActivity().finishCounting();
            } else{
                TomatoClockActivity.getTomatoClockActivity().finishCounting();
            }
        });
        banned.setNegativeButton("放棄使用", ((dialog, which) -> {
            Log.v("shuffTest", "Pressed NO");
            Intent intentHome;
            if(GeneralTimerActivity.getIsCounting()){
                intentHome = new Intent(getApplicationContext(), GeneralTimerActivity.class);
            } else{
                intentHome = new Intent(getApplicationContext(), TomatoClockActivity.class);
            }
            intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intentHome);
        }));
        AlertDialog alertDialog = banned.create();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if(!Settings.canDrawOverlays(getApplicationContext())){
                    Log.e("OVERLAY","overlay error");
                }
                else{
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }
            }, 500);
        }
        else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        alertDialog.show();
        //windowBanned = new WindowBanned(getApplicationContext());
        //windowBanned.open();
        super.onStartCommand(intent , flags , startId) ;
        return START_STICKY ;
    }

    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        super.onDestroy() ;
        this.stopSelf();
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
                .setContentText("正在偵測使用中的APP")

                // this is important, otherwise the notification will show the way
                // you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}