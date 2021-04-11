package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

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
                recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                //跳出視窗
                AlertDialog.Builder builder = new AlertDialog.Builder(StopWatchActivity.this);
                AlertDialog dialog = builder.create();
                dialog.setMessage("本次累積"+ recordTime);
                dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
                recordTime = 0;
                chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
            }
        });
    }
}