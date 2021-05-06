package com.GraduateProject.TimeManagementApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {    //server是一個在背景執行的服務，透過bindservice create、startservice start

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    Timer timer ;
    TimerTask timerTask ;
    String TAG = "Timers" ;
    int Your_X_SECS = 4 ;

    @Override
    public IBinder onBind (Intent arg0) {  //將app綁定server服務
        return null;
    }

    @Override
    public void onCreate () {   //一旦離開app，建立server服務
        Log. e ( TAG , "onCreate" ) ;
    }

    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {  //建立以後，啟動server服務
        Log. e ( TAG , "onStartCommand" ) ;
        startTimer() ;  //設定計時器
        super .onStartCommand(intent , flags , startId) ;
        return START_STICKY ;
    }

    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        super .onDestroy() ;
    }

    //用 handler 處理計時器任務
    final Handler handler = new Handler() ;

    //建立計時器
    public void startTimer () {
        timer = new Timer() ;
        initializeTimerTask ();
        timer .schedule( timerTask , 10 , Your_X_SECS * 5000 ) ; //每5秒執行一次task
    }

    //時間內，任務要做的任務
    public void initializeTimerTask () {
        long t0 = System.currentTimeMillis();
        timerTask = new TimerTask() {
            public void run () {
                if (System.currentTimeMillis() - t0 > 3 * 1000) {  //若時間大於3秒
                    cancel();   //關閉任務
                } else {
                    createNotification ();  //跳出通知
                }
            }
        } ;
    }

    //跳出通知
    private void createNotification () {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
        mBuilder.setContentTitle( "時間通知" ) ;
        mBuilder.setContentText( "停止計時，前次紀錄作廢" ) ;
        mBuilder.setTicker( "停止計時" ) ;
        mBuilder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        mBuilder.setAutoCancel( true ) ; //

        //點通知回到主畫面??
        Intent it = new Intent();
        it.setAction(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_HOME);
        startActivity(it);
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