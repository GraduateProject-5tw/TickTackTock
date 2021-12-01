package com.GraduateProject.TimeManagementApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.GraduateProject.TimeManagementApp.Adapters.AppListAdapter;
import com.google.gson.Gson;

import java.util.List;

public class AppsListActivity extends AppCompatActivity {

    protected static String userName;
    private static DBTotalHelper dbBannedAppsHelper = null;
    private static final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static SQLiteDatabase db = null;
    private final Gson gson = new Gson();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customapp_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("編輯禁用");

        RecyclerView app_list = findViewById(R.id.app_list);
        app_list.setLayoutManager(new LinearLayoutManager(this));
        List<AppInfo> allApps = LoadingApp.getAllowedAppInfos();
        List<String> bannedApps = LoadingApp.getAllowedApps();
        AppListAdapter adapter = new AppListAdapter(allApps, bannedApps);
        app_list.setAdapter(adapter);

        openDB();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("Update from adapter", gson.toJson(AppListAdapter.getEditedApps()));
        customAppsUpdateDB(gson.toJson(AppListAdapter.getEditedApps()));
        LoadingApp.setCustomAllowedApps(AppListAdapter.getEditedApps());
        LoadingApp.setAllowedAppInfos(AppListAdapter.getEditedAppInfos());
        finishAndRemoveTask();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.e("Update from adapter", gson.toJson(AppListAdapter.getEditedApps()));
            customAppsUpdateDB(gson.toJson(AppListAdapter.getEditedApps()));
            LoadingApp.setCustomAllowedApps(AppListAdapter.getEditedApps());
            LoadingApp.setAllowedAppInfos(AppListAdapter.getEditedAppInfos());

            final Dialog leave = new Dialog(this);
            leave.requestWindowFeature(Window.FEATURE_NO_TITLE);
            leave.setCancelable(false);
            leave.setContentView(R.layout.activity_popup_yesnobutton);

            TextView title = (TextView) leave.findViewById(R.id.txt_tit);
            title.setText("離 開");

            TextView content = (TextView) leave.findViewById(R.id.txt_dia);
            content.setText("尚未儲存變更，確定要離開嗎？");

            Button no = (Button) leave.findViewById(R.id.btn_no);
            no.setText("否");
            no.setOnClickListener(v -> leave.dismiss());

            Button yes = (Button) leave.findViewById(R.id.btn_yes);
            yes.setText("是");
            yes.setOnClickListener(v -> {
                leave.dismiss();
                finishAndRemoveTask();
            });
            leave.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //資料庫相關
    //打開database
    private void openDB() {
        dbBannedAppsHelper = new DBTotalHelper(this);
        db = dbBannedAppsHelper.getWritableDatabase();
    }

    public void customAppsUpdateDB(String customs){
        ContentValues values = new ContentValues();
        values.put("_CUSTOM", customs);
        db.update(TABLE_APPS,values,null, null);
    }

    private void closeDB() {
        dbBannedAppsHelper.close();
    }


}