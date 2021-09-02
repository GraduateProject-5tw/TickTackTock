package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeBlockerActivity extends AppCompatActivity implements WeekDayView.MonthChangeListener,
        WeekDayView.EventClickListener, WeekDayView.EventLongPressListener,WeekDayView.EmptyViewClickListener,WeekDayView.EmptyViewLongPressListener,WeekDayView.ScrollListener {
    //view
    private WeekDayView mWeekView;
    private WeekHeaderView mWeekHeaderView;
    private TextView mTv_date;
    List<WeekViewEvent> mNewEvent = new ArrayList<WeekViewEvent>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_blocker);
        assignViews();

        DBTotalHelper dbTimeBlockHelper=new DBTotalHelper(this);
        TextView textView = findViewById(R.id.weekdayview);
        Cursor cursor = dbTimeBlockHelper.ViewData();

        int theID = 0;
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
        while (cursor.moveToNext()){
            //int EventID = cursor.getInt(0);
            String Date = cursor.getString(1);
            String Course = cursor.getString(2);
            String starTime = cursor.getString(3);
            String stopTime = cursor.getString(4);

            int theYear = Integer.parseInt(Date.substring(0,4));
            int theMonth = Integer.parseInt(Date.substring(5,7));
            Date=Date+"0";
            int theDay = Integer.parseInt(Date.substring(8,10));

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
            event.setColor(getResources().getColor(R.color.red));
            events.add(event);

            theID=theID+1;
        }
    }
    private void assignViews() {
        mWeekView = (WeekDayView) findViewById(R.id.weekdayview);
        mWeekHeaderView= (WeekHeaderView) findViewById(R.id.weekheaderview);
        mTv_date =(TextView)findViewById(R.id.tv_date);
//init WeekView
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setScrollListener(this);
        mWeekHeaderView.setDateSelectedChangeListener(new WeekHeaderView.DateSelectedChangeListener() {
            @Override
            public void onDateSelectedChange(Calendar oldSelectedDay, Calendar newSelectedDay) {
                mWeekView.goToDate(newSelectedDay);
            }
        });
        mWeekHeaderView.setScrollListener(new WeekHeaderView.ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                mWeekView.goToDate(mWeekHeaderView.getSelectedDay());
            }
        });
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
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR, 1);
        endTime.set(Calendar.MONTH, newMonth - 1);
        WeekViewEvent event = new WeekViewEvent(1, "This is a Event!!", startTime, endTime);
        event.setColor(getResources().getColor(R.color.black));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.red));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 4);
        startTime.set(Calendar.MINUTE, 20);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 5);
        endTime.set(Calendar.MINUTE, 0);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.purple_200));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(2, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.light_slate_Blue));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(2, "dddd", startTime, endTime);
        event.setColor(getResources().getColor(R.color.design_default_color_primary_dark));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        startTime.add(Calendar.DATE, 1);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(3, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.purple_200));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 15);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(4, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.light_slate_Blue));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 1);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.design_default_color_primary));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.HOUR_OF_DAY, 15);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.design_default_color_error));
        events.add(event);
        events.addAll(mNewEvent);
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
    public void onSelectedDaeChange(Calendar selectedDate) {
        mWeekHeaderView.setSelectedDay(selectedDate);
        mTv_date.setText(selectedDate.get(Calendar.YEAR)+"年"+(selectedDate.get(Calendar.MONTH))+"月");
    }
}