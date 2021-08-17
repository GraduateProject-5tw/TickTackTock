package com.GraduateProject.TimeManagementApp;

import android.graphics.drawable.Drawable;

import java.util.Objects;

public class AppInfo {
    String packageName;
    String appName;
    String appCategory;
    Drawable appLogo;
    boolean appStatus; //禁用為true 開訪則false

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof AppInfo)) return false;
        AppInfo appInfo=(AppInfo) o;
        return Objects.equals(getPackageName(),appInfo.getPackageName());
    }

    @Override
    public int hashCode(){
        return 0;
    }

    public String getPackageName(){
        return packageName;
    }

    public void setPackageName(String packageName){
        this.packageName=packageName;
    }

    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName){
        this.appName=appName;
    }

    public String getAppCategory(){
        return appCategory;
    }

    public void setAppCategory(String appName){
        this.appCategory=appCategory;
    }

    public Drawable getAppLogo(){
        return appLogo;
    }

    public void setAppLogo(Drawable appLogo){
        this.appLogo=appLogo;
    }

    public boolean isAppStatus(){
        return appStatus;
    }

    public void setAppStatus(boolean appStatus){
        this.appStatus=appStatus;
    }

}
