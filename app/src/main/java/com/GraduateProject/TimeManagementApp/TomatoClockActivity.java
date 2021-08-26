package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TomatoClockActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static TomatoClockActivity tomatoClockActivity;
    private Button startBtn;
    private Button stopBtn;
    private int futureInMillis;
    //private int studyInMillis;
    private int studyInMillis=1500000;
    //private int stopInMillis;
    private int stopInMillis=600000;
    private long beginTime;
    private long recordTime = 0;
    private static boolean isCounting = false;
    private MyCountdownTimer study;
    private RingProgressBar spinnerStudy;
    private int mCurrentProgress;
    final String[] studytime = new String[]{"15","20","25","30","35","40","45","50","55","60"};
    final String[] resttime = new String[]{"0","5","10","15","20","25","30","35","40","45","50","55","60"};
    private int Preset = 0; //讀書科目
    private String   TomatoStudyCourse;//記錄的讀書科目
    private AnalogClockStyle timeButton;
    private AppBarConfiguration mAppBarConfiguration;
    private DBTimeBlockHelper DBHelper = null;
    private String startTime;
    private String date;
    private String stopTime;
    private String totalTime;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tomatoClockActivity = this;
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml
        showDialogStart();
        Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();
        startBtn = findViewById(R.id.tstart_btn);
        stopBtn = findViewById(R.id.tstop_btn);     //可用K停止
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        timeButton = findViewById(R.id.clock); //clock image


        //目錄相關
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.nav_open, R.string.nav_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.todolist, R.id.studytime,R.id.setting).setDrawerLayout(drawer).build();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(navigationView);
            }
        });
        //toolbar.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) drawer);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    // launch general timer
                    case R.id.nav_home:
                        break;
                    // launch to do list
                    case R.id.todolist:
                        Log.e("Menu", "to do list");
                        startActivity(new Intent(TomatoClockActivity.this, TodayToDoListActivity.class));
                        break;
                    // launch time block
                    case R.id.studytime:
                        startActivity(new Intent(TomatoClockActivity.this, TimeBlockerActivity.class));
                        break;
                    // launch settings activity
                    case R.id.setting:
                        startActivity(new Intent(TomatoClockActivity.this, SettingsActivity.class));
                        break;
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        navigationView.setCheckedItem(R.id.nav_home);


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
            timeConfirm.setSingleChoiceItems(studytime, -1, (dialog, which) -> {
                // TODO Auto-generated method stub
               int i = Integer.parseInt(studytime[which]);
                studyInMillis = i*60000;
            });
            //設定取消按鈕並且設定響應事件
            timeConfirm.setNegativeButton("取消", (dialog, which) -> {
                // TODO Auto-generated method stub
                //取消按鈕響應事件
                dialog.dismiss();//結束對話框
            });
            timeConfirm.setPositiveButton("下一步→", (dialog, which) -> {
                StoptimeConfirm.setTitle("休息時間設置:(分鐘)");
                StoptimeConfirm.setSingleChoiceItems(resttime, -1, (dialog1, which1) -> {
                    // TODO Auto-generated method stub
                    Toast.makeText(TomatoClockActivity.this, resttime[which1], Toast.LENGTH_SHORT).show();
                    int j = Integer.parseInt(resttime[which1]);
                    stopInMillis = j*60000;
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
                change.setPositiveButton("是", (dialog, i) -> {
                    //general的切換頁面
                    Intent intent = new Intent();
                    intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                    //顯示紀錄時間
                    AlertDialog.Builder record = new AlertDialog.Builder(TomatoClockActivity.this); //創建訊息方塊
                    record.setTitle("紀錄時間");
                    record.setMessage("讀書時間：\n\n"+Time);
                    record.setPositiveButton("ok", (dialog12, which) -> {
                        spinnerStudy.setIsNewProgress(false);
                        spinnerStudy.setIsNewProgress(false);
                        recordTime = 0;
                        beginTime = 0;
                        study.cancel();
                        TomatoClockActivity.this.finish();
                        startActivity(intent);
                    });
                    record.show();
                });
                //否的話留在一般
                change.setNegativeButton("否", (dialog, i) -> {
                    stopBtn.setVisibility(View.VISIBLE);
                    beginTime=SystemClock.elapsedRealtime();  //抓取當下時間
                    isCounting = true; //正在計時中
                    timeButton.setEnabled(false); //計時中不得按時鐘
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
            startTime = getTime();
            date = getDay();
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
                    }
                    vibration();

                }
            };

            study.start();
        });


        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            stopTime = getTime();
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            timeButton.setEnabled(true);
            recordTime+=(SystemClock.elapsedRealtime()-beginTime);
            if(isCounting){
                study.cancel();
                spinnerStudy.setVisibility(View.GONE);
                isCounting = false;
            }
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            totalTime=getTotalTime(recordTime);
        //跳出視窗
            final  String[] course={"國文","英文","數學","社會","自然","其他"};
            final EditText editText = new EditText(TomatoClockActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(TomatoClockActivity.this);
            builder.setCancelable(false);
            builder.setTitle("本次累積："+ Time);
            builder.setSingleChoiceItems(course, Preset, (dialog, which) -> Preset = which);
            builder.setPositiveButton("確認", (dialog, which) -> {
                if (Preset ==5) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(TomatoClockActivity.this);
                    alert.setCancelable(false);
                    alert.setTitle("輸入讀書科目");
                    alert.setView(editText);
                    alert.setPositiveButton("確定", (dialogInterface, i) -> TomatoStudyCourse = editText.getText().toString());
                    alert.show();
                }
                else{
                    TomatoStudyCourse= course[Preset];
                }
            });
            insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
            builder.show();
            recordTime = 0;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {
        showDialog();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isCounting){
            startService(new Intent(TomatoClockActivity.this, CheckFrontApp.class));
            startService(new Intent(TomatoClockActivity.this, CheckFrontCommuApp.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, CheckFrontApp.class));
        stopService(new Intent(this, CheckFrontCommuApp.class));
    }

    public static TomatoClockActivity getTomatoClockActivity(){
        return tomatoClockActivity;
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

        v.vibrate(pattern, 5);
        return v;

    }

    public static boolean getIsCounting(){
        return isCounting;
    }

    public void finishCounting(){
        isCounting = false;
        study.cancel();
        timeButton.setEnabled(true);
        spinnerStudy.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.GONE);
        recordTime=0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showDialog() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);

            AlertDialog.Builder alert = new AlertDialog.Builder(this); //創建訊息方塊
            alert.setTitle("離開");
            alert.setMessage("確定要離開?");
            alert.setCancelable(false);
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

            AlertDialog alert2 = new AlertDialog.Builder(this)
                    .setTitle("Usage Access")
                    .setCancelable(false)
                    .setMessage("此APP需要允許漂浮視窗，否則將無法使用禁用APP的功能。")
                    .setPositiveButton("設定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        alert.show();//顯示訊息視窗
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            if (appList.size() == 0) {

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Usage Access")
                        .setCancelable(false)
                        .setMessage("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。")
                        .setPositiveButton("設定", (dialog, which) -> {
                            // continue with delete
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                                alert2.show();//顯示訊息視窗
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
            else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                alert2.show();//顯示訊息視窗
            } else{
                alert.show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showDialogStart() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);

            AlertDialog alert2 = new AlertDialog.Builder(this)
                    .setTitle("Usage Access")
                    .setMessage("此APP需要允許漂浮視窗，否則將無法使用禁用APP的功能。")
                    .setPositiveButton("設定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("放棄", (dialog, which) -> {
                        // do nothing
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            if (appList.size() == 0) {

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Usage Access")
                        .setMessage("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。")
                        .setPositiveButton("設定", (dialog, which) -> {
                            // continue with delete
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                                alert2.show();//顯示訊息視窗
                            }
                        })
                        .setNegativeButton("放棄", (dialog, which) -> {
                            // do nothing
                            dialog.dismiss();
                            if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                                alert2.show();//顯示訊息視窗
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
            else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                alert2.show();//顯示訊息視窗
            }
        }
    }

    //目錄相關操作
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_menuitem, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment2);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public String getDay(){
        String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return nowDate;
    }
    public String getTime(){
        String nowTime= new SimpleDateFormat("HH:mm:ss").format(new Date());
        return nowTime ;
    }
    public static String getTotalTime(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours +
                " : " +
                minutes +
                " : " +
                seconds );
    }


    //打開database
    private void openDB() {
        DBHelper = new DBTimeBlockHelper(this);
    }

    private void insertDB(String date ,String TomatoStudyCourse, String startTime,String stopTime ,String totalTime){

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_DATE ",date);
        values.put("_COURSE",TomatoStudyCourse);
        values.put("_STARTTIME",startTime);
        values.put("_STOPTIME",stopTime);
        values.put("_TOTAL",totalTime);
        db.insert("TimeBlocker",null,values);

    }
    private void closeDB() {
        DBHelper.close();
    }

    private void onDestory(){
        super.onDestroy();

        closeDB();
    }


}
