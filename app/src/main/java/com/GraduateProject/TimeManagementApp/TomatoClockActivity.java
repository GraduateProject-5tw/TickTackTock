package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    final String[] resttime = new String[]{"5","10","15","20","25","30","35","40","45","50","55","60"};
    private int Preset = 0; //????????????
    private String   TomatoStudyCourse;//?????????????????????
    private AnalogClockStyle timeButton;
    private AppBarConfiguration mAppBarConfiguration;
    private DBTotalHelper DBHelper;
    private final String TABLE_APPS = "Courses";
    private final String TABLE_BG = "Background";
    private final ArrayList<String> courses = new ArrayList<>();
    private String startTime;
    private String date;
    private String stopTime;
    private String totalTime;
    private ToggleButton toggleButton;
    private String recordCourse;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tomatoClockActivity = this;
        setContentView(R.layout.activity_tomatoclock);  //???????????????????????????????????????activity_tomatoclock.xml

        showDialogStart();
        Toast.makeText(TomatoClockActivity.this, "????????????????????????", Toast.LENGTH_LONG).show();
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);     //??????K??????
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        timeButton = findViewById(R.id.clock); //clock image
        openDB();

        //??????????????????
        toggleButton=(ToggleButton)findViewById(R.id.tb);
        ImageView img= findViewById(R.id.backgroundtheme);
        boolean BGStatus;
        if(getBGStatus() == 1){
            Log.e("BG STATUS", "true");
            img.setBackground(getDrawable(R.drawable.background_view));
            BGStatus = true;
        }
        else {
            Log.e("BG STATUS", "false");
            img.setBackground(getDrawable(R.drawable.background_view_night));
            BGStatus = false;
        }
        toggleButton.setChecked(BGStatus);	//?????????????????? - true:??????, false:?????????
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) //???????????????????????????
            {
                updateBGStatus(1);
                img.setBackground(getDrawable(R.drawable.background_view));
            } else //??????????????????????????????
            {
                updateBGStatus(0);
                img.setBackground(getDrawable(R.drawable.background_view_night));
            }
        });


        //????????????
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
                R.id.nav_home, R.id.todolist, R.id.studytime,R.id.setting,R.id.web).setOpenableLayout(drawer).build();
        toolbar.setNavigationOnClickListener(view -> drawer.openDrawer(navigationView));

        navigationView.setNavigationItemSelectedListener(item -> {

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
                // launch web activity
                case R.id.web:
                    startActivity(new Intent(TomatoClockActivity.this, WebActivity.class));
                    break;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        navigationView.setCheckedItem(R.id.nav_home);


        //???????????????
        timeButton.setOnClickListener(v -> {
            showDialogSetTime("??????????????????:(??????)", studytime, 0);
        });

        general_btn.setEnabled(true);
        general_btn.setBackgroundColor(-1); //??????
        general_btn.setOnClickListener(v -> {
            if(isCounting){
                final Dialog change_clock = new Dialog(TomatoClockActivity.this);
                change_clock.requestWindowFeature(Window.FEATURE_NO_TITLE);
                change_clock.setCancelable(false);
                change_clock.setContentView(R.layout.activity_popup_yesnobutton);
                isCounting = false;
                stopTime = getTime();
                recordTime += (SystemClock.elapsedRealtime()-beginTime);//???????????????????????????
                String Time = getDurationBreakdown(recordTime);  //?????????????????????
                totalTime = getTotalTime(recordTime);

                TextView title = (TextView) change_clock.findViewById(R.id.txt_tit);
                title.setText("????????????");

                TextView content = (TextView) change_clock.findViewById(R.id.txt_dia);
                content.setText("???????????????????????????");
                Button yes = (Button) change_clock.findViewById(R.id.btn_yes);
                yes.setText("???");
                yes.setOnClickListener(v2 -> {
                    //general???????????????
                    Intent intent = new Intent();
                    intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
                    if(recordTime < 15*60000){
                        final Dialog study_record = new Dialog(TomatoClockActivity.this);
                        study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        study_record.setCancelable(false);
                        study_record.setContentView(R.layout.activity_popup_yesnobutton);

                        TextView title2 = (TextView) study_record.findViewById(R.id.txt_tit);
                        title2.setText("????????????");

                        TextView content2 = (TextView) study_record.findViewById(R.id.txt_dia);
                        content2.setText("??????????????????15?????? \n\n ???????????????????????????????????????");
                        Button yes_recordstudy = (Button) study_record.findViewById(R.id.btn_yes);
                        yes_recordstudy.setText("???");
                        yes_recordstudy.setOnClickListener(v3 -> {
                            //??????????????????
                            study_record.dismiss();
                            getCoursesInfo();
                            courses.add("????????????");
                            final String[] coursesArray = courses.toArray(new String[0]);
                            showStudyRecordDialog(Time,coursesArray,intent);
                        });
                        Button no_recordstudy = (Button) study_record.findViewById(R.id.btn_no);
                        no_recordstudy.setText("???");
                        no_recordstudy.setOnClickListener(v3 -> {
                            //tomato???????????????
                            study_record.dismiss();
                            courses.clear();
                            spinnerStudy.setIsNewProgress(false);
                            spinnerStudy.setIsNewProgress(false);
                            recordTime = 0;
                            beginTime = 0;
                            study.cancel();
                            TomatoClockActivity.this.finish();
                            startActivity(intent);
                        });
                        study_record.show();
                    }
                    else{
                        //??????????????????
                        getCoursesInfo();
                        courses.add("????????????");
                        final String[] coursesArray = courses.toArray(new String[0]);
                        showStudyRecordDialog(Time,coursesArray,intent);
                    }
                });

                //?????????????????????
                Button no = (Button) change_clock.findViewById(R.id.btn_no);
                no.setText("???");
                no.setOnClickListener(v2 -> {
                    change_clock.dismiss();
                    courses.clear();
                    stopBtn.setVisibility(View.VISIBLE);
                    beginTime=SystemClock.elapsedRealtime();  //??????????????????
                    isCounting = true; //???????????????
                    timeButton.setEnabled(false); //????????????????????????
                });

                change_clock.show();
            }
            else{
                //general???????????????
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

        //tomato?????????
        tomato_btn.setEnabled(false);
        tomato_btn.setBackgroundColor(-3355444); //?????????
        tomato_btn.setTextColor(-1);

        //???????????????????????????
        startBtn.setOnClickListener(v -> {
            stopBtn.setVisibility(View.VISIBLE);
            startBtn.setVisibility(View.GONE);
            startTime = getTime();
            date = getDay();
            beginTime=SystemClock.elapsedRealtime();  //??????????????????
            isCounting = true; //???????????????
            timeButton.setEnabled(false); //????????????????????????

            //progress bar????????????
            initVariable();
            spinnerStudy = findViewById(R.id.progressBarStudy);
            Calendar calendar = Calendar.getInstance();
            spinnerStudy.setTime(futureInMillis);
            spinnerStudy.setMinute(calendar.get(Calendar.MINUTE));
            spinnerStudy.setVisibility(View.VISIBLE);

            //?????????progress bar???
            study = new MyCountdownTimer(futureInMillis, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if(mCurrentProgress > 0) {
                        mCurrentProgress -= 1000;
                        spinnerStudy.setProgress(mCurrentProgress);
                    }
                }


                @Override
                public void onFinish() {  // ???????????????,???????????????
                    if (futureInMillis == studyInMillis) {
                        recordTime += (SystemClock.elapsedRealtime()-beginTime);//???????????????????????????
                        isCounting = false;
                        spinnerStudy.setVisibility(View.GONE);
                        AlertDialog.Builder startrest = new AlertDialog.Builder(TomatoClockActivity.this);
                        startrest.setMessage("????????????");
                        startrest.setCancelable(true);  // disable click back button
                        startrest.setOnCancelListener(dialog -> {
                            //????????????progress bar
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
                        startstudy.setMessage("????????????");
                        startstudy.setCancelable(true);  // disable click back button
                        startstudy.setOnCancelListener(dialog -> {
                            //????????????progress bar
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


        //???????????????????????????
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
            String Time = getDurationBreakdown(recordTime);  //?????????????????????
            totalTime = getTotalTime(recordTime);
            //????????????
            if (recordTime < 15 * 60000) {
                final Dialog study_record = new Dialog(TomatoClockActivity.this);
                study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
                study_record.setCancelable(false);
                study_record.setContentView(R.layout.activity_popup_yesnobutton);

                TextView title2 = (TextView) study_record.findViewById(R.id.txt_tit);
                title2.setText("????????????");

                TextView content2 = (TextView) study_record.findViewById(R.id.txt_dia);
                content2.setText("??????????????????15?????? \n\n ???????????????????????????????????????");
                Button yes_recordstudy = (Button) study_record.findViewById(R.id.btn_yes);
                yes_recordstudy.setText("???");
                yes_recordstudy.setOnClickListener(v3 -> {
                    study_record.dismiss();
                    getCoursesInfo();
                    courses.add("????????????");
                    final String[] coursesArray = courses.toArray(new String[0]);
                    showStudyRecordDialog(Time,coursesArray);
                });
                Button no_recordstudy = (Button) study_record.findViewById(R.id.btn_no);
                no_recordstudy.setText("???");
                no_recordstudy.setOnClickListener(v3 -> {
                    //tomato???????????????
                    study_record.dismiss();
                    courses.clear();
                    spinnerStudy.setIsNewProgress(false);
                    spinnerStudy.setIsNewProgress(false);
                    recordTime = 0;
                    beginTime = 0;
                    study.cancel();
                });
                study_record.show();
            } else {
                //??????????????????
                getCoursesInfo();
                courses.add("????????????");
                final String[] coursesArray = courses.toArray(new String[0]);
                showStudyRecordDialog(Time,coursesArray);
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
        if (isCounting) {
            startForegroundService(new Intent(this, CheckFrontApp.class));
            startForegroundService(new Intent(this, CheckFrontCommuApp.class));
            startForegroundService(new Intent(this,CheckFrontBrowser.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
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
            Toast.makeText(TomatoClockActivity.this, "????????????????????????", Toast.LENGTH_LONG).show();
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
                " ?????? " +
                minutes +
                " ??? " +
                seconds +
                " ???");
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
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        stopService(new Intent(this, CheckFrontApp.class));
        stopService(new Intent(this, CheckFrontCommuApp.class));
        stopService(new Intent(this, DialogShow.class));
        stopService(new Intent(this, DialogShowCommu.class));
        stopService(new Intent(this, DialogShowBrowser.class));
        stopService(new Intent(this,CheckFrontBrowser.class));
    }

    public static boolean getIsCounting() {
        return isCounting;
    }

    //??????????????????
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


    //??????database
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

    private int getBGStatus(){
        String Query = "Select * from " + TABLE_BG;
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        int BG = cursor.getInt(cursor.getColumnIndex("_TOMATO"));
        cursor.close();
        return BG;
    }

    private void updateBGStatus(int BG){
        ContentValues values = new ContentValues();
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        values.put("_TOMATO", BG);
        db.update(TABLE_BG,values,null, null);
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
        title.setText("??? ???");

        TextView content = (TextView) leave.findViewById(R.id.txt_dia);
        content.setText("?????????????????????");

        Button no = (Button) leave.findViewById(R.id.btn_no);
        no.setText("???");
        no.setOnClickListener(v -> leave.dismiss());

        Button yes = (Button) leave.findViewById(R.id.btn_yes);
        yes.setText("???");
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
        text.setText("???APP???????????????????????? \n\n ???????????????????????????APP?????????");

        Button setFloat = (Button) floating.findViewById(R.id.btn_yes);
        setFloat.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            floating.dismiss();
            leave.show();
        });

        if (appList.size() == 0) {

            final Dialog access = new Dialog(TomatoClockActivity.this);
            access.requestWindowFeature(Window.FEATURE_NO_TITLE);
            access.setCancelable(false);
            access.setContentView(R.layout.activity_popup_singlebutton);

            TextView text2 = (TextView) access.findViewById(R.id.txt_dia);
            text2.setText("???APP????????????????????????APP?????? \n\n ???????????????????????????APP?????????");

            Button setAccess = (Button) access.findViewById(R.id.btn_yes);
            setAccess.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                    floating.show();//??????????????????
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
            floating.show();//??????????????????
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
        text.setText("???APP???????????????????????? \n\n ???????????????????????????APP?????????");

        Button setFloat = (Button) floating.findViewById(R.id.btn_yes);
        setFloat.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            floating.dismiss();
        });

        if (appList.size() == 0) {

            final Dialog access = new Dialog(TomatoClockActivity.this);
            access.requestWindowFeature(Window.FEATURE_NO_TITLE);
            access.setCancelable(false);
            access.setContentView(R.layout.activity_popup_singlebutton);

            TextView text2 = (TextView) access.findViewById(R.id.txt_dia);
            text2.setText("???APP????????????????????????APP?????? \n\n ???????????????????????????APP?????????");

            Button setAccess = (Button) access.findViewById(R.id.btn_yes);
            setAccess.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
                    floating.show();//??????????????????
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(TomatoClockActivity.this)) {
            floating.show();//??????????????????
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
                Toast.makeText(TomatoClockActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
            }
            else{
                dialog.dismiss();
                showDialogSetRestTime("??????????????????:(??????)", resttime, 0);
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
                Toast.makeText(TomatoClockActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
            }
            else {
                dialog.dismiss();
                //????????????????????????
                Toast.makeText(TomatoClockActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                futureInMillis = studyInMillis;
                //????????????????????????
                startBtn.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
    }
    public void showStudyRecordDialog(String Time,String[]coursesArray,Intent intent){
        //??????????????????
        final Dialog study_record = new Dialog(TomatoClockActivity.this);
        study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
        study_record.setCancelable(false);
        study_record.setContentView(R.layout.activity_popup_radiobutton);
        TextView text = (TextView) study_record.findViewById(R.id.txt_tit);
        text.setText("???????????????" + Time);
        //Create an instance of Adapter for Listview
        SingleChoooiceAdapter adapter = new SingleChoooiceAdapter(TomatoClockActivity.this, R.layout.list_item, coursesArray);
        //Create an instance of ListView for AlertDialog
        final ListView simpleListView = (ListView)study_record.findViewById(R.id.txt_dia);
        simpleListView.setAdapter(adapter);
        simpleListView.setOnItemClickListener(
                (arg0, view, position1, id) -> {
                    // TODO Auto-generated method stub
                    adapter.setSelectedIndex(position1);
                    adapter.notifyDataSetChanged();
                    Object o = simpleListView.getItemAtPosition(position1);
                    String record = o.toString();
                    recordCourse = record;


                }
        );
        Button no = (Button) study_record.findViewById(R.id.btn_no);
        no.setOnClickListener(v -> study_record.dismiss());

        Button yes = (Button) study_record.findViewById(R.id.btn_yes);
        yes.setOnClickListener(v -> {
            Log.e("INDEX", ": " + recordCourse);
            if(recordCourse.isEmpty()){
                Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
            }
            else if(recordCourse.equals("????????????")) {
                study_record.dismiss();
                final Dialog editstudy = new Dialog(TomatoClockActivity.this);
                editstudy.requestWindowFeature(Window.FEATURE_NO_TITLE);
                editstudy.setCancelable(false);
                editstudy.setContentView(R.layout.activity_popup_edittext);

                TextView text1 = (TextView) editstudy.findViewById(R.id.txt_tit);
                text1.setText("??????????????????");

                EditText editText = (EditText) editstudy.findViewById(R.id.editText);


                Button no1 = (Button) editstudy.findViewById(R.id.btn_no);
                no1.setOnClickListener(v1 -> editstudy.dismiss());

                Button yes1 = (Button) editstudy.findViewById(R.id.btn_yes);
                yes1.setOnClickListener(v1 -> {
                    if (editText.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        TomatoStudyCourse = editText.getText().toString();
                        Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                        insertCourse(TomatoStudyCourse);
                        insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                        editstudy.dismiss();
                        Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();

                    }

                });
                editstudy.show();
            } else{
                TomatoStudyCourse= recordCourse;
                insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                TomatoClockActivity.this.finish();
                startActivity(intent);
            }
            courses.clear();
            spinnerStudy.setIsNewProgress(false);
            spinnerStudy.setIsNewProgress(false);
            recordTime = 0;
            beginTime = 0;
            study.cancel();
        });
        study_record.show();

    }
    public void showStudyRecordDialog(String Time,String[]coursesArray){
        //??????????????????
        final Dialog study_record = new Dialog(TomatoClockActivity.this);
        study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
        study_record.setCancelable(false);
        study_record.setContentView(R.layout.activity_popup_radiobutton);
        TextView text = (TextView) study_record.findViewById(R.id.txt_tit);
        text.setText("???????????????" + Time);
        //Create an instance of Adapter for Listview
        SingleChoooiceAdapter adapter = new SingleChoooiceAdapter(TomatoClockActivity.this, R.layout.list_item, coursesArray);
        //Create an instance of ListView for AlertDialog
        final ListView simpleListView = (ListView)study_record.findViewById(R.id.txt_dia);
        simpleListView.setAdapter(adapter);
        simpleListView.setOnItemClickListener(
                (arg0, view, position1, id) -> {
                    // TODO Auto-generated method stub
                    adapter.setSelectedIndex(position1);
                    adapter.notifyDataSetChanged();
                    Object o = simpleListView.getItemAtPosition(position1);
                    String record = o.toString();
                    recordCourse = record;


                }
        );
        Button no = (Button) study_record.findViewById(R.id.btn_no);
        no.setOnClickListener(v -> study_record.dismiss());

        Button yes = (Button) study_record.findViewById(R.id.btn_yes);
        yes.setOnClickListener(v -> {
            Log.e("INDEX", ": " + recordCourse);
            if(recordCourse.isEmpty()){
                Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
            }
            else if(recordCourse.equals("????????????")) {
                study_record.dismiss();
                final Dialog editstudy = new Dialog(TomatoClockActivity.this);
                editstudy.requestWindowFeature(Window.FEATURE_NO_TITLE);
                editstudy.setCancelable(false);
                editstudy.setContentView(R.layout.activity_popup_edittext);

                TextView text1 = (TextView) editstudy.findViewById(R.id.txt_tit);
                text1.setText("??????????????????");

                EditText editText = (EditText) editstudy.findViewById(R.id.editText);


                Button no1 = (Button) editstudy.findViewById(R.id.btn_no);
                no1.setOnClickListener(v1 -> editstudy.dismiss());

                Button yes1 = (Button) editstudy.findViewById(R.id.btn_yes);
                yes1.setOnClickListener(v1 -> {
                    if (editText.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        TomatoStudyCourse = editText.getText().toString();
                        Log.e("COURSE", "selected course is " + TomatoStudyCourse);
                        insertCourse(TomatoStudyCourse);
                        insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                        editstudy.dismiss();
                        Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();

                    }

                });
                editstudy.show();
            } else{
                TomatoStudyCourse= recordCourse;
                insertDB(date,TomatoStudyCourse,startTime,stopTime,totalTime);
                Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                study_record.dismiss();

            }
            courses.clear();
            spinnerStudy.setIsNewProgress(false);
            spinnerStudy.setIsNewProgress(false);
            recordTime = 0;
            beginTime = 0;
            study.cancel();
        });
        study_record.show();

    }

    private void closeDB() {
        DBHelper.close();
    }

    private void onDestory(){
        super.onDestroy();
    }

}
