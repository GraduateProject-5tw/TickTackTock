package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class TomatoClockActivity extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private Chronometer chronometer;
    private long recordTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml
        startBtn = findViewById(R.id.tstart_btn);
        stopBtn = findViewById(R.id.tstop_btn);
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        AnalogClockStyle timeButton = findViewById(R.id.clock); //clock image

        Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();

        //當按下時鐘
        timeButton.setOnClickListener(v -> {
            AlertDialog.Builder timeConfirm = new AlertDialog.Builder(TomatoClockActivity.this);
            timeConfirm.setTitle("時間配置確認");
            timeConfirm.setMessage("\n\n讀書時間：25分鐘\n\n休息時間：10分鐘");
            timeConfirm.setIcon(android.R.drawable.ic_dialog_info);
            timeConfirm.setCancelable(false);

            //設定視窗按鈕的功能
            timeConfirm.setPositiveButton("OK", (dialog, which) -> {
                //顯示設定完成提醒
                Toast.makeText(TomatoClockActivity.this, "時間設定完成", Toast.LENGTH_SHORT).show();

                //出現開始計時按鈕
                startBtn.setVisibility(View.VISIBLE);
            });

            timeConfirm.show();
        });
        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            chronometer.stop();
            recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            //跳出視窗
            AlertDialog.Builder builder = new AlertDialog.Builder(TomatoClockActivity.this);
            AlertDialog dialog = builder.create();
            dialog.setMessage("本次累積：\n\n"+ Time);
            dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
            dialog.show();
            recordTime = 0;
            chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
        });



        general_btn.setEnabled(true);
        general_btn.setBackgroundColor(-1); //白色
        //general的切換頁面
        Intent intent = new Intent();
        intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
        general_btn.setOnClickListener(v ->
                startActivity(intent));

        //general的禁按
        tomato_btn.setEnabled(false);
        tomato_btn.setBackgroundColor(-3355444); //淺灰色
    }
    @Override
    protected void onPause() {  //當APP跑到背景時
        startService( new Intent( this, Alert.class )) ;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        super.onPause();
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

        String sb = hours +
                " 小時 " +
                minutes +
                " 分 " +
                seconds +
                " 秒";
        return(sb);
    }



}
