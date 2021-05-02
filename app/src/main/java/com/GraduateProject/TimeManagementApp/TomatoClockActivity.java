package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TomatoClockActivity extends AppCompatActivity {

    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);  //指定對應的畫面呈現程式碼在activity_tomatoclock.xml
        startBtn = findViewById(R.id.tstart_btn);
        Button general_btn = findViewById(R.id.generalTimer_btn);
        Button tomato_btn = findViewById(R.id.tomatoClock_btn);
        AnalogClockStyle timeButton = findViewById(R.id.clock); //clock image

        Toast.makeText(TomatoClockActivity.this, "點選時鐘設定時長", Toast.LENGTH_LONG).show();

        //當按下時鐘
        timeButton.setOnClickListener(v -> {
            AlertDialog.Builder timeConfirm = new AlertDialog.Builder(TomatoClockActivity.this);
            timeConfirm.setTitle("時間配置確認");
            timeConfirm.setMessage("\n\n讀書時間：25分鐘\n\n休息時間：10分鐘");
            timeConfirm.setIcon(android.R.drawable.ic_dialog_info);
            timeConfirm.setCancelable(false);

            //設定視窗按鈕的功能
            timeConfirm.setPositiveButton("OK", (dialog, which) -> {
                //顯示設定完成提醒
                Toast.makeText(TomatoClockActivity.this, "時間設定完成", Toast.LENGTH_SHORT).show();

                //出現開始計時按鈕
                startBtn.setVisibility(View.VISIBLE);
            });

            timeConfirm.show();
        });


        general_btn.setEnabled(true);
        general_btn.setBackgroundColor(-1); //白色
        //general的切換頁面
        Intent intent = new Intent();
        intent.setClass(TomatoClockActivity.this, GeneralTimerActivity.class);
        general_btn.setOnClickListener(v ->
                startActivity(intent));

        //general的禁按
        tomato_btn.setEnabled(false);
        tomato_btn.setBackgroundColor(-3355444); //淺灰色
    }


}
