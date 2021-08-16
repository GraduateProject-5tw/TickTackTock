package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PopupMessageCommu extends AppCompatActivity {

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
        setFullscreen();

        btn_yes.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed YES");
            if(GeneralTimerActivity.getIsCounting()){
                GeneralTimerActivity.getActivity().finishCounting();
            } else{
                TomatoClockActivity.getTomatoClockActivity().finishCounting();
            }
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(intent.getStringExtra("FrontApp"));
            startActivity(launchIntent);
            finish();
        });

        btn_no.setOnClickListener(v -> {
            Log.v("shuffTest", "Pressed NO");
            ActivityManager mActivityManager = (ActivityManager) PopupMessageCommu.this.getSystemService(Context.ACTIVITY_SERVICE);
            mActivityManager.killBackgroundProcesses(getForegroundTask());
            finish();
        });
    }

    private String getForegroundTask() {
        String currentApp = "NULL";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

    public void setFullscreen() {
        Log.e("Hide","FullScreen");
        setFullscreen(this);
    }

    public void setFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (isImmersiveAvailable()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        } else {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static boolean isImmersiveAvailable() {
        return Build.VERSION.SDK_INT >= 19;
    }
}