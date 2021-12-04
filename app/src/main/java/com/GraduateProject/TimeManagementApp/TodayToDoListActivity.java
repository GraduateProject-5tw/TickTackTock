package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.GraduateProject.TimeManagementApp.Adapters.ToDoAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class TodayToDoListActivity extends AppCompatActivity implements DialogCloseListener{

    @SuppressLint("StaticFieldLeak")
    private static DBToDoHelper db;
    private static DBTotalHelper db_total;
    private static RecyclerView tasksRecyclerView;
    private static ToDoAdapter taskAdapter;
    private static FloatingActionButton new_btn;
    private static TodayToDoListActivity todayToDoListActivity;
    private static Toolbar toolbar;
    private static Calendar today;
    private ToggleButton toggleButton;
    private AppBarConfiguration mAppBarConfiguration;
    private static List<ToDoModel> taskList = new ArrayList<>();
    private TodoHeaderView mWeekHeaderView;
    private static final String TABLE_BG = "Background";
    private static SQLiteDatabase dbase = null;

//to do list建立
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);

        openDB();

        //深色背景按鈕
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
        toggleButton.setChecked(BGStatus);	//設定按紐狀態 - true:選取, false:未選取
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) //當按鈕狀態為選取時
            {
                updateBGStatus(1);
                img.setBackground(getDrawable(R.drawable.background_view));
            } else //當按鈕狀態為未選取時
            {
                updateBGStatus(0);
                img.setBackground(getDrawable(R.drawable.background_view_night));
            }
        });

        todayToDoListActivity = this;

        new_btn = findViewById(R.id.new_btn);
        new_btn.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        //目錄相關
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
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
                    startActivity(new Intent(TodayToDoListActivity.this, GeneralTimerActivity.class));
                    break;
                // launch to do list
                case R.id.todolist:
                    break;
                // launch time block
                case R.id.studytime:
                    startActivity(new Intent(TodayToDoListActivity.this, TimeBlockerActivity.class));
                    break;
                // launch settings activity
                case R.id.setting:
                    startActivity(new Intent(TodayToDoListActivity.this, SettingsActivity.class));
                    break;
                // launch web activity
                case R.id.web:
                    startActivity(new Intent(TodayToDoListActivity.this, WebActivity.class));
                    break;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        navigationView.setCheckedItem(R.id.nav_home);

        assignViews();

        today = Calendar.getInstance();
        int selectedMonth = today.get(Calendar.MONTH) + 1;

        String selectedDate = today.get(Calendar.YEAR) + "-" + String.format("%02d", selectedMonth) + "-" + String.format("%02d", today.get(Calendar.DAY_OF_MONTH));
        Log.e("TASK", selectedDate);

        tasksRecyclerView = todayToDoListActivity.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(todayToDoListActivity));

        taskList.clear();
        taskList = db.getAllTasks(selectedDate);
        Collections.reverse(taskList);
        taskAdapter = new ToDoAdapter(db, todayToDoListActivity);
        taskAdapter.setTasks(taskList);
        tasksRecyclerView.setAdapter(taskAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
    }

    //目錄相關操作
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //標頭
    @Override
    public void handleDialogClose(DialogInterface dialog){
        Calendar day = mWeekHeaderView.getSelectedDay();
        int selectedMonth = day.get(Calendar.MONTH) + 1;

        String selectedDate = day.get(Calendar.YEAR) + "-" + String.format("%02d", selectedMonth) + "-" + String.format("%02d", day.get(Calendar.DAY_OF_MONTH));
        Log.e("TASK", selectedDate);

        taskList = db.getAllTasks(selectedDate);
        Collections.reverse(taskList);
        taskAdapter.setTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }

    private void assignViews() {
        mWeekHeaderView= (TodoHeaderView) findViewById(R.id.weekheaderview);

        //init WeekView
        mWeekHeaderView.setDateSelectedChangeListener((oldSelectedDay, newSelectedDay) -> mWeekHeaderView.goToDate(newSelectedDay));
        mWeekHeaderView.setScrollListener((newFirstVisibleDay, oldFirstVisibleDay) -> mWeekHeaderView.goToDate(mWeekHeaderView.getSelectedDay()));
        setupDateTimeInterpreter(false);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     *
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        final String[] weekLabels={"日","一","二","三","四","五","六"};
    }


    public static void getTask(Calendar day) {
        toolbar.setTitle(day.get(Calendar.YEAR)+" 年 "+(day.get(Calendar.MONTH) + 1)+" 月 ");

        if(day.get(Calendar.YEAR) <= today.get(Calendar.YEAR) && day.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)){
            Log.e("BUTTON", "button disappear");
            new_btn.setVisibility(View.GONE);
        }
        else{
            new_btn.setVisibility(View.VISIBLE);
        }

        int selectedMonth = day.get(Calendar.MONTH) + 1;

        String selectedDate = day.get(Calendar.YEAR) + "-" + String.format("%02d", selectedMonth) + "-" + String.format("%02d", day.get(Calendar.DAY_OF_MONTH));
        Log.e("TASK", selectedDate);

        taskList = db.getAllTasks(selectedDate);
        Collections.reverse(taskList);
        taskAdapter.setTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }


    private void openDB() {
        db = new DBToDoHelper(this);
        db_total = new DBTotalHelper(this);
        dbase = db_total.getWritableDatabase();
    }

    private int getBGStatus(){
        String Query = "Select * from " + TABLE_BG;
        Cursor cursor = dbase.rawQuery(Query, null);
        cursor.moveToFirst();
        int BG = cursor.getInt(cursor.getColumnIndex("_TODO"));
        cursor.close();
        return BG;
    }

    private void updateBGStatus(int BG){
        ContentValues values = new ContentValues();
        values.put("_TODO", BG);
        dbase.update(TABLE_BG,values,null, null);
    }
}
