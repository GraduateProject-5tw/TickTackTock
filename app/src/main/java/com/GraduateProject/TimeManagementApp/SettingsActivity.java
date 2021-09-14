package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

public class SettingsActivity extends AppCompatActivity {

    protected static SwitchPreference mSwitchPreference;
    protected static SwitchPreference mSwitchPreferenceCommu;
    protected static Preference editButton;
    protected static Preference editCourse;
    protected static String userName;
    private static DBTotalHelper dbBannedAppsHelper = null;
    private static final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static SQLiteDatabase db = null;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        openDB();

        Toolbar myToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("設定");

        // load settings fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new MainPreferenceFragment()).commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("UPDATE", Integer.toString(LoadingApp.getIsCustom()));
        customAppsUpdateDB(LoadingApp.getIsCustom(), LoadingApp.getIsBannedCommu());
        Intent intent = new Intent(SettingsActivity.this, GeneralTimerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAndRemoveTask();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.e("UPDATE", Integer.toString(LoadingApp.getIsCustom()));
            customAppsUpdateDB(LoadingApp.getIsCustom(), LoadingApp.getIsBannedCommu());
            AlertDialog.Builder alert = new AlertDialog.Builder(this); //創建訊息方塊
            alert.setTitle("離開");
            alert.setMessage("尚未儲存變更，確定要離開設定?");
            alert.setCancelable(false);
            //按"是",則退出應用程式
            alert.setPositiveButton("立即儲存", (dialog, i) -> {
                Intent intent = new Intent(SettingsActivity.this, GeneralTimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAndRemoveTask();
            });
            //按"否",則不執行任何操作
            alert.setNegativeButton("否", (dialog, i) -> {
            });
            alert.show();
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

    public void customAppsUpdateDB(int isCustom, int isBanned){
        ContentValues values = new ContentValues();
        values.put("_ISCUSTOM", isCustom);
        values.put("_BANNEDCOMMU", isBanned);
        db.update(TABLE_APPS,values,null, null);
    }

    private void closeDB() {
        dbBannedAppsHelper.close();
    }




    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        private static int isCustom;
        private static int bannedCommu;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.main_setttings, rootKey);

            mSwitchPreference = (SwitchPreference) findPreference("isCustomApp"); //Preference Key
            mSwitchPreferenceCommu = (SwitchPreference) findPreference("bannedCommuApp");
            editButton = findPreference("editCustomApp");
            editCourse = findPreference("editCourses");

            if(LoadingApp.getIsCustom() == 1){
                mSwitchPreference.setChecked(true);
                editButton.setEnabled(true);
            }
            else{
                mSwitchPreference.setChecked(false);
                editButton.setEnabled(false);
            }
            LoadingApp.setAllowedApps();

            if(LoadingApp.getIsBannedCommu() == 1){
                mSwitchPreferenceCommu.setChecked(true);
            }
            else{
                mSwitchPreferenceCommu.setChecked(false);
            }
            LoadingApp.setAllowedCommuApps();

            mSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals(true)) {
                    isCustom = 1;
                    editButton.setEnabled(true);
                    mSwitchPreference.setChecked(true);
                    LoadingApp.setIsCustom(isCustom);
                    LoadingApp.setAllowedApps();
                } else {
                    isCustom = 0;
                    LoadingApp.setIsCustom(isCustom);
                    LoadingApp.setAllowedApps();
                    editButton.setEnabled(false);
                    mSwitchPreference.setChecked(false);
                }
                Log.e("Custom", "is Custom" + LoadingApp.getIsCustom());
                return true;
            });

            editButton.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent("com.GraduateProject.TimeManagementApp.Banned");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
                return true;
            });

            mSwitchPreferenceCommu.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals(true)) {
                    bannedCommu = 1;
                    mSwitchPreferenceCommu.setChecked(true);
                    LoadingApp.setIfBannedCommu(bannedCommu);
                    LoadingApp.setAllowedCommuApps();
                } else {
                    bannedCommu = 0;
                    LoadingApp.setIfBannedCommu(bannedCommu);
                    LoadingApp.setAllowedCommuApps();
                    mSwitchPreferenceCommu.setChecked(false);
                }
                Log.e("COMMU", "is banned" + LoadingApp.getIsBannedCommu());
                return true;
            });

            editCourse.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent("com.GraduateProject.TimeManagementApp.course");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
                return true;
            });
        }
    }

}