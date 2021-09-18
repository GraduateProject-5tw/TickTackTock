package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.GraduateProject.TimeManagementApp.Adapters.CourseListAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {

    private static DBTotalHelper dbBannedAppsHelper = null;
    private static final String TABLE_APPS = "Courses";
    private static SQLiteDatabase db = null;
    private final Gson gson = new Gson();
    private ArrayList<String> courses = new ArrayList<>();
    private ArrayList<Integer> colors = new ArrayList<>();
    private ArrayList<Integer> textColors = new ArrayList<>();
    private TextView add;
    private String course;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        final EditText editText = new EditText(CourseListActivity.this);//其他的文字輸入方塊

        openDB();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.course_toolbar);
        add = findViewById(R.id.add_course);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("編輯讀書科目");

        add.setOnClickListener(v -> {
            AlertDialog.Builder courseDialog = new AlertDialog.Builder(CourseListActivity.this);
            courseDialog.setTitle("輸入新科目");
            courseDialog.setView(editText);
            courseDialog.setPositiveButton("確定", ((dialogs, y) -> { }));
            courseDialog.setNegativeButton("取消", ((dialogs, y) -> { }));
            AlertDialog alert = courseDialog.create();
            alert.show();
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((x -> {
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "科目不可空白", Toast.LENGTH_SHORT).show();
                } else {
                    course = editText.getText().toString();
                    Log.e("COURSE", "added course is " + course);
                    courses.add(course);
                    insertCourse(course);
                    alert.dismiss();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
            }));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((x -> {
                alert.dismiss();
            }));
            alert.setCanceledOnTouchOutside(true);
            alert.setCancelable(true);
        });

        RecyclerView course_list = findViewById(R.id.course_list);
        course_list.setLayoutManager(new LinearLayoutManager(this));
        colors.clear();
        textColors.clear();
        getCoursesInfo();
        CourseListAdapter adapter = new CourseListAdapter(courses, colors, textColors, db);
        course_list.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CourseTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(course_list);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("Update from adapter", gson.toJson(AppListAdapter.getEditedApps()));
        customAppsUpdateDB(CourseListAdapter.getCourse_list(), CourseListAdapter.getColor_list(), CourseListAdapter.getText_list());
        finishAndRemoveTask();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            customAppsUpdateDB(CourseListAdapter.getCourse_list(), CourseListAdapter.getColor_list(), CourseListAdapter.getText_list());
            AlertDialog.Builder alert = new AlertDialog.Builder(this); //創建訊息方塊
            alert.setTitle("離開");
            alert.setMessage("尚未儲存變更，確定要離開設定?");
            alert.setCancelable(false);
            //按"是",則退出應用程式
            alert.setPositiveButton("立即儲存", (dialog, i) -> {
                finishAndRemoveTask();
            });
            //按"否",則不執行任何操作
            alert.setNegativeButton("否", (dialog, i) -> {
            });
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //資料庫相關
    //打開database
    private void openDB() {
        dbBannedAppsHelper = new DBTotalHelper(this);
        db = dbBannedAppsHelper.getWritableDatabase();
    }

    private void insertCourse(String course){

        ContentValues values = new ContentValues();
        values.put("_COURSE",course);
        values.put("_COLOR", -3825153);
        values.put("_TEXT",-1);
        db.insert(TABLE_APPS,null,values);

    }

    public void customAppsUpdateDB(List<String> course, List<Integer> color, List<Integer> text){
        db.execSQL("delete from "+ "Courses");
        for(int i = 0; i < course.size() ; i++){
            ContentValues values = new ContentValues();
            values.put("_COURSE",course.get(i));
            values.put("_COLOR", color.get(i));
            values.put("_TEXT", text.get(i));
            db.insert(TABLE_APPS,null,values);
        }
    }

    private void getCoursesInfo(){
        String Query = "Select * from " + TABLE_APPS;
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        int icount = cursor.getCount();
        if(icount > 0) {
            do{
                String course = cursor.getString(cursor.getColumnIndex("_COURSE"));
                int color = cursor.getInt(cursor.getColumnIndex("_COLOR"));
                int text = cursor.getInt(cursor.getColumnIndex("_TEXT"));
                courses.add(course);
                colors.add(color);
                textColors.add(text);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void closeDB() {
        dbBannedAppsHelper.close();
    }


}