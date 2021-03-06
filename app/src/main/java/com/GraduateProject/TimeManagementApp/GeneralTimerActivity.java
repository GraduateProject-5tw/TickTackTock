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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import com.GraduateProject.TimeManagementApp.Adapters.SingleChoooiceAdapter;
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
    private Chronometer chronometer; //?????????
    private Button startBtn;
    private Button stopBtn;
    private long recordTime;  //???????????????
    private static boolean isCounting = false;
    private int Preset = -1; //????????????
    private String GeneralStudyCourse;//?????????????????????
    private AppBarConfiguration mAppBarConfiguration;
    private String date;
    private String startTime;
    private String stopTime;
    private String totalTime;
    private ToggleButton toggleButton;
    private Chronometer mChronometer;
    private DBTotalHelper DBHelper;
    private final String TABLE_APPS = "Courses";
    private final String TABLE_BG = "Background";
    private final ArrayList<String> courses = new ArrayList<>();
    private Context context;
    private View mView;
    private String recordCourse;

    public GeneralTimerActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generaltimer);  //?????????????????????????????????activity_stopwatch.xml
        chronometer = findViewById(R.id.time_view);   //???id??????????????????????????????????????????????????????
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);

        generalTimerActivity = this;
        openDB();
        showDialogStart();

        //??????????????????
        toggleButton=(ToggleButton)findViewById(R.id.tb);
        ImageView img= findViewById(R.id.backgroundtheme);
        boolean BGStatus;
        if(getBGStatus() == 1){
            Log.e("BG STATUS", "true");
            img.setBackground(getDrawable(R.drawable.background_view));
            chronometer.setTextColor(Color.BLACK);
            BGStatus = true;
        }
        else {
            Log.e("BG STATUS", "false");
            img.setBackground(getDrawable(R.drawable.background_view_night));
            chronometer.setTextColor(Color.WHITE);
            BGStatus = false;
        }
        toggleButton.setChecked(BGStatus);	//?????????????????? - true:??????, false:?????????
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) //???????????????????????????
            {
                updateBGStatus(1);
                img.setBackground(getDrawable(R.drawable.background_view));
                chronometer.setTextColor(Color.BLACK);
            } else //??????????????????????????????
            {
                updateBGStatus(0);
                img.setBackground(getDrawable(R.drawable.background_view_night));
                chronometer.setTextColor(Color.WHITE);
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


        //???????????????????????????
        startBtn.setOnClickListener(v -> {
            chronometer.setBase(SystemClock.elapsedRealtime());  //????????????0
            chronometer.start();
            startBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            isCounting = true;
            date= getDay();
            startTime=getTime();
        });

        //???????????????????????????
        stopBtn.setOnClickListener(v -> {
            chronometer.stop();
            stopTime = getTime();
            isCounting = false;
            recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //????????????????????????????????????
            String Time = getDurationBreakdown(recordTime);  //?????????????????????
            totalTime = getTotalTime(recordTime);
            startBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            //????????????
            getCoursesInfo();
            courses.add("????????????");
            final String[] coursesArray = courses.toArray(new String[0]);
            if (recordTime < 15 * 60000) {
                final Dialog study_record_check = new Dialog(GeneralTimerActivity.this);
                study_record_check.requestWindowFeature(Window.FEATURE_NO_TITLE);
                study_record_check.setCancelable(false);
                study_record_check.setContentView(R.layout.activity_popup_yesnobutton);

                TextView title2 = (TextView) study_record_check.findViewById(R.id.txt_tit);
                title2.setText("????????????");

                TextView content2 = (TextView) study_record_check.findViewById(R.id.txt_dia);
                content2.setText("??????????????????15?????? \n\n ???????????????????????????????????????");

                Button yes_recordstudy = (Button) study_record_check.findViewById(R.id.btn_yes);
                yes_recordstudy.setText("???");
                yes_recordstudy.setOnClickListener(v3 -> {
                    study_record_check.dismiss();
                    showStudyRecordDialog(Time,coursesArray);

                });
                Button no_recordstudy = (Button) study_record_check.findViewById(R.id.btn_no);
                no_recordstudy.setText("???");
                no_recordstudy.setOnClickListener(v3 -> {
                            //tomato???????????????
                            study_record_check.dismiss();
                            courses.clear();
                            recordTime = 0;
                            chronometer.setBase(SystemClock.elapsedRealtime());
                        });
               study_record_check.show();

            } else {
                //??????????????????
                showStudyRecordDialog(Time,coursesArray);
            }
        });

        tomato_btn.setEnabled(true);
        tomato_btn.setBackgroundColor(-1);
        tomato_btn.setOnClickListener(view ->
        {
            getCoursesInfo();
            courses.add("????????????");
            final String[] coursesArray = courses.toArray(new String[0]);
            final EditText editText = new EditText(GeneralTimerActivity.this);//???????????????????????????
            if (isCounting) {

                final Dialog change_clock = new Dialog(GeneralTimerActivity.this);
                change_clock.requestWindowFeature(Window.FEATURE_NO_TITLE);
                change_clock.setCancelable(false);
                change_clock.setContentView(R.layout.activity_popup_yesnobutton);
                chronometer.stop();
                isCounting = false;
                recordTime = SystemClock.elapsedRealtime() - chronometer.getBase();  //????????????????????????????????????
                String Time = getDurationBreakdown(recordTime);  //?????????????????????

                TextView title = (TextView) change_clock.findViewById(R.id.txt_tit);
                title.setText("????????????");

                TextView content = (TextView) change_clock.findViewById(R.id.txt_dia);
                content.setText("????????????????????????????");

                Button no = (Button) change_clock.findViewById(R.id.btn_no);
                no.setText("???");
                no.setOnClickListener(v -> {
                    change_clock.dismiss();
                    courses.clear();
                    startBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.VISIBLE);
                    double temp = Double.parseDouble(chronometer.getText().toString().split(":")[1]) * 1000;
                    chronometer.setBase((long) (SystemClock.elapsedRealtime() - temp));
                    chronometer.start();
                    isCounting = true;
                });
                //??????????????????tomato
                Button yes = (Button) change_clock.findViewById(R.id.btn_yes);
                yes.setText("???");
                yes.setOnClickListener(v -> {
                    //tomato???????????????
                    change_clock.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(GeneralTimerActivity.this, TomatoClockActivity.class);
                    //????????????
                    if (recordTime < 15 * 60000) {
                        final Dialog study_record = new Dialog(GeneralTimerActivity.this);
                        study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        study_record.setCancelable(false);
                        study_record.setContentView(R.layout.activity_popup_yesnobutton);

                        TextView title2 = (TextView) study_record.findViewById(R.id.txt_tit);
                        title2.setText("????????????");

                        TextView content2 = (TextView) study_record.findViewById(R.id.txt_dia);
                        content2.setText("??????????????????15?????? \n\n ???????????????????????????????????????");
                        Button yes_recordstudy = (Button) study_record.findViewById(R.id.btn_yes);
                        yes_recordstudy.setText("???");
                        yes_recordstudy.setOnClickListener(v3 -> {//??????????????????
                                     study_record.dismiss();
                                    showStudyRecordDialog(Time,coursesArray);
                                });
                        Button no_recordstudy = (Button) study_record.findViewById(R.id.btn_no);
                        no_recordstudy.setText("???");
                        no_recordstudy.setOnClickListener(v3 -> {
                            //tomato???????????????
                            study_record.dismiss();
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

                        study_record.show();
                    } else {
                        //??????????????????
                        showStudyRecordDialog(Time,coursesArray);
                    }
                });
             change_clock.show();

            } else {
                //tomato???????????????
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

        //general?????????
        general_btn.setEnabled(false);
        general_btn.setBackgroundColor(-3355444);
        general_btn.setTextColor(-1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {  //??????back?????????
        showDialog();
    }

    @Override
    public void onPause() {
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
        stopService(new Intent(this, CheckFrontCommuApp.class));
        stopService(new Intent(this, DialogShow.class));
        stopService(new Intent(this, DialogShowCommu.class));
        stopService(new Intent(this, DialogShowBrowser.class));
        stopService(new Intent(this,CheckFrontBrowser.class));
    }

    public void finishCounting(){
        Log.e("Finish", "finish counting");
        isCounting = false;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
        recordTime = 0;
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        stopService(new Intent(this, CheckFrontApp.class));
        stopService(new Intent(this, CheckFrontCommuApp.class));
        stopService(new Intent(this, DialogShow.class));
        stopService(new Intent(this, DialogShowCommu.class));
        stopService(new Intent(this, DialogShowBrowser.class));
        stopService(new Intent(this,CheckFrontBrowser.class));
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
                " ?????? " +
                minutes +
                " ??? " +
                seconds +
                " ???");
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


    //??????????????????
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //dialogs
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showDialog() {
        @SuppressWarnings("WrongConstant")
        UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 1000, time);

        final Dialog leave = new Dialog(GeneralTimerActivity.this);
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

        final Dialog floating = new Dialog(GeneralTimerActivity.this);
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

            final Dialog access = new Dialog(GeneralTimerActivity.this);
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
                if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
                    floating.show();//??????????????????
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
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

        final Dialog floating = new Dialog(GeneralTimerActivity.this);
        floating.requestWindowFeature(Window.FEATURE_NO_TITLE);
        floating.setCancelable(false);
        floating.setContentView(R.layout.activity_popup_singlebutton);

        TextView text = (TextView) floating.findViewById(R.id.txt_dia);
        text.setText("???APP???????????????????????? \n\n ???????????????????????????APP????????????");

        Button setFloat = (Button) floating.findViewById(R.id.btn_yes);
        setFloat.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            floating.dismiss();
        });

        if (appList.size() == 0) {

            final Dialog access = new Dialog(GeneralTimerActivity.this);
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
                if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
                    floating.show();//??????????????????
                }
                access.dismiss();
            });
            access.show();
        }
        else if (!Settings.canDrawOverlays(GeneralTimerActivity.this)) {
            floating.show();//??????????????????
        }
    }
    public void showStudyRecordDialog(String Time,String[]coursesArray){
        //??????????????????
        final Dialog study_record = new Dialog(GeneralTimerActivity.this);
        study_record.requestWindowFeature(Window.FEATURE_NO_TITLE);
        study_record.setCancelable(false);
        study_record.setContentView(R.layout.activity_popup_radiobutton);
        TextView text = (TextView) study_record.findViewById(R.id.txt_tit);
        text.setText("???????????????" + Time);
        //Create an instance of Adapter for Listview
        SingleChoooiceAdapter adapter = new SingleChoooiceAdapter(GeneralTimerActivity.this, R.layout.list_item, coursesArray);
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
                final Dialog editstudy = new Dialog(GeneralTimerActivity.this);
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
                        GeneralStudyCourse = editText.getText().toString();
                        Log.e("COURSE", "selected course is " + GeneralStudyCourse);
                        insertCourse(GeneralStudyCourse);
                        insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                        editstudy.dismiss();
                        Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();

                    }

                });
                editstudy.show();
            }else {
                study_record.dismiss();
                GeneralStudyCourse = recordCourse;
                insertDB(date,GeneralStudyCourse,startTime,stopTime,totalTime);
                Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();

            }
            courses.clear();
            recordTime = 0;
            chronometer.setBase(SystemClock.elapsedRealtime());
        });
        study_record.show();

    }




    //??????database
    private void openDB() {
        DBHelper = new DBTotalHelper(this);
    }  //???:new DBTimeBlockHelper

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

    private int getBGStatus(){
        String Query = "Select * from " + TABLE_BG;
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        int BG = cursor.getInt(cursor.getColumnIndex("_GENERAL"));
        cursor.close();
        return BG;
    }

    private void updateBGStatus(int BG){
        ContentValues values = new ContentValues();
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        values.put("_GENERAL", BG);
        db.update(TABLE_BG,values,null, null);
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
            ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup)mView.getParent()).removeAllViews();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }


}







