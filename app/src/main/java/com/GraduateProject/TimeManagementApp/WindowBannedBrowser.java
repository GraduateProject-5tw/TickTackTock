package com.GraduateProject.TimeManagementApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class WindowBannedBrowser {
    // declaring required variables
    private final Context context;
    private final View mView;
    private final WindowManager.LayoutParams mParams;
    private final WindowManager mWindowManager;
    private EditText SearchKey;
    private final String[] bannedCat = {"facebook","youtube","購物","遊戲","Instagram","旅遊"};
    private final List<String> bannedBrowser = Arrays.asList(bannedCat);
    public WindowBannedBrowser(Context context){
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }
        // getting a LayoutInflater
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.activity_browser_search, null);

        // set onClickListener on the remove button, which removes
        // the view from the window
        mView.findViewById(R.id.btn_search).setOnClickListener(view -> {

            Log.v("shuffTest", "Pressed SEARCH");
            SearchKey = mView.findViewById(R.id.search_key);
            String url = "http://www.google.com/search?q = "+ SearchKey;

            Crawler crawler = new Crawler();
            String word = crawler.webGet(url);
            if(word.contains((CharSequence) bannedBrowser)){
              View  aView = layoutInflater.inflate(R.layout.activity_popup_message_browser, null);
                aView.findViewById(R.id.btn_yes).setOnClickListener(views -> {
                    close();
                    Log.v("shuffTest", "Pressed YES");
                    if(GeneralTimerActivity.getIsCounting()){
                        GeneralTimerActivity.getActivity().finishCounting();
                    } else{
                        TomatoClockActivity.getTomatoClockActivity().finishCounting();
                    }
                });

                aView.findViewById(R.id.btn_no).setOnClickListener(views-> {
                    close();
                    Log.v("shuffTest", "Pressed NO");
                    Intent intentHome;
                    if(GeneralTimerActivity.getIsCounting()){
                        intentHome = new Intent(context, GeneralTimerActivity.class);
                    } else{
                        intentHome = new Intent(context, TomatoClockActivity.class);
                    }
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentHome);
                });

                mView.findViewById(R.id.message_background).setOnClickListener(v -> {
                });


            }
            else{

            }



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

