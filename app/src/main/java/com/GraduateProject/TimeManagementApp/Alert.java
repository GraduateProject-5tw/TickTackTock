package com.GraduateProject.TimeManagementApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.NotificationCompat;
import java.util.Timer;
import java.util.TimerTask;

public class Alert extends Activity {

    //跳出通知
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //判斷按下返回鍵
            AlertDialog.Builder alert = new AlertDialog.Builder(Alert.this); //創建訊息方塊
            alert.setTitle("離開");
            alert.setMessage("確定要離開?");
            alert.setPositiveButton("是", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
                public void onClick(DialogInterface dialog, int i) {
                    Alert.this.finish();//關閉activity
                }
            });

            alert.setNegativeButton("否", new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
                public void onClick(DialogInterface dialog, int i) {
                }
            });
            alert.show();//顯示訊息視窗

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}