package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import java.util.concurrent.TimeUnit;

public class GeneralTimerActivity extends AppCompatActivity implements LifecycleObserver {

    private Chronometer chronometer; //計時器
    private Button startBtn;
    private Button stopBtn;
    private long recordTime;  //累計的時間

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generaltimer);  //指定對應的呈現程式碼在activity_stopwatch.xml
        chronometer = findViewById(R.id.time_view);   //用id尋找在介面佈局檔案中，時間呈現的區塊
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);

        //計時按鈕的功能實作
        startBtn.setOnClickListener(v -> {
            chronometer.setBase(SystemClock.elapsedRealtime());  //計時器歸0
            chronometer.start();
            startBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);

        });

        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            chronometer.stop();
            recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            //跳出視窗
            AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
            AlertDialog dialog = builder.create();
            dialog.setMessage("本次累積：\n\n"+ Time);
            dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
            dialog.show();
            recordTime = 0;
            chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
        });

        tomato_btn.setEnabled(true);
        tomato_btn.setBackgroundColor(-1); //白色
        //tomato的切換頁面
        tomato_btn.setOnClickListener(view -> startActivity(new Intent(GeneralTimerActivity.this, TomatoClockActivity.class)));

        //general的禁按
        general_btn.setEnabled(false);
        general_btn.setBackgroundColor(-3355444);
    }

    @Override
    public void onBackPressed() {  //當按back按紐時
        Intent notification = new Intent( this, NotificationService.class );

        AlertDialog.Builder alert = new AlertDialog.Builder(GeneralTimerActivity.this); //創建訊息方塊
        alert.setTitle("離開");
        alert.setMessage("確定要離開?");
        alert.setPositiveButton("是", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
            public void onClick(DialogInterface dialog, int i) {
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                //跳出app立刻將時間歸零
                if(recordTime>0) {
                    startService(new Intent(GeneralTimerActivity.this, NotificationService.class));
                    recordTime = 0;
                    chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
                }

            }
        });
        alert.setNegativeButton("否", new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        alert.show();//顯示訊息視窗



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

        return(hours +
                " 小時 " +
                minutes +
                " 分 " +
                seconds +
                " 秒");
    }
}