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

import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TimeBlockerActivity extends AppCompatActivity implements WeekDayView.MonthChangeListener,
        WeekDayView.EventClickListener, WeekDayView.EventLongPressListener,WeekDayView.EmptyViewClickListener,WeekDayView.EmptyViewLongPressListener,WeekDayView.ScrollListener {
    //view
    private WeekDayView mWeekView;
    private static WeekHeaderView mWeekHeaderView;
    private static Toolbar toolbar;
    private int currentYear = 0;
    private int currentMonth = 0;
    List<WeekViewEvent> mNewEvent = new ArrayList<WeekViewEvent>();
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_blocker);
        assignViews();

        //目錄相關
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleMarginStart(20);
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
                    startActivity(new Intent(TimeBlockerActivity.this, LoginActivity.class));
                    break;
                // launch general timer
                case R.id.nav_home:
                    startActivity(new Intent(TimeBlockerActivity.this, GeneralTimerActivity.class));
                    break;
                // launch to do list
                case R.id.todolist:
                    Log.e("Menu", "to do list");
                    startActivity(new Intent(TimeBlockerActivity.this, TodayToDoListActivity.class));
                    break;
                // launch time block
                case R.id.studytime:
                    break;
                // launch settings activity
                case R.id.setting:
                    startActivity(new Intent(TimeBlockerActivity.this, SettingsActivity.class));
                    break;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        navigationView.setCheckedItem(R.id.nav_home);

    }

    //目錄相關操作
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void assignViews() {
        mWeekView = (WeekDayView) findViewById(R.id.weekdayview);
        mWeekHeaderView= (WeekHeaderView) findViewById(R.id.weekheaderview);
        //init WeekView
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setScrollListener(this);
        mWeekHeaderView.setDateSelectedChangeListener((oldSelectedDay, newSelectedDay) -> mWeekView.goToDate(newSelectedDay));
        mWeekHeaderView.setScrollListener((newFirstVisibleDay, oldFirstVisibleDay) -> mWeekView.goToDate(mWeekHeaderView.getSelectedDay()));
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
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat("d", Locale.getDefault());
                return format.format(date.getTime());
            }
            @Override
            public String interpretTime(int hour) {
                return String.format("%02d:00", hour);
            }
            @Override
            public String interpretWeek(int date) {
                if(date>7||date<1){
                    return null;
                }
                return weekLabels[date-1];
            }
        });
    }
    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
// Populate the week view with some events.

        DBTotalHelper dbTimeBlockHelper=new DBTotalHelper(this);
        Cursor cursor = dbTimeBlockHelper.ViewData();
        cursor.moveToFirst();

        int theID = 0;
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
        int rotation = 1;
        do{
            //int EventID = cursor.getInt(0);
            String Date = cursor.getString(1);
            String Course = cursor.getString(2);
            String starTime = cursor.getString(3);
            String stopTime = cursor.getString(4);

            int theYear = Integer.parseInt(Date.substring(0,4));
            int theMonth = Integer.parseInt(Date.substring(5,7)) - 1;
            //Date=Date+"0";
            int theDay = Integer.parseInt(Date.substring(8,10));

            if(theMonth == newMonth){
                String[] starts = starTime.split(":");
                int starHour = Integer.parseInt(starts[0]);
                int starMinute = Integer.parseInt(starts[1]);
                String[] ends = stopTime.split(":");
                int endHour = Integer.parseInt(ends[0]);
                int endMinute = Integer.parseInt(ends[1]);


                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, starHour);
                startTime.set(Calendar.MINUTE, starMinute);
                startTime.set(Calendar.DAY_OF_MONTH, theDay);
                startTime.set(Calendar.MONTH, theMonth);
                startTime.set(Calendar.YEAR, theYear);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.set(Calendar.HOUR_OF_DAY, endHour);
                endTime.set(Calendar.MINUTE, endMinute);
                WeekViewEvent event = new WeekViewEvent(theID, Course, startTime, endTime);
                event.setColor(getResources().getColor(R.color.purple_200));
                events.add(event);

                Log.e("TIME BLOCK", "回數：" + rotation + " 科目：" + Course + " 日期：" + Date);
            }

            theID=theID+1;
        }
        while (cursor.moveToNext());
        return events;
    }
    private String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH));
    }
    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(TimeBlockerActivity.this, "Clicked "+event.getName(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(TimeBlockerActivity.this, "Long pressed event: "+event.getName(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onEmptyViewClicked(Calendar time) {
        Toast.makeText(TimeBlockerActivity.this, "Empty View clicked "+time.get(Calendar.YEAR)+"/"+time.get(Calendar.MONTH)+"/"+time.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_LONG).show();
    }
    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(TimeBlockerActivity.this, "Empty View long clicked "+time.get(Calendar.YEAR)+"/"+time.get(Calendar.MONTH)+"/"+time.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_LONG).show();
    }
    @Override
    public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
    }
    @Override
    public void onSelectedDateChange(Calendar selectedDate) {
        mWeekHeaderView.setSelectedDay(selectedDate);
        Log.e("DATE", selectedDate.get(Calendar.YEAR)+" 年 "+(selectedDate.get(Calendar.MONTH)+1)+" 月 ");
        toolbar.setTitle(selectedDate.get(Calendar.YEAR)+" 年 "+(selectedDate.get(Calendar.MONTH) + 1)+" 月 ");
    }

    public static void onSelectedDateChangeHeader(Calendar selectedDate) {
        Log.e("DATE", selectedDate.get(Calendar.YEAR)+" 年 "+(selectedDate.get(Calendar.MONTH)+1)+" 月 ");
        toolbar.setTitle(selectedDate.get(Calendar.YEAR)+" 年 "+(selectedDate.get(Calendar.MONTH) + 1 )+" 月 ");
    }
}