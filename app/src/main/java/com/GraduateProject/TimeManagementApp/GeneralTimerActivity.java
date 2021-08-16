package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleObserver;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.facebook.stetho.Stetho;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class GeneralTimerActivity extends AppCompatActivity implements LifecycleObserver {

    @SuppressLint("StaticFieldLeak")
    private static GeneralTimerActivity generalTimerActivity;
    private Chronometer chronometer; //計時器
    private Button startBtn;
    private Button stopBtn;
    private long recordTime;  //累計的時間
    private static boolean isCounting = false;
    private static boolean setPermission = false;
    private int Preset = 0; //讀書科目
    private String GeneralStudyCourse;//記錄的讀書科目
    private AppBarConfiguration mAppBarConfiguration;
    private Calendar calendar;
    private String date;
    private String startTime;
    private String stopTime;
    private String totalTime;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    DBTimeBlockerHelper DBHelper;

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
        openDB();
        Stetho.initializeWithDefaults(this);


        //目錄相關
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.nav_open, R.string.nav_close);
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
                        startActivity(new Intent(GeneralTimerActivity.this, TodayToDoListActivity.class));
                        break;
                        // launch time block
                    case R.id.studytime:
                        startActivity(new Intent(GeneralTimerActivity.this, TimeBlockerActivity.class));
                        break;
                        // launch settings activity
                    case R.id.setting:
                        startActivity(new Intent(GeneralTimerActivity.this, SettingsActivity.class));
                        break;
                    }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        navigationView.setCheckedItem(R.id.nav_home);


        //計時按鈕的功能實作
        startBtn.setOnClickListener(v -> {
            chronometer.setBase(SystemClock.elapsedRealtime());  //計時器歸0
            chronometer.start();
            startBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            isCounting = true;
            date= getDay();
            startTime=getTime();

        });

        //停止按鈕的功能實作
        stopBtn.setOnClickListener(v -> {
            chronometer.stop();
            stopTime = getTime();
            isCounting = false;
            recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //取得累計時間，單位是毫秒
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            totalTime = getTotalTime(recordTime);
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);


            //跳出視窗
            final String[] course = {"國文", "英文", "數學", "社會", "自然", "其他"};
            final EditText editText = new EditText(GeneralTimerActivity.this);//其他的文字輸入方塊
            AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
            builder.setCancelable(false);
            builder.setTitle("本次累積：" + Time);
            builder.setSingleChoiceItems(course, Preset, (dialog, which) -> Preset = which);
            builder.setPositiveButton("確認", (dialog, which) -> {
                if (Preset == 5) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(GeneralTimerActivity.this);
                    alert.setCancelable(false);
                    alert.setTitle("輸入讀書科目");
                    alert.setView(editText);
                    alert.setPositiveButton("確定", (dialogInterface, i) -> GeneralStudyCourse = editText.getText().toString());
                    alert.show();
                }
                else {
                    GeneralStudyCourse = course[Preset];
                }
            });
            builder.show();
            insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
            recordTime = 0;
            chronometer.setBase(SystemClock.elapsedRealtime()); //將計時器歸0

        });

        tomato_btn.setEnabled(true);
        tomato_btn.setBackgroundColor(-1); //白色
        tomato_btn.setOnClickListener(view ->
        {
            if (isCounting) {
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
                    record.setMessage("讀書時間：\n\n" + Time);
                    record.setPositiveButton("ok", (dialog1, which) -> {
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        recordTime = 0;
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
            } else {
                //tomato的切換頁面
                Intent intent = new Intent();
                intent.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                isCounting = false;
                chronometer.setBase(SystemClock.elapsedRealtime());
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                recordTime = 0;
                startActivity(intent);

            }
        });

        //general的禁按
        general_btn.setEnabled(false);
        general_btn.setBackgroundColor(-3355444);
    }

    @Override
    public void onBackPressed() {  //當按back按紐時
        showDialog();
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
        setPermission = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCounting && !setPermission) {
            startService(new Intent(GeneralTimerActivity.this, CheckFrontApp.class));
        }
    }

    public void finishCounting(){
        isCounting = false;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        recordTime = 0;
    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours +
                " 小時 " +
                minutes +
                " 分 " +
                seconds +
                " 秒");
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
                seconds +
                " :");
    }

    public static GeneralTimerActivity getActivity() {
        return generalTimerActivity;
    }

    public static boolean getIsCounting() {
        return isCounting;
    }

    public String getDay(){
        String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return nowDate;
    }
    public String getTime(){
        String nowTime= new SimpleDateFormat("HH:mm:ss").format(new Date());
        return nowTime ;
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void showDialog() {
        setPermission = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList.size() == 0) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Usage Access")
                        .setMessage("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。")
                        .setPositiveButton("設定", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("放棄", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
        }
    }



    //打開database
    private void openDB() {
        DBHelper = new DBTimeBlockerHelper(this);
    }

    private void insertDB(String date ,String GeneralStudyCourse, String stratTime,String stopTime ,String totalTime ){

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_DATE ",date);
        values.put("_COURSE",GeneralStudyCourse);
        values.put("_STARTTIME",stratTime);
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







