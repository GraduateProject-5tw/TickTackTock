package com.GraduateProject.TimeManagementApp;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ToDoModel {
    private int id, status;
    private String task;
    private String nowDate;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }
    public void setTask(String task) {
        this.task = task;
    }

    //抓當下日期
    public String getDay(){
        Calendar day = TodoHeaderView.getSelectedAddDay();
        int selectedMonth = day.get(Calendar.MONTH) + 1;
        nowDate = day.get(Calendar.YEAR) + "-" + String.format("%02d", selectedMonth) + "-" + String.format("%02d", day.get(Calendar.DAY_OF_MONTH));
        return nowDate;
    }
    public void setDay(String nowDate){ this.nowDate = nowDate; }
}
