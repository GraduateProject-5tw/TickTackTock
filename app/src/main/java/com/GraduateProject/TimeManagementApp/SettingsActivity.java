package com.GraduateProject.TimeManagementApp;

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
    protected static Preference editButton;
    protected static String userName;
    private static DBBannedAppHelper dbBannedAppsHelper = null;
    private static final String TABLE_APPS = "BannedApps";
    private static final String COL_USER = "_USER";
    private static final String COL_CHECK = "_ISCUSTOM";
    private static SQLiteDatabase db = null;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        openDB();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("設定");

        // load settings fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new MainPreferenceFragment()).commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                String updateString= gson.toJson(LoadingApp.getAllowedApps());
                customAppsUpdateDB(updateString, MainPreferenceFragment.getIsCustom());
                LoadingApp.setIsCustom(MainPreferenceFragment.getIsCustom());
                Log.e("UPDATE", updateString);
                Log.e("UPDATE", Integer.toString(MainPreferenceFragment.getIsCustom()));
                Intent intent = new Intent(SettingsActivity.this, GeneralTimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                closeDB();
                finishAndRemoveTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //資料庫相關
    //打開database
    private void openDB() {
        dbBannedAppsHelper = new DBBannedAppHelper(this);
        db = dbBannedAppsHelper.getWritableDatabase();
    }

    public void customAppsUpdateDB(String customs, int isCustom){
        ContentValues values = new ContentValues();
        values.put("_ISCUSTOM", isCustom);
        values.put("_CUSTOM", customs);
        db.update(TABLE_APPS,values,COL_USER + " = " + "'" + userName + "'", null);
    }

    private void closeDB() {
        dbBannedAppsHelper.close();
    }




    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        private static int isCustom;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.main_setttings, rootKey);

            mSwitchPreference = (SwitchPreference) findPreference("isCustomApp"); //Preference Key
            editButton = findPreference("editCustomApp");

            if(LoadingApp.getIsCustom() == 1){
                mSwitchPreference.setChecked(true);
                editButton.setEnabled(true);
                LoadingApp.setAllowedApps(LoadingApp.getCustomAllowedApps());
            }
            else{
                mSwitchPreference.setChecked(false);
                editButton.setEnabled(false);
                LoadingApp.setAllowedApps(LoadingApp.getDefaultAllowedApps());
            }

            mSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)) {
                        isCustom = 1;
                        Log.e("Custom", "is Custom" + isCustom);
                        editButton.setEnabled(true);
                        mSwitchPreference.setChecked(true);
                        LoadingApp.setAllowedApps(LoadingApp.getCustomAllowedApps());
                    } else {
                        isCustom = 0;
                        Log.e("Custom", "is Default" + isCustom);
                        LoadingApp.setAllowedApps(LoadingApp.getDefaultAllowedApps());
                        editButton.setEnabled(false);
                        mSwitchPreference.setChecked(false);
                    }
                    return true;
                }
            });

            editButton.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent("com.GraduateProject.TimeManagementApp.Banned");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
                return true;
            });
        }

        public static int getIsCustom() {
            return isCustom;
        }
    }

}