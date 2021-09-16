package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
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

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class GeneralTimerActivity extends AppCompatActivity implements LifecycleObserver {

    @SuppressLint("StaticFieldLeak")
    private static GeneralTimerActivity generalTimerActivity;
    private Chronometer chronometer; //計時器
    private Button startBtn;
    private Button stopBtn;
    private long recordTime;  //累計的時間
    private static boolean isCounting = false;
    private int Preset = -1; //讀書科目
    private String GeneralStudyCourse;//記錄的讀書科目
    private AppBarConfiguration mAppBarConfiguration;
    private String date;
    private String startTime;
    private String stopTime;
    private String totalTime;
    private DBTotalHelper DBHelper;
    private final String TABLE_APPS = "Courses";
    private final ArrayList<String> courses = new ArrayList<>();

    public GeneralTimerActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        showDialogStart();

        //目錄相關
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.nav_open, R.string.nav_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_login,R.id.nav_home, R.id.todolist, R.id.studytime,R.id.setting).setOpenableLayout(drawer).build();
        toolbar.setNavigationOnClickListener(view -> drawer.openDrawer(navigationView));
        navigationView.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                //lunch login activity
                case R.id.nav_login:
                    startActivity(new Intent(GeneralTimerActivity.this, LoginActivity.class));
                    break;
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
                // launch web activity
                case R.id.web:
                    startActivity(new Intent(GeneralTimerActivity.this, WebActivity.class));
                    break;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
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
            getCoursesInfo();
            courses.add("新增科目");
            final String[] coursesArray = courses.toArray(new String[0]);
            final EditText editText = new EditText(GeneralTimerActivity.this);//其他的文字輸入方塊
            if (recordTime < 15 * 6000) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
                builder.setCancelable(false);
                builder.setTitle("紀錄確認");
                builder.setMessage("讀書時間未滿15分鐘，請問是否需要儲存此次紀錄？");
                builder.setPositiveButton("是", (dialog12, which) -> {
                    //顯示紀錄時間
                    AlertDialog.Builder b = new AlertDialog.Builder(GeneralTimerActivity.this);
                    b.setCancelable(false);
                    b.setTitle("本次累積：" + Time);
                    b.setSingleChoiceItems(coursesArray, Preset, (dialoga, whichi) -> Preset = whichi);
                    b.setPositiveButton("確認", ((dialoga, whichi) -> { }));
                    AlertDialog a = b.create();
                    a.show();
                    a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                        Log.e("INDEX", ": " + Preset);
                        if(Preset < 0){
                            Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                        }
                        else if(coursesArray[Preset].equals("新增科目")) {
                            a.dismiss();
                            AlertDialog.Builder courseDialog = new AlertDialog.Builder(GeneralTimerActivity.this);
                            courseDialog.setTitle("輸入讀書科目");
                            courseDialog.setView(editText);
                            courseDialog.setPositiveButton("確定", ((dialogs, y) -> { }));
                            AlertDialog alert = courseDialog.create();
                            alert.show();
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                                } else {
                                    GeneralStudyCourse = editText.getText().toString();
                                    Log.e("COURSE", "selected course is " + GeneralStudyCourse);
                                    insertCourse(GeneralStudyCourse);
                                    insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                    alert.dismiss();
                                }
                            }));
                            alert.setCancelable(false);
                            alert.setCanceledOnTouchOutside(false);
                        }else {
                            a.dismiss();
                            GeneralStudyCourse = coursesArray[Preset];
                            insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                        }
                        courses.clear();
                        recordTime = 0;
                        chronometer.setBase(SystemClock.elapsedRealtime());
                    }));
                });
                builder.setNegativeButton("否", (dialog13, which) -> {
                    courses.clear();
                    recordTime = 0;
                    chronometer.setBase(SystemClock.elapsedRealtime());
                });
                builder.show();
            } else {
                //顯示紀錄時間
                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
                builder.setCancelable(false);
                builder.setTitle("本次累積：" + Time);
                builder.setSingleChoiceItems(coursesArray, Preset, (dialoga, which) -> Preset = which);
                builder.setPositiveButton("確認", ((dialoga, which) -> { }));
                AlertDialog a = builder.create();
                a.show();
                a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                    if(Preset == -1){
                        Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                    }else if (coursesArray[Preset].equals("新增科目")) {
                        a.dismiss();
                        AlertDialog.Builder courseDialog = new AlertDialog.Builder(GeneralTimerActivity.this);
                        courseDialog.setTitle("輸入讀書科目");
                        courseDialog.setView(editText);
                        courseDialog.setPositiveButton("確定", ((dialogs, y) -> { }));
                        AlertDialog alert = courseDialog.create();
                        alert.show();
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                            if (editText.getText().toString().isEmpty()) {
                                Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                            } else {
                                GeneralStudyCourse = editText.getText().toString();
                                Log.e("COURSE", "selected course is " + GeneralStudyCourse);
                                insertCourse(GeneralStudyCourse);
                                insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                alert.dismiss();
                            }
                        }));
                        alert.setCancelable(false);
                        alert.setCanceledOnTouchOutside(false);
                    }else {
                        a.dismiss();
                        GeneralStudyCourse = coursesArray[Preset];
                        insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                    }
                    courses.clear();
                    recordTime = 0;
                    chronometer.setBase(SystemClock.elapsedRealtime());
                }));
            }
        });

        tomato_btn.setEnabled(true);
        tomato_btn.setBackgroundColor(-1); //白色
        tomato_btn.setOnClickListener(view ->
        {
            getCoursesInfo();
            courses.add("新增科目");
            final String[] coursesArray = courses.toArray(new String[0]);
            final EditText editText = new EditText(GeneralTimerActivity.this);//其他的文字輸入方塊
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
                    //跳出視窗
                    if (recordTime < 15 * 6000) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("紀錄確認");
                        builder.setMessage("讀書時間未滿15分鐘，請問是否需要儲存此次紀錄？");
                        builder.setPositiveButton("是", (dialog12, which) -> {
                            //顯示紀錄時間
                            AlertDialog.Builder b = new AlertDialog.Builder(GeneralTimerActivity.this);
                            b.setCancelable(false);
                            b.setTitle("本次累積：" + Time);
                            b.setSingleChoiceItems(coursesArray, Preset, (dialoga, whicho) -> Preset = whicho);
                            b.setPositiveButton("確認", ((dialoga, whicho) -> { }));
                            AlertDialog a = b.create();
                            a.show();
                            a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                                if(Preset == -1){
                                    Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                                }else if (coursesArray[Preset].equals("新增科目")) {
                                    a.dismiss();
                                    AlertDialog.Builder courseDialog = new AlertDialog.Builder(GeneralTimerActivity.this);
                                    courseDialog.setTitle("輸入讀書科目");
                                    courseDialog.setView(editText);
                                    courseDialog.setPositiveButton("確定", ((dialogs, y) -> { }));
                                    AlertDialog alert = courseDialog.create();
                                    alert.show();
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                        if (editText.getText().toString().isEmpty()) {
                                            Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                                        } else {
                                            GeneralStudyCourse = editText.getText().toString();
                                            Log.e("COURSE", "selected course is " + GeneralStudyCourse);
                                            insertCourse(GeneralStudyCourse);
                                            insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                            alert.dismiss();
                                            //tomato的切換頁面
                                            courses.clear();
                                            Intent intent2 = new Intent();
                                            intent2.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                                            startBtn.setVisibility(View.VISIBLE);
                                            stopBtn.setVisibility(View.GONE);
                                            startActivity(intent2);
                                            GeneralTimerActivity.this.finish();
                                        }
                                    }));
                                    alert.setCancelable(false);
                                    alert.setCanceledOnTouchOutside(false);
                                }else {
                                    a.dismiss();
                                    GeneralStudyCourse = coursesArray[Preset];
                                    insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                    //tomato的切換頁面
                                    courses.clear();
                                    Intent intent2 = new Intent();
                                    intent2.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                                    startBtn.setVisibility(View.VISIBLE);
                                    stopBtn.setVisibility(View.GONE);
                                    startActivity(intent2);
                                    GeneralTimerActivity.this.finish();
                                }
                                recordTime = 0;
                                chronometer.setBase(SystemClock.elapsedRealtime());
                            }));
                        });
                        builder.setNegativeButton("否", (dialog13, which) -> {
                            //tomato的切換頁面
                            courses.clear();
                            Intent intent2 = new Intent();
                            intent2.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                            startBtn.setVisibility(View.VISIBLE);
                            stopBtn.setVisibility(View.GONE);
                            startActivity(intent2);
                            GeneralTimerActivity.this.finish();
                            recordTime = 0;
                            chronometer.setBase(SystemClock.elapsedRealtime());
                        });
                        builder.show();
                    } else {
                        //顯示紀錄時間
                        AlertDialog.Builder builder = new AlertDialog.Builder(GeneralTimerActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("本次累積：" + Time);
                        builder.setSingleChoiceItems(coursesArray, Preset, (dialoga, which) -> Preset = which);
                        builder.setPositiveButton("確認", ((dialoga, which) -> { }));
                        AlertDialog a = builder.create();
                        a.show();
                        a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                            if(Preset == -1){
                                Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                            }else if (coursesArray[Preset].equals("新增科目")) {
                                a.dismiss();
                                AlertDialog.Builder courseDialog = new AlertDialog.Builder(GeneralTimerActivity.this);
                                courseDialog.setTitle("輸入讀書科目");
                                courseDialog.setView(editText);
                                courseDialog.setPositiveButton("確定", ((dialogs, y) -> { }));
                                AlertDialog alert = courseDialog.create();
                                alert.show();
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                    if (editText.getText().toString().isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                                    } else {
                                        GeneralStudyCourse = editText.getText().toString();
                                        Log.e("COURSE", "selected course is " + GeneralStudyCourse);
                                        insertCourse(GeneralStudyCourse);
                                        insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                        alert.dismiss();
                                        //tomato的切換頁面
                                        courses.clear();
                                        Intent intent2 = new Intent();
                                        intent2.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                                        startBtn.setVisibility(View.VISIBLE);
                                        stopBtn.setVisibility(View.GONE);
                                        startActivity(intent2);
                                        GeneralTimerActivity.this.finish();
                                    }
                                }));
                                alert.setCancelable(false);
                                alert.setCanceledOnTouchOutside(false);
                            }else {
                                a.dismiss();
                                GeneralStudyCourse = coursesArray[Preset];
                                insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                                //tomato的切換頁面
                                courses.clear();
                                Intent intent2 = new Intent();
                                intent2.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                                startBtn.setVisibility(View.VISIBLE);
                                stopBtn.setVisibility(View.GONE);
                                startActivity(intent2);
                                GeneralTimerActivity.this.finish();
                            }
                            recordTime = 0;
                            isCounting = false;
                            chronometer.setBase(SystemClock.elapsedRealtime());
                        }));
                    }
                });
                //否的話留在一般
                change.setNegativeButton("否", (dialog, i) -> {
                    courses.clear();
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
                courses.clear();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {  //當按back按紐時
        showDialog();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCounting) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                startForegroundService(new Intent(this, CheckFrontApp.class));
                startForegroundService(new Intent(this, CheckFrontCommuApp.class));
            } else {
                startService(new Intent(this, CheckFrontApp.class));
                startService(new Intent(this, CheckFrontCommuApp.class));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, CheckFrontApp.class));
        //stopService(new Intent(this, DialogShow.class));
        stopService(new Intent(this, CheckFrontCommuApp.class));
        stopService(new Intent(this, DialogShow.class));
    }

    public void finishCounting(){
        Log.e("Finish", "finish counting");
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


    //目錄相關操作
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
            alert.setCancelable(false);
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
                            if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
                                alert2.show();//顯示訊息視窗
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
            else if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
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
                    .setCancelable(false)
                    .setPositiveButton("設定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            if (appList.size() == 0) {

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Usage Access")
                        .setMessage("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。")
                        .setCancelable(false)
                        .setPositiveButton("設定", (dialog, which) -> {
                            // continue with delete
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
                                alert2.show();//顯示訊息視窗
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
            }
            else if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
                alert2.show();//顯示訊息視窗
            }
        }
    }




    //打開database
    private void openDB() {
        DBHelper = new DBTotalHelper(this);
    }  //原:new DBTimeBlockHelper

    private void insertDB(String date ,String GeneralStudyCourse, String stratTime,String stopTime ,String totalTime ){
        Log.e("COURSE", "insert in timeblock " + GeneralStudyCourse);
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_DATE ",date);
        values.put("_COURSE",GeneralStudyCourse);
        values.put("_STARTTIME",stratTime);
        values.put("_STOPTIME",stopTime);
        values.put("_TOTAL",totalTime);
        db.insert("TimeBlocker",null,values);

    }

    private void insertCourse(String course){

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_COURSE",course);
        values.put("_COLOR", -3825153);
        values.put("_TEXT",-1);
        db.insert(TABLE_APPS,null,values);

    }

    private void getCoursesInfo(){
        String Query = "Select * from " + TABLE_APPS;
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        int icount = cursor.getCount();
        if(icount > 0) {
            do{
                String course = cursor.getString(cursor.getColumnIndex("_COURSE"));
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void closeDB() {
        DBHelper.close();
    }

    private void onDestory(){
        super.onDestroy();
    }


}







