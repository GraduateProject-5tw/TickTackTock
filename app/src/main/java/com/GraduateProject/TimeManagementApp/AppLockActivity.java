package com.GraduateProject.TimeManagementApp;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppLockActivity  extends AppCompatActivity {
    RecyclerView app_list;
    Button Applock_btn;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applock);
        app_list = findViewById(R.id.app_list);
        Applock_btn = findViewById(R.id.Applock_btn);
        app_list.setLayoutManager(new LinearLayoutManager(this));
        loadAppList();
    }
        public void loadAppList(){
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> homeApps = packageManager.queryIntentActivities(intent, 0);
            List<AppInfo> apps = new ArrayList<>();
            for (ResolveInfo info : homeApps) {
                AppInfo appInfo = new AppInfo();
                appInfo.setAppLogo(info.activityInfo.loadIcon(packageManager));
                appInfo.setPackageName(info.activityInfo.packageName);
                appInfo.setAppName((String) info.activityInfo.loadLabel(packageManager));
                apps.add(appInfo);
            }
            AppListAdapter adapter = new AppListAdapter(apps);
            app_list.setAdapter(adapter);
        }



}