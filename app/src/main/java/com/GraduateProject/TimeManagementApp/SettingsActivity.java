package com.GraduateProject.TimeManagementApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    public static SwitchPreference mSwitchPreference;
    public static Preference editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("設定");
        // load settings fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new MainPreferenceFragment()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SettingsActivity.this, GeneralTimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.main_setttings, rootKey);

            mSwitchPreference = (SwitchPreference) findPreference("isCustomApp"); //Preference Key
            editButton = findPreference("editCustomApp");

            mSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)) {
                        Log.e("Custom", "is Custom");
                        editButton.setEnabled(true);
                        mSwitchPreference.setChecked(true);
                        LoadingApp.setAllowedApps(LoadingApp.getCustomAllowedApps());
                    } else {
                        Log.e("Custom", "is Default");
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
    }
}