package com.GraduateProject.TimeManagementApp;

import java.text.SimpleDateFormat;
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
        nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return nowDate;
    }
    public void setDay(String nowDate){ this.nowDate = nowDate; }
}
