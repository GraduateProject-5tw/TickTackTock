package com.GraduateProject.TimeManagementApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReciever extends BroadcastReceiver {
    private static final String NO_ACTION = "NO_ACTION";
    private static final String YES_ACTION = "YES_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (YES_ACTION.equals(action)) {
            Log.v("shuffTest", "Pressed YES");

        } else if (NO_ACTION.equals(action)) {
            Log.v("shuffTest", "Pressed MAYBE");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
