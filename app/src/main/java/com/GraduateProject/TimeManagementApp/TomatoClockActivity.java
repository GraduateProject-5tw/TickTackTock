package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import com.GraduateProject.TimeManagementApp.Adapters.SingleChoooiceAdapter;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TomatoClockActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static TomatoClockActivity tomatoClockActivity;
    private Button startBtn;
    private Button stopBtn;
    private int futureInMillis;
    //private int studyInMillis;
    private int studyInMillis=0;
    //private int stopInMillis;
    private int stopInMillis=0;
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
    private DBTotalHelper DBHelper;
    private final String TABLE_APPS = "Courses";
    private final ArrayList<String> courses = new ArrayList<>();
    private String startTime;
    private String date;
    private String stopTime;
    private String totalTime;
    private ToggleButton toggleButton;
    private Context context;
    private View mView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tomatoClockActivity = this;
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml

        //深色背景按鈕
        toggleButton=(ToggleButton)findViewById(R.id.tb);
        ImageView img= findViewById(R.id.backgroundtheme);
        toggleButton.setChecked(true);	//設定按紐狀態 - true:選取, false:未選取
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) //當按鈕狀態為選取時
                {
                    img.setBackground(getDrawable(R.drawable.background_view));
                } else //當按鈕狀態為未選取時
                {
                    img.setBackground(getDrawable(R.drawable.background_view_night));
                }
            }
        });

        showDialogStart();
        Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);     //可用K停止
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        timeButton = findViewById(R.id.clock); //clock image
        openDB();


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
                    startActivity(new Intent(TomatoClockActivity.this, LoginActivity.class));
                    break;
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
        });
        navigationView.setCheckedItem(R.id.nav_home);


        //當按下時鐘
        timeButton.setOnClickListener(v -> {
            showDialogSetTime("讀書時間設置:(分鐘)", studytime, 0);
        });

        general_btn.setEnabled(true);
        general_btn.setBackgroundColor(-1); //白色
        general_btn.setOnClickListener(v -> {
            if(isCounting){
                // getting a LayoutInflater
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // inflating the view with the custom layout we created
                mView = layoutInflater.inflate(R.layout.activity_change_generalclock, null);
                mView.setFocusable(true);
                AlertDialog.Builder change = new AlertDialog.Builder(TomatoClockActivity.this);
                change.setTitle("一般計時");
                change.setMessage("是否要換成一般計時?");
                isCounting = false;
                stopTime = getTime();
                recordTime += (SystemClock.elapsedRealtime()-beginTime);//將讀完時間記錄下來
                String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
                totalTime = getTotalTime(recordTime);
                mView.findViewById(R.id.btn_yes).setOnClickListener(view -> {
                    //general的切換頁面
                    close();
                    Intent intent = new Intent();
                    intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                    if(recordTime < 15*60000){
                        // inflating the view with the custom layout we created
                        mView = layoutInflater.inflate(R.layout.activity_record_study, null);
                        mView.setFocusable(true);
                        mView.findViewById(R.id.btn_yes).setOnClickListener(view1 -> {
                            close();
                            //顯示紀錄時間
                            getCoursesInfo();
                            courses.add("新增科目");
                            final String[] coursesArray = courses.toArray(new String[0]);
                            final EditText editText = new EditText(TomatoClockActivity.this);
                            AlertDialog.Builder b = new AlertDialog.Builder(TomatoClockActivity.this);
                            b.setCancelable(false);
                            b.setTitle("本次累積："+ Time);
                            b.setSingleChoiceItems(coursesArray, Preset, (dialoga, which2) -> Preset = which2);
                            b.setPositiveButton("確認", (dialoga, which2) -> { });
                            AlertDialog a = b.create();
                            a.show();
                            a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                                if(Preset == -1){
                                    Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                                }else if (coursesArray[Preset].equals("新增科目")) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TomatoClockActivity.this);
                                    alertDialog.setTitle("輸入讀書科目");
                                    alertDialog.setView(editText);
                                    alertDialog.setPositiveButton("確定",((dialogs, y) -> {}));
                                    AlertDialog alert = alertDialog.create();
                                    alert.show();
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                        if(editText.getText().toString().isEmpty()){
                                            Toast.makeText(getApplicationContext(),"讀書科目不可空白",Toast.LENGTH_SHORT).show();}
                                        else {
                                            TomatoStudyCourse = editText.getText().toString();
                                            Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                                            insertCourse(TomatoStudyCourse);
                                            insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                            TomatoClockActivity.this.finish();
                                            startActivity(intent);
                                            alert.dismiss();
                                        }
                                    }));
                                    alert.setCancelable(false);
                                    alert.setCanceledOnTouchOutside(false);
                                }
                                else{
                                    TomatoStudyCourse= coursesArray[Preset];
                                    insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                    TomatoClockActivity.this.finish();
                                    startActivity(intent);
                                }
                                courses.clear();
                                spinnerStudy.setIsNewProgress(false);
                                spinnerStudy.setIsNewProgress(false);
                                recordTime = 0;
                                beginTime = 0;
                                study.cancel();
                            }));
                        });

                        mView.findViewById(R.id.btn_no).setOnClickListener(view1 -> {
                            close();
                            courses.clear();
                            spinnerStudy.setIsNewProgress(false);
                            spinnerStudy.setIsNewProgress(false);
                            recordTime = 0;
                            beginTime = 0;
                            study.cancel();
                            TomatoClockActivity.this.finish();
                            startActivity(intent);
                        });

                    }
                    else{
                        //顯示紀錄時間
                        getCoursesInfo();
                        courses.add("新增科目");
                        final String[] coursesArray = courses.toArray(new String[0]);
                        final EditText editText = new EditText(TomatoClockActivity.this);
                        AlertDialog.Builder b = new AlertDialog.Builder(TomatoClockActivity.this);
                        b.setCancelable(false);
                        b.setTitle("本次累積："+ Time);
                        b.setSingleChoiceItems(coursesArray, Preset, (dialoga, which2) -> Preset = which2);
                        b.setPositiveButton("確認", (dialoga, which2) -> { });
                        AlertDialog a = b.create();
                        a.show();
                        a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                            if(Preset == -1){
                                Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                            }else if (coursesArray[Preset].equals("新增科目")) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(TomatoClockActivity.this);
                                alertDialog.setTitle("輸入讀書科目");
                                alertDialog.setView(editText);
                                alertDialog.setPositiveButton("確定",((dialogs, y) -> {}));
                                AlertDialog alert = alertDialog.create();
                                alert.show();
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                    if(editText.getText().toString().isEmpty()){
                                        Toast.makeText(getApplicationContext(),"讀書科目不可空白",Toast.LENGTH_SHORT).show();}
                                    else {
                                        TomatoStudyCourse = editText.getText().toString();
                                        Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                                        insertCourse(TomatoStudyCourse);
                                        insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                        TomatoClockActivity.this.finish();
                                        startActivity(intent);
                                        alert.dismiss();
                                    }
                                }));
                                alert.setCancelable(false);
                                alert.setCanceledOnTouchOutside(false);
                            }
                            else{
                                TomatoStudyCourse= coursesArray[Preset];
                                insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                TomatoClockActivity.this.finish();
                                startActivity(intent);
                            }
                            courses.clear();
                            spinnerStudy.setIsNewProgress(false);
                            spinnerStudy.setIsNewProgress(false);
                            recordTime = 0;
                            beginTime = 0;
                            study.cancel();
                        }));
                    }
                });
                //否的話留在番茄
                mView.findViewById(R.id.btn_no).setOnClickListener(views -> {
                    close();
                    courses.clear();
                    stopBtn.setVisibility(View.VISIBLE);
                    beginTime=SystemClock.elapsedRealtime();  //抓取當下時間
                    isCounting = true; //正在計時中
                    timeButton.setEnabled(false); //計時中不得按時鐘
                });

            }
            else{
                //general的切換頁面
                courses.clear();
                Intent intent = new Intent();
                intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                isCounting = false;
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                TomatoClockActivity.this.finish();
                startActivity(intent);
            }
        });

        //tomato的禁按
        tomato_btn.setEnabled(false);
        tomato_btn.setBackgroundColor(-3355444); //淺灰色
        tomato_btn.setTextColor(-1);

        //計時按鈕的功能實作
        startBtn.setOnClickListener(v -> {
            stopBtn.setVisibility(View.VISIBLE);
            startBtn.setVisibility(View.GONE);
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
            recordTime += (SystemClock.elapsedRealtime() - beginTime);
            if (isCounting) {
                study.cancel();
                spinnerStudy.setVisibility(View.GONE);
                isCounting = false;
            }
            String Time = getDurationBreakdown(recordTime);  //轉成小時分鐘秒
            totalTime = getTotalTime(recordTime);
            //跳出視窗
            if (recordTime < 15 * 60000) {
                // getting a LayoutInflater
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // inflating the view with the custom layout we created
                mView = layoutInflater.inflate(R.layout.activity_record_study, null);
                mView.setFocusable(true);
                mView.findViewById(R.id.btn_yes).setOnClickListener(view -> {
                    close();
                    getCoursesInfo();
                    courses.add("新增科目");
                    final String[] coursesArray = courses.toArray(new String[0]);
                    final EditText editText = new EditText(TomatoClockActivity.this);
                    AlertDialog.Builder b = new AlertDialog.Builder(TomatoClockActivity.this);
                    b.setCancelable(false);
                    b.setTitle("本次累積："+ Time);
                    b.setSingleChoiceItems(coursesArray, Preset, (dialoga, which2) -> Preset = which2);
                    b.setPositiveButton("確認", (dialoga, which2) -> { });
                    AlertDialog a = b.create();
                    a.show();
                    a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                        if(Preset == -1){
                            Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                        }else if (coursesArray[Preset].equals("新增科目")) {
                            a.dismiss();
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(TomatoClockActivity.this);
                            alertDialog.setTitle("輸入讀書科目");
                            alertDialog.setView(editText);
                            alertDialog.setPositiveButton("確定",((dialogs, y) -> {}));
                            AlertDialog alert = alertDialog.create();
                            alert.show();
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                                if(editText.getText().toString().isEmpty()){
                                    Toast.makeText(getApplicationContext(),"讀書科目不可空白",Toast.LENGTH_SHORT).show();}
                                else {
                                    TomatoStudyCourse = editText.getText().toString();
                                    Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                                    insertCourse(TomatoStudyCourse);
                                    insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                    alert.dismiss();
                                }
                            }));
                            alert.setCancelable(false);
                            alert.setCanceledOnTouchOutside(false);
                        }
                        else{
                            a.dismiss();
                            TomatoStudyCourse= coursesArray[Preset];
                            insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                        }
                        courses.clear();
                        spinnerStudy.setIsNewProgress(false);
                        spinnerStudy.setIsNewProgress(false);
                        recordTime = 0;
                        beginTime = 0;
                        study.cancel();
                    }));
                });

                mView.findViewById(R.id.btn_no).setOnClickListener(view -> {
                    close();
                    courses.clear();
                    spinnerStudy.setIsNewProgress(false);
                    spinnerStudy.setIsNewProgress(false);
                    recordTime = 0;
                    beginTime = 0;
                    study.cancel();
                });

            } else {
                //顯示紀錄時間
                getCoursesInfo();
                courses.add("新增科目");
                final String[] coursesArray = courses.toArray(new String[0]);
                final EditText editText = new EditText(TomatoClockActivity.this);
                AlertDialog.Builder b = new AlertDialog.Builder(TomatoClockActivity.this);
                b.setCancelable(false);
                b.setTitle("本次累積："+ Time);
                b.setSingleChoiceItems(coursesArray, Preset, (dialoga, which2) -> Preset = which2);
                b.setPositiveButton("確認", (dialoga, which2) -> { });
                AlertDialog a = b.create();
                a.show();
                a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x1 -> {
                    if(Preset == -1){
                        Toast.makeText(getApplicationContext(), "讀書科目不可空白", Toast.LENGTH_SHORT).show();
                    }else if (coursesArray[Preset].equals("新增科目")) {
                        a.dismiss();
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TomatoClockActivity.this);
                        alertDialog.setTitle("輸入讀書科目");
                        alertDialog.setView(editText);
                        alertDialog.setPositiveButton("確定",((dialogs, y) -> {}));
                        AlertDialog alert = alertDialog.create();
                        alert.show();
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                            if(editText.getText().toString().isEmpty()){
                                Toast.makeText(getApplicationContext(),"讀書科目不可空白",Toast.LENGTH_SHORT).show();}
                            else {
                                TomatoStudyCourse = editText.getText().toString();
                                Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                                insertCourse(TomatoStudyCourse);
                                insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                                alert.dismiss();
                            }
                        }));
                        alert.setCancelable(false);
                        alert.setCanceledOnTouchOutside(false);
                    }
                    else{
                        a.dismiss();
                        TomatoStudyCourse= coursesArray[Preset];
                        insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                    }
                    courses.clear();
                    spinnerStudy.setIsNewProgress(false);
                    spinnerStudy.setIsNewProgress(false);
                    recordTime = 0;
                    beginTime = 0;
                    study.cancel();
                }));
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, CheckFrontApp.class));
                startForegroundService(new Intent(TomatoClockActivity.this, CheckFrontCommuApp.class));
                startForegroundService(new Intent(this, CheckFrontBrowser.class));
            } else {
                startService(new Intent(this, CheckFrontApp.class));
                startService(new Intent(TomatoClockActivity.this, CheckFrontCommuApp.class));
                startService(new Intent(this, CheckFrontBrowser.class));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, CheckFrontApp.class));
        stopService(new Intent(this, DialogShow.class));
        stopService(new Intent(this, CheckFrontCommuApp.class));
        stopService(new Intent(this, DialogShowCommu.class));
        stopService(new Intent(this, CheckFrontBrowser.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!isCounting){
            startBtn.setVisibility(View.GONE);
            Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();
        }
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

        long[] pattern = { 0, 3000, 3000, 1000, 1000, 3000};

        v.vibrate(pattern, 2);
        return v;

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

    //目錄相關操作
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
        DBHelper = new DBTotalHelper(this);
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


    //dialogs
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showDialog() {
        @SuppressWarnings("WrongConstant")
        UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 1000, time);

        final Dialog leave = new Dialog(TomatoClockActivity.this);
        leave.requestWindowFeature(Window.FEATURE_NO_TITLE);
        leave.setCancelable(false);
        leave.setContentView(R.layout.activity_popup_yesnobutton);

        TextView title = (TextView) leave.findViewById(R.id.txt_tit);
        title.setText("離 開");

        TextView content = (TextView) leave.findViewById(R.id.txt_dia);
        content.setText("確定要離開嗎？");

        Button no = (Button) leave.findViewById(R.id.btn_no);
        no.setText("否");
        no.setOnClickListener(v -> leave.dismiss());

        Button yes = (Button) leave.findViewById(R.id.btn_yes);
        yes.setText("是");
        yes.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            leave.dismiss();
        });

        final Dialog floating = new Dialog(TomatoClockActivity.this);
        floating.requestWindowFeature(Window.FEATURE_NO_TITLE);
        floating.setCancelable(false);
        floating.setContentView(R.layout.activity_popup_singlebutton);

        TextView text = (TextView) floating.findViewById(R.id.txt_dia);
        text.setText("此APP需要允許漂浮視窗，否則將無法使用禁用APP的功能。");

        Button setFloat = (Button) floating.findViewById(R.id.btn_yes);
        setFloat.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            floating.dismiss();
            leave.show();//顯示訊息視窗
        });

        if (appList.size() == 0) {

            final Dialog access = new Dialog(TomatoClockActivity.this);
            access.requestWindowFeature(Window.FEATURE_NO_TITLE);
            access.setCancelable(false);
            access.setContentView(R.layout.activity_popup_singlebutton);

            TextView text2 = (TextView) access.findViewById(R.id.txt_dia);
            text2.setText("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。");

            Button setAccess = (Button) access.findViewById(R.id.btn_yes);
            setAccess.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                    floating.show();//顯示訊息視窗
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
            floating.show();//顯示訊息視窗
        } else{
            leave.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showDialogStart() {
        @SuppressWarnings("WrongConstant")
        UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 1000, time);

        final Dialog floating = new Dialog(TomatoClockActivity.this);
        floating.requestWindowFeature(Window.FEATURE_NO_TITLE);
        floating.setCancelable(false);
        floating.setContentView(R.layout.activity_popup_singlebutton);

        TextView text = (TextView) floating.findViewById(R.id.txt_dia);
        text.setText("此APP需要允許漂浮視窗，否則將無法使用禁用APP的功能。");

        Button setFloat = (Button) floating.findViewById(R.id.btn_yes);
        setFloat.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            floating.dismiss();//顯示訊息視窗
        });

        if (appList.size() == 0) {

            final Dialog access = new Dialog(TomatoClockActivity.this);
            access.requestWindowFeature(Window.FEATURE_NO_TITLE);
            access.setCancelable(false);
            access.setContentView(R.layout.activity_popup_singlebutton);

            TextView text2 = (TextView) access.findViewById(R.id.txt_dia);
            text2.setText("此APP需要使用到部分權限，否則將無法使用禁用APP的功能。");

            Button setAccess = (Button) access.findViewById(R.id.btn_yes);
            setAccess.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                    floating.show();//顯示訊息視窗
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
            floating.show();//顯示訊息視窗
        }
    }

    public void showDialogSetTime(String title, String[] time, int position){
        final Dialog dialog = new Dialog(TomatoClockActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_popup_radiobutton);

        TextView text = (TextView) dialog.findViewById(R.id.txt_tit);
        text.setText(title);

        //Create an instance of Adapter for Listview
        SingleChoooiceAdapter adapter = new SingleChoooiceAdapter(TomatoClockActivity.this, R.layout.list_item, time);

        //Create an instance of ListView for AlertDialog
        final ListView simpleListView = (ListView)dialog.findViewById(R.id.txt_dia);
        simpleListView.setAdapter(adapter);
        simpleListView.setOnItemClickListener(
                (arg0, view, position1, id) -> {
                    // TODO Auto-generated method stub
                    adapter.setSelectedIndex(position1);
                    adapter.notifyDataSetChanged();
                    Object o = simpleListView.getItemAtPosition(position1);
                    String t = o.toString();
                    int tim = Integer.parseInt(t);
                    studyInMillis = tim * 60000;
                }
        );

        Button no = (Button) dialog.findViewById(R.id.btn_no);
        no.setOnClickListener(v -> dialog.dismiss());

        Button yes = (Button) dialog.findViewById(R.id.btn_yes);
        yes.setOnClickListener(v -> {
            if(studyInMillis == 0){
                Toast.makeText(TomatoClockActivity.this, "尚未選擇讀書時間", Toast.LENGTH_SHORT).show();
            }
            else{
                dialog.dismiss();
                showDialogSetRestTime("休息時間設置:(分鐘)", resttime, 0);
            }
        });


        dialog.show();

    }

    public void showDialogSetRestTime(String title, String[] time, int position){
        final Dialog dialog = new Dialog(TomatoClockActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_popup_radiobutton);

        TextView text = (TextView) dialog.findViewById(R.id.txt_tit);
        text.setText(title);

        //Create an instance of Adapter for Listview
        SingleChoooiceAdapter adapter = new SingleChoooiceAdapter(TomatoClockActivity.this, R.layout.list_item, time);

        //Create an instance of ListView for AlertDialog
        final ListView simpleListView = (ListView)dialog.findViewById(R.id.txt_dia);
        simpleListView.setAdapter(adapter);
        simpleListView.setOnItemClickListener(
                (arg0, view, position1, id) -> {
                    // TODO Auto-generated method stub
                    adapter.setSelectedIndex(position1);
                    adapter.notifyDataSetChanged();
                    Object o = simpleListView.getItemAtPosition(position1);
                    String t = o.toString();
                    int tim = Integer.parseInt(t);
                    stopInMillis = tim * 60000;
                }
        );

        Button no = (Button) dialog.findViewById(R.id.btn_no);
        no.setOnClickListener(v -> dialog.dismiss());

        Button yes = (Button) dialog.findViewById(R.id.btn_yes);
        yes.setOnClickListener(v -> {
            if(stopInMillis > studyInMillis || stopInMillis == studyInMillis){
                Toast.makeText(TomatoClockActivity.this, "休息時間只能小於讀書時間", Toast.LENGTH_SHORT).show();
            }
            else {
                dialog.dismiss();
                //顯示設定完成提醒
                Toast.makeText(TomatoClockActivity.this, "時間設定完成", Toast.LENGTH_SHORT).show();
                futureInMillis = studyInMillis;
                //出現開始計時按鈕
                startBtn.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
    }

    private void closeDB() {
        DBHelper.close();
    }

    private void onDestory(){
        super.onDestroy();
    }
    public void close() {

        try {
            // remove the view from the window
            ((WindowManager) context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup) mView.getParent()).removeAllViews();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2", e.toString());
        }
    }
}
