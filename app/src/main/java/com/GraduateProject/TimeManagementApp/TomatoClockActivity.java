package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;

public class TomatoClockActivity extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private int futureInMillis;
    private final int cushion = 5000;
    private long beginTime;
    private long recordTime = 0;
    private boolean isCounting = false;
    private CountDownTimer study;
    private RingProgressBar spinner;
    private int mCurrentProgress;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml
        Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();
        startBtn = findViewById(R.id.tstart_btn);
        stopBtn = findViewById(R.id.tstop_btn);     //可用K停止
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        AnalogClockStyle timeButton = findViewById(R.id.clock); //clock image


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
                futureInMillis = 1500000;

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
            isCounting = true; //正在計時中
            timeButton.setEnabled(false); //計時中不得按時鐘

            //progress bar起始位置
            initVariable();
            spinner = findViewById(R.id.progressBarCircle);
            Calendar calendar = Calendar.getInstance();
            spinner.setTime(futureInMillis);
            spinner.setMinute(calendar.get(Calendar.MINUTE));
            spinner.setVisibility(View.VISIBLE);

            //先計時25分鐘
            study = new CountDownTimer(futureInMillis, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if(mCurrentProgress > 0) {
                        mCurrentProgress -= 1000;
                        spinner.setProgress(mCurrentProgress);
                    }
                }


                @Override
                public void onFinish() {  // 倒數結束時,會執行這裡
                    if (futureInMillis == 1500000 || futureInMillis == 1500000 + cushion) {
                        recordTime += (SystemClock.elapsedRealtime()-beginTime);//將讀完時間記錄下來
                        beginTime = SystemClock.elapsedRealtime();
                        isCounting = false;
                        AlertDialog.Builder startrest = new AlertDialog.Builder(TomatoClockActivity.this);
                        startrest.setMessage("開始休息");
                        startrest.setCancelable(true);  // disable click back button
                        startrest.show();
                        futureInMillis = 600000 + cushion;

                    } else {
                        beginTime=SystemClock.elapsedRealtime();    //重設開始的時間
                        isCounting = true;
                        AlertDialog.Builder startstudy = new AlertDialog.Builder(TomatoClockActivity.this);
                        startstudy.setMessage("開始讀書");
                        startstudy.setCancelable(true);  // disable click back button
                        startstudy.show();
                        futureInMillis = 1500000 + cushion;
                    }
                    //重畫新的progress bar
                    spinner.setInit(false);
                    spinner.setTime(futureInMillis);
                    spinner.setMinute(Calendar.getInstance().get(Calendar.MINUTE));
                    initVariable();
                    spinner.invalidate();
                    this.start();
                }
            };
            study.start();
        });
        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            recordTime+=(SystemClock.elapsedRealtime()-beginTime);
            isCounting = false;
            spinner.setInit(false);
            spinner.setVisibility(View.GONE);
            study.cancel();
            timeButton.setEnabled(true);

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

    private  void initVariable(){
        mCurrentProgress = futureInMillis ;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(TomatoClockActivity.this); //創建訊息方塊
        alert.setTitle("離開");
        alert.setMessage("確定要離開?");
        //按"是",則退出應用程式
        alert.setPositiveButton("是", (dialog, i) -> moveTaskToBack(true));

        //按"否",則不執行任何操作
        alert.setNegativeButton("否", (dialog, i) -> { });
        alert.show();//顯示訊息視窗


    }

    @Override
    public void onPause(){
        super.onPause();
        if(isCounting){
            isCounting = false;
            spinner.setInit(false);
            startService(new Intent(TomatoClockActivity.this, NotificationService.class));
            recordTime = 0;//若離開則歸0
            beginTime = 0;
            study.cancel();
            TomatoClockActivity.this.recreate();
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
}
