package com.GraduateProject.TimeManagementApp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class NotificationReciever extends BroadcastReceiver {
    private static final String NO_ACTION = "NO_ACTION";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.contains("com")) {
            /**if(GeneralTimerActivity.getInstance()!=null){
                Log.v("shuffTest", "Pressed YES");
                GeneralTimerActivity.getInstance().refresh();

                Intent service=  new Intent(context,NotificationService.class);
                context.stopService(service);
            }
        } else if (NO_ACTION.equals(action)) {
            Log.v("shuffTest", "Pressed NO");
            android.os.Process.killProcess(android.os.Process.myPid());**/

        }
    }


}
