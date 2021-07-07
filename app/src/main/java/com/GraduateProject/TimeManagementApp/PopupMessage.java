package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PopupMessage extends AppCompatActivity {

    private static Button btn_yes, btn_no;
    FrameLayout bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_popup_message);
        super.onCreate(savedInstanceState);
        Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
        bg = findViewById(R.id.transparentBG);
        btn_yes = findViewById(R.id.btn_yes);
        btn_no = findViewById(R.id.btn_no);

        bg.setBackground(wallpaper);

        btn_yes.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed YES");
            GeneralTimerActivity.getActivity().finish();
            finish();
        });

        btn_no.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed NO");
            ActivityManager mActivityManager = (ActivityManager) PopupMessage.this.getSystemService(Context.ACTIVITY_SERVICE);
            mActivityManager.killBackgroundProcesses(getForegroundTask());
            finish();
        });
    }

    private String getForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant") UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return currentApp;
    }

    public void popMessage(int i, String frontApp){

        btn_yes.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed YES");
            if(i == 1){
                GeneralTimerActivity.getActivity().finish();
            }
            else if(i == 2){
                Intent intent = new Intent(PopupMessage.this, TomatoClockActivity.class);
                intent.putExtra("StopCurrent", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();
        });

        btn_no.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed NO");
            ActivityManager mActivityManager = (ActivityManager) PopupMessage.this.getSystemService(Context.ACTIVITY_SERVICE);
            mActivityManager.killBackgroundProcesses(frontApp);
            finish();
        });
    }
}