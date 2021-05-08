package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.os.SystemClock;
import android.view.View;

import android.webkit.WebView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class TomatoClockActivity extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private int futureInMillis = 1500000;
    private int cushion = 5000;
    private ProgressBar spinner;
    private long beginTime;
    private long recordTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml
        startBtn = findViewById(R.id.tstart_btn);
        stopBtn = findViewById(R.id.tstop_btn);     //可用K停止
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        AnalogClockStyle timeButton = findViewById(R.id.clock); //clock image
        spinner = (ProgressBar) findViewById(R.id.progressBarCircle);


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

        //計時按鈕的功能實作
        startBtn.setOnClickListener(v -> {
            stopBtn.setVisibility(View.VISIBLE);
            beginTime=SystemClock.elapsedRealtime();  //抓取當下時間


            //先計時25分鐘
            CountDownTimer study = new CountDownTimer(futureInMillis, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                }


                @Override
                public void onFinish() {  // 倒數結束時,會執行這裡
                    if (futureInMillis == 1500000 || futureInMillis == 1500000 + cushion) {
                        recordTime += (SystemClock.elapsedRealtime()-beginTime);//將讀完時間記錄下來
                        AlertDialog.Builder startrest = new AlertDialog.Builder(TomatoClockActivity.this);
                        startrest.setMessage("開始休息");
                        startrest.setCancelable(true);  // disable click back button
                        startrest.show();
                        futureInMillis = 600000 + cushion;
                        this.start();

                    } else {
                        beginTime=SystemClock.elapsedRealtime();    //重設開始的時間
                        AlertDialog.Builder startstudy = new AlertDialog.Builder(TomatoClockActivity.this);
                        startstudy.setMessage("開始讀書");
                        startstudy.setCancelable(true);  // disable click back button
                        startstudy.show();
                        futureInMillis = 1500000 + cushion;
                        this.start();

                    }
                }
            };
            study.start();
        });
        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            recordTime+=(SystemClock.elapsedRealtime()-beginTime);

            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            //跳出視窗
            AlertDialog.Builder builder = new AlertDialog.Builder(TomatoClockActivity.this);
            AlertDialog dialog = builder.create();
            dialog.setMessage("本次累積：\n\n"+ Time);
            dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
            dialog.show();
            recordTime = 0;


        });


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(TomatoClockActivity.this); //創建訊息方塊
        alert.setTitle("離開");
        alert.setMessage("確定要離開?");
        alert.setPositiveButton("是", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
            public void onClick(DialogInterface dialog, int i) {
                System.exit(0);//關閉activity
                moveTaskToBack(true);
                recordTime=0;//若離開則歸零
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

        String sb = hours +
                " 小時 " +
                minutes +
                " 分 " +
                seconds +
                " 秒";
        return(sb);
    }

}
