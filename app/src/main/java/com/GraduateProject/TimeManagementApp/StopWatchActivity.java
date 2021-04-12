package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class StopWatchActivity extends AppCompatActivity {

    private Chronometer chronometer; //計時器
    private Button startBtn, stopBtn;
    private long recordTime;  //累計的時間
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);  //指定對應的呈現程式碼在activity_stopwatch.xml
        chronometer = findViewById(R.id.time_view);   //用id尋找在介面佈局檔案中，時間呈現的區塊
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);
        setContentView(R.layout. activity_main ) ;

        //計時按鈕的功能實作
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());  //計時器歸0
                chronometer.start();
                startBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.VISIBLE);
            }
        });

        //停止按鈕的功能實作
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
                recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
                String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                //跳出視窗
                AlertDialog.Builder builder = new AlertDialog.Builder(StopWatchActivity.this);
                AlertDialog dialog = builder.create();
                dialog.setMessage("本次累積："+ Time);
                dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
                dialog.show();
                recordTime = 0;
                Time = "";
                chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
            }
        });
    }

    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append(" 小時 ");
        sb.append(minutes);
        sb.append(" 分 ");
        sb.append(seconds);
        sb.append(" 秒");

        return(sb.toString());
    }

    @Override
    protected void onStop () {
        super .onStop() ;
        startService( new Intent( this, NotificationService. class )) ;
    }
    public void closeApp (View view) {
        finish() ;
    }
}