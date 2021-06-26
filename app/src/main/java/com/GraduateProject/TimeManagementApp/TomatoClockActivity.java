package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
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
    private int studyInMillis;
    private int stopInMillis;
    private long beginTime;
    private long recordTime = 0;
    private boolean isCounting = false;
    private MyCountdownTimer study;
    private RingProgressBar spinnerStudy;
    private int mCurrentProgress;
    final String[] studytime = new String[]{"15","20","25","30","35","40","45","50","55","60"};
    final String[] resttime = new String[]{"0","5","10","15","20","25","30","35","40","45","50","55","60"};

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
            AlertDialog.Builder StoptimeConfirm = new AlertDialog.Builder(TomatoClockActivity.this);
            AlertDialog.Builder remind = new AlertDialog.Builder(TomatoClockActivity.this);
            timeConfirm.setTitle("讀書時間設置:(分鐘)");
            timeConfirm.setIcon(android.R.drawable.ic_dialog_info);
            StoptimeConfirm.setIcon(android.R.drawable.ic_dialog_info);
            remind.setIcon(android.R.drawable.ic_popup_reminder);
            StoptimeConfirm.setCancelable(false);
            timeConfirm.setCancelable(false);
            //設定單選列表
            timeConfirm.setSingleChoiceItems(studytime, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Toast.makeText(TomatoClockActivity.this, studytime[which], Toast.LENGTH_SHORT).show();
                    int i = Integer.valueOf(studytime[which]);
                    studyInMillis = i*60000;
                }
            });
            //設定取消按鈕並且設定響應事件
            timeConfirm.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    //取消按鈕響應事件
                    dialog.dismiss();//結束對話框
                }
            });
            timeConfirm.setPositiveButton("下一步→", (dialog, which) -> {
                StoptimeConfirm.setTitle("休息時間設置:(分鐘)");
                StoptimeConfirm.setSingleChoiceItems(resttime, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Toast.makeText(TomatoClockActivity.this, resttime[which], Toast.LENGTH_SHORT).show();
                        int j = Integer.valueOf(resttime[which]);
                        stopInMillis = j*60000;
                    }
                });
                StoptimeConfirm.setPositiveButton("確定", (ddialog, wwhich) -> {
                    if(stopInMillis>studyInMillis){
                        remind.setTitle("請重新設置");
                        remind.setMessage("尚未點選\n\nor\n\n讀書時間大於休息時間");
                        remind.show();
                        dialog.dismiss();
                    }else {
                        //顯示設定完成提醒
                        Toast.makeText(TomatoClockActivity.this, "時間設定完成", Toast.LENGTH_SHORT).show();
                        futureInMillis = studyInMillis;
                        //出現開始計時按鈕
                        startBtn.setVisibility(View.VISIBLE);
                    }
                });
                StoptimeConfirm.show();
            });
            timeConfirm.show();
        });

        general_btn.setEnabled(true);
        general_btn.setBackgroundColor(-1); //白色
        general_btn.setOnClickListener(v -> {
            if(isCounting){
                AlertDialog.Builder change = new AlertDialog.Builder(TomatoClockActivity.this);
                change.setTitle("一般計時");
                change.setMessage("是否要換成一般計時?");
                isCounting = false;
                recordTime += (SystemClock.elapsedRealtime()-beginTime);//將讀完時間記錄下來
                String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
                change.setPositiveButton("是", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int i) {
                        //general的切換頁面
                        Intent intent = new Intent();
                        intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                        //顯示紀錄時間
                        AlertDialog.Builder record = new AlertDialog.Builder(TomatoClockActivity.this); //創建訊息方塊
                        record.setTitle("紀錄時間");
                        record.setMessage("讀書時間：\n\n"+Time);
                        record.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                spinnerStudy.setIsNewProgress(false);
                                spinnerStudy.setIsNewProgress(false);
                                recordTime = 0;
                                beginTime = 0;
                                study.cancel();
                                TomatoClockActivity.this.finish();
                                startActivity(intent);
                            }
                        });
                        record.show();
                    }
                });
                change.setNegativeButton("否", new DialogInterface.OnClickListener() { //否的話留在一般
                    public void onClick(DialogInterface dialog, int i) {
                        stopBtn.setVisibility(View.VISIBLE);
                        beginTime=SystemClock.elapsedRealtime();  //抓取當下時間
                        isCounting = true; //正在計時中
                        timeButton.setEnabled(false); //計時中不得按時鐘
                    }
                });
                change.show();
            }
            else{
                //general的切換頁面
                Intent intent = new Intent();
                intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                isCounting = false;
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                startActivity(intent);
            }
        });

        //tomato的禁按
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
            spinnerStudy = findViewById(R.id.progressBarStudy);
            Calendar calendar = Calendar.getInstance();
            spinnerStudy.setTime(futureInMillis);
            spinnerStudy.setMinute(calendar.get(Calendar.MINUTE));
            spinnerStudy.setVisibility(View.VISIBLE);

            //讓讀書progress bar動
            study = new MyCountdownTimer(futureInMillis, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if(mCurrentProgress > 0) {
                        mCurrentProgress -= 1000;
                        spinnerStudy.setProgress(mCurrentProgress);
                    }
                }


                @Override
                public void onFinish() {  // 倒數結束時,會執行這裡
                    if (futureInMillis == studyInMillis) {
                        recordTime += (SystemClock.elapsedRealtime()-beginTime);//將讀完時間記錄下來
                        isCounting = false;
                        spinnerStudy.setVisibility(View.GONE);
                        AlertDialog.Builder startrest = new AlertDialog.Builder(TomatoClockActivity.this);
                        startrest.setMessage("開始休息");
                        startrest.setCancelable(true);  // disable click back button
                        startrest.setOnCancelListener(dialog -> {
                            //重畫新的progress bar
                            vibration().cancel();
                            futureInMillis = stopInMillis;
                            beginTime = SystemClock.elapsedRealtime();
                            initVariable();
                            spinnerStudy.setIsNewProgress(true);
                            spinnerStudy.setProgress(Calendar.getInstance().get(Calendar.MINUTE), futureInMillis);
                            spinnerStudy.setVisibility(View.VISIBLE);
                            this.setMillisInFuture(futureInMillis);
                            this.start();
                        });
                        startrest.show();
                        vibration();
                    }else{
                        isCounting = true;
                        spinnerStudy.setVisibility(View.GONE);
                        AlertDialog.Builder startstudy = new AlertDialog.Builder(TomatoClockActivity.this);
                        startstudy.setMessage("開始讀書");
                        startstudy.setCancelable(true);  // disable click back button
                        startstudy.setOnCancelListener(dialog -> {
                            //重畫新的progress bar
                            vibration().cancel();
                            futureInMillis = studyInMillis;
                            beginTime = SystemClock.elapsedRealtime();
                            initVariable();
                            spinnerStudy.setIsNewProgress(true);
                            spinnerStudy.setProgress(Calendar.getInstance().get(Calendar.MINUTE), futureInMillis);
                            spinnerStudy.setVisibility(View.VISIBLE);
                            this.setMillisInFuture(futureInMillis);
                            this.start();
                        });
                        startstudy.show();
                        vibration();
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
            if(isCounting){
                study.cancel();
                spinnerStudy.setVisibility(View.GONE);
                isCounting = false;

            }

            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            //跳出視窗
            AlertDialog.Builder builder = new AlertDialog.Builder(TomatoClockActivity.this);
            AlertDialog dialog = builder.create();
            dialog.setMessage("本次累積：\n\n"+ Time);
            dialog.setCanceledOnTouchOutside(true); //允許按對話框外部來關閉視窗
            dialog.setOnCancelListener(dialog1 ->
                    TomatoClockActivity.this.recreate()
            );
            dialog.show();

            recordTime = 0;
        });
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
            spinnerStudy.setVisibility(View.GONE);
            study.cancel();
            startService(new Intent(TomatoClockActivity.this, NotificationService.class));
            recordTime = 0;//若離開則歸0
            beginTime = 0;
            TomatoClockActivity.this.recreate();
        }
    }

    private  void initVariable(){
        mCurrentProgress = futureInMillis;
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

    public Vibrator vibration() {

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        long[] pattern = { 0, 3000, 3000 };

        v.vibrate(pattern, 0);
        return v;

    }
}
