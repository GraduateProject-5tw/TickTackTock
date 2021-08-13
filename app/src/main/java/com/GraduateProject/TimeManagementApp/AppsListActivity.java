package com.GraduateProject.TimeManagementApp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends AppCompatActivity {
    RecyclerView app_list;
    Button Applock_btn;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customapp_list);
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