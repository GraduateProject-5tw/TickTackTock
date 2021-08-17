package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.GraduateProject.TimeManagementApp.Adapters.ToDoAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;


public class TodayToDoListActivity extends AppCompatActivity implements DialogCloseListener{

    @SuppressLint("StaticFieldLeak")
    private DBToDoHelper db;
    private RecyclerView tasksRecyclerView;
    private ToDoAdapter taskAdapter;
    private FloatingActionButton new_btn;
    private static TodayToDoListActivity todayToDoListActivity;

    private List<ToDoModel> taskList;

    private WeekHeaderView mWeekHeaderView;
    private TextView mTv_date;

    private void assignViews() {
        mWeekHeaderView= (WeekHeaderView) findViewById(R.id.weekheaderview);
        mTv_date =(TextView)findViewById(R.id.tv_date);
//init WeekView
        /*
        mWeekHeaderView.setDateSelectedChangeListener(new WeekHeaderView.DateSelectedChangeListener() {
            @Override
            public void onDateSelectedChange(Calendar oldSelectedDay, Calendar newSelectedDay) {
                mWeekView.goToDate(newSelectedDay);
            }
        });

         */
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todolist);
        //Objects.requireNonNull(getSupportActionBar()).hide();
        assignViews();

        db = new DBToDoHelper(this);
        db.openDatabase();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new ToDoAdapter(db, TodayToDoListActivity.this);
        tasksRecyclerView.setAdapter(taskAdapter);


        todayToDoListActivity = this;

        new_btn = findViewById(R.id.new_btn);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        taskAdapter.setTasks(taskList);

        new_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });
    }

    @Override
    public void handleDialogClose(DialogInterface dialog){
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        taskAdapter.setTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }
}

