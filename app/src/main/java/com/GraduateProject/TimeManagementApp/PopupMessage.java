package com.GraduateProject.TimeManagementApp;

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
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PopupMessage extends AppCompatActivity {

    LinearLayout bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_popup_message);
        super.onCreate(savedInstanceState);
        Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
        bg = findViewById(R.id.transparentBG);
        Button btn_yes = findViewById(R.id.btn_yes);
        Button btn_no = findViewById(R.id.btn_no);
        Intent intent = getIntent();

        bg.setBackground(wallpaper);

        btn_yes.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed YES");
            if(GeneralTimerActivity.getIsCounting()){
            GeneralTimerActivity.getActivity().finish();
            } else{
            TomatoClockActivity.getTomatoClockActivity().finish();
            }
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(intent.getStringExtra("FrontApp"));
            startActivity(launchIntent);
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
                if (!mySortedMap.isEmpty()) {
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
}