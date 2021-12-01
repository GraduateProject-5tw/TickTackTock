package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

public class SettingsActivity extends AppCompatActivity {

    protected static SwitchPreference mSwitchPreference;
    protected static SwitchPreference mSwitchPreferenceCommu;
    protected static SwitchPreference mSwitchPreferenceTheme;
    protected static Preference editButton;
    protected static Preference editCourse;
    protected static String userName;
    protected static int background;
    private static DBTotalHelper dbBannedAppsHelper = null;
    private static final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static SQLiteDatabase db = null;
    private final Gson gson = new Gson();
    private ToggleButton toggleButton;

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

            final Dialog leave = new Dialog(this);
            leave.requestWindowFeature(Window.FEATURE_NO_TITLE);
            leave.setCancelable(false);
            leave.setContentView(R.layout.activity_popup_yesnobutton);

            TextView title = (TextView) leave.findViewById(R.id.txt_tit);
            title.setText("離 開");

            TextView content = (TextView) leave.findViewById(R.id.txt_dia);
            content.setText("尚未儲存變更，確定要離開設定嗎？");

            Button no = (Button) leave.findViewById(R.id.btn_no);
            no.setText("否");
            no.setOnClickListener(v -> leave.dismiss());

            Button yes = (Button) leave.findViewById(R.id.btn_yes);
            yes.setText("是");
            yes.setOnClickListener(v -> {
                leave.dismiss();
                Intent intent = new Intent(SettingsActivity.this, GeneralTimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
            mSwitchPreferenceTheme = (SwitchPreference) findPreference("ThemeColor");
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

    public static int getBackground() {
        return background;
    }
}