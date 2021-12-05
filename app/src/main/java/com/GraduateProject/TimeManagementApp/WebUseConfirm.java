package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static android.content.Context.WINDOW_SERVICE;

public class WebUseConfirm {

    // declaring required variables
    private final Context context;
    private final View mView;
    private final WindowManager.LayoutParams mParams;
    private final WindowManager mWindowManager;

    public WebUseConfirm(Context context){
        this.context=context;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            Log.e("VERSION", "ABOVE 26");
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);

        }
        else{
            Log.e("VERSION", "BELOW 26");
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows-
                    WindowManager.LayoutParams.TYPE_PHONE,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }
        mParams.dimAmount = 0.65f;
        // getting a LayoutInflater
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.activity_popup_singlebutton, null);

        mView.setFocusable(true);
        mView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_HOME) {
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_MENU){
                    return true;
                }
            }
            return false;
        });
        // set onClickListener on the remove button, which removes
        // the view from the window
        TextView tit = mView.findViewById(R.id.txt_tit);
        tit.setText("搜 尋 確 認");
        TextView dia = mView.findViewById(R.id.txt_dia);
        dia.setText("由於現在是專注時間 \n\n 搜尋時請避免非課業相關內容");

        Button confirm = mView.findViewById(R.id.btn_yes);
        confirm.setText("確 認");
        confirm.setOnClickListener(view -> {
            close();
            Intent intentHome;
            intentHome = new Intent(context, WebActivityBasic.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentHome);
        });

        mView.findViewById(R.id.message_background).setOnClickListener(v -> {
        });

        // Define the position of the
        // window within the screen
        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
        Log.e("POPUP", "window create");
    }

    public void open() {
        Log.e("POPUP", "window.open success");
        try {
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) {
                    mWindowManager.addView(mView, mParams);
                }
            }
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }

    }

    public void close() {

        try {
            // remove the view from the window
            ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup)mView.getParent()).removeAllViews();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }
}
