package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeneralTimerActivity extends AppCompatActivity implements LifecycleObserver {

    @SuppressLint("StaticFieldLeak")
    private static GeneralTimerActivity generalTimerActivity;
    private Chronometer chronometer; //計時器
    private Button startBtn;
    private Button stopBtn;
    private long recordTime;  //累計的時間
    private static boolean isCounting = false;
    private int Preset = 0; //讀書科目
    private String GeneralStudyCourse;//記錄的讀書科目



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generaltimer);  //指定對應的呈現程式碼在activity_stopwatch.xml
        chronometer = findViewById(R.id.time_view);   //用id尋找在介面佈局檔案中，時間呈現的區塊
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        generalTimerActivity = this;



        //計時按鈕的功能實作
        startBtn.setOnClickListener(v -> {
            chronometer.setBase(SystemClock.elapsedRealtime());  //計時器歸0
            chronometer.start();
            startBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            isCounting = true;
        });

        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            chronometer.stop();
            isCounting = false;
            recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            //跳出視窗
            final  String[] course={"國文","英文","數學","社會","自然","其他"};
            final EditText editText = new EditText(GeneralTimerActivity.this);//其他的文字輸入方塊
            AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
            builder.setCancelable(false);
            builder.setTitle("本次累積："+ Time);
            builder.setSingleChoiceItems(course, Preset, (dialog, which) -> Preset = which);
            builder.setPositiveButton("確認", (dialog, which) -> {
                if (Preset ==5) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(GeneralTimerActivity.this);
                            alert.setCancelable(false);
                            alert.setTitle("輸入讀書科目");
                            alert.setView(editText);
                            alert.setPositiveButton("確定", (dialogInterface, i) -> GeneralStudyCourse = editText.getText().toString());
                    alert.show();
                        }
                else{
                    GeneralStudyCourse= course[Preset];
                }
            });
            builder.show();
            recordTime = 0;
            chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0
        });

        tomato_btn.setEnabled(true);
        tomato_btn.setBackgroundColor(-1); //白色
        tomato_btn.setOnClickListener(view ->
        {
            if(isCounting){
                AlertDialog.Builder change = new AlertDialog.Builder(GeneralTimerActivity.this);
                change.setTitle("番茄時鐘");
                change.setMessage("是否要換成番茄時鐘?");
                chronometer.stop();
                isCounting = false;
                recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
                String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
                //是的話跳轉到tomato
                change.setPositiveButton("是", (dialog, i) -> {
                    //tomato的切換頁面
                    Intent intent = new Intent();
                    intent.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                    //顯示紀錄時間
                    AlertDialog.Builder record = new AlertDialog.Builder(GeneralTimerActivity.this); //創建訊息方塊
                    record.setTitle("紀錄時間");
                    record.setMessage("讀書時間：\n\n"+Time);
                    record.setPositiveButton("ok", (dialog1, which) -> {
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        recordTime=0;
                        startActivity(intent);
                    });
                    record.show();
                });
                //否的話留在一般
                change.setNegativeButton("否", (dialog, i) -> {
                    startBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.VISIBLE);
                    double temp = Double.parseDouble(chronometer.getText().toString().split(":")[1]) * 1000;
                    chronometer.setBase((long) (SystemClock.elapsedRealtime() - temp));
                    chronometer.start();
                    isCounting = true;
                });
                change.show();
            }
            else{
                //tomato的切換頁面
                Intent intent = new Intent();
                intent.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                isCounting = false;
                chronometer.setBase(SystemClock.elapsedRealtime());
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                recordTime=0;
                startActivity(intent);

            }
        });

        //general的禁按
        general_btn.setEnabled(false);
        general_btn.setBackgroundColor(-3355444);
    }

    @Override
    public void onBackPressed() {  //當按back按紐時
        AlertDialog.Builder alert = new AlertDialog.Builder(GeneralTimerActivity.this); //創建訊息方塊
        alert.setTitle("離開");
        alert.setMessage("確定要離開?");
        //按"是",則退出應用程式
        alert.setPositiveButton("是", (dialog, i) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        });
        //按"否",則不執行任何操作
        alert.setNegativeButton("否", (dialog, i) -> {
        });
        alert.show();//顯示訊息視窗
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isCounting){
            startService(new Intent(GeneralTimerActivity.this, CheckFrontApp.class));
        }
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

    public static GeneralTimerActivity getActivity(){
        return generalTimerActivity;
    }

    public static boolean getIsCounting(){
        return isCounting;
    }
}
