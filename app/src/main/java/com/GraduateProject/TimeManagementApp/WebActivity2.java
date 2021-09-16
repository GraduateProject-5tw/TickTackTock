package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class WebActivity2 extends AppCompatActivity {

    private WebView mWebView;
    private String rootUrl = "https://www.google.com.tw/";
    private String nowUrl;
    private String textt;
    private EditText result;
    private Crawler crawler;
    private boolean ret;
    private String[] split;
    private AlertDialog dialog;
    static Set<String> urls = new CopyOnWriteArraySet<String>();
    static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    static AtomicInteger count = new AtomicInteger(0);
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.web_banned, null);

        AlertDialog.Builder remindBuilder = new AlertDialog.Builder(WebActivity2.this);

        // set prompts.xml to alertdialog builder
        remindBuilder.setView(promptsView);
        remindBuilder.setCancelable(false);
        remindBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        remindBuilder.setNegativeButton("直接使用",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });


        //final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        remindBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                setContentView(R.layout.activity_web);
                                //mWebView = findViewById(R.id.webview_show);
                                setWebView(rootUrl);
                                myOnclick();


                                //result.setText(userInput.getText());
                                //textt = userInput.getText().toString();
                                split = textt.split(",");
                                //for (int i=0; i<split.length; i++)
                                //System.out.println(split[i]);

                                //System.out.println("Done");
                            }
                        })
                .setNegativeButton("直接使用",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = remindBuilder.create();

        // show it
        alertDialog.show();

        AlertDialog.Builder bannedAlert = new AlertDialog.Builder(WebActivity2.this);
        bannedAlert.setTitle("禁用網站");
        bannedAlert.setMessage("此網站不含您設定的關鍵字，請問要繼續使用嗎? /n/n 選取「確定」會停止計時");
        bannedAlert.setPositiveButton("確定", ((dialog, which) -> {
            if (GeneralTimerActivity.getIsCounting()) {
                GeneralTimerActivity.getActivity().finishCounting();
            } else {
                TomatoClockActivity.getTomatoClockActivity().finishCounting();
            }
            finish();
        }));
        bannedAlert.setNegativeButton("取消", ((dialog, which) -> {
            Intent intentHome = null;
            if(GeneralTimerActivity.getIsCounting()){
                intentHome = new Intent(getApplicationContext(), GeneralTimerActivity.class);
            } else{
                intentHome = new Intent(getApplicationContext(), TomatoClockActivity.class);
            }
            startActivity(intentHome);
            finish();
            dialog.dismiss();        }));
        dialog = remindBuilder.create();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v -> {

        }));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v -> {

        }));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


    }

    private void setWebView(String url) {
        //      對webView的設置
        WebSettings websettings = mWebView.getSettings();
        websettings.setSupportZoom(true);
        websettings.setBuiltInZoomControls(true);
        websettings.setDisplayZoomControls(false);
        websettings.setJavaScriptEnabled(true);
        websettings.setAppCacheEnabled(true);
        websettings.setSaveFormData(true);
        websettings.setAllowFileAccess(true);
        websettings.setDomStorageEnabled(true);
        /**
         * 拿到當前頁面的路徑
         */
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        //mWebView.loadUrl(url);
        ret = crawler.webGet(url, split);
        if (ret) {
            mWebView.loadUrl(url);
        } else {
            //TODO 跳出通知
            dialog.show();
        }
        //nowUrl = url;
    }

    private void myOnclick() {
//      監聽返回鍵
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) { //只處理一次
                        myLastUrl();
                    }
                    return true;
                }
                return false;
            }

        });
    }

    /**
     * 拿到上一頁的路徑
     */
    private void myLastUrl() {
        WebBackForwardList backForwardList = mWebView.copyBackForwardList();
        if (backForwardList != null && backForwardList.getSize() != 0) {
            //當前頁面在歷史佇列中的位置
            int currentIndex = backForwardList.getCurrentIndex();
            WebHistoryItem historyItem =
                    backForwardList.getItemAtIndex(currentIndex - 1);
            if (historyItem != null) {
                //String backPageUrl = historyItem.getUrl();
//                Logger.t("111").d("拿到返回上一頁的url"+backPageUrl);
                mWebView.goBack();
            }
        }
    }
}