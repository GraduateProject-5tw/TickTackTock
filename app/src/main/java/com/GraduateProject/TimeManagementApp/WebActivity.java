package com.GraduateProject.TimeManagementApp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.EditText;
import android.widget.Toast;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private String rootUrl = "https://www.google.com.tw/";
    private String textt;
    private boolean ret;
    private String[] split;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.webview_show);
        alert_edit();
    }

    public void alert_edit() {
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("添加允許搜尋的關鍵字: \n(請用逗號區隔單字)")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下確定鍵後的事件
                        mWebView = findViewById(R.id.webview_show);
                        textt = et.getText().toString();
                        split = textt.split(",");
                        for (int j = 0; j < split.length; j++) {
                            System.out.println(split[j]);
                        }


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

                        //mWebView.addJavascriptInterface(this,"android");
                        mWebView.setWebChromeClient(webChromeClient);

                        mWebView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Crawler crawler = new Crawler();
                                            ret = crawler.webGet(url, split);
                                            System.out.println(url);
                                            System.out.println("ret=" + ret);
                                            Log.i("ansen", "攔截url:" + url);

                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }).start();
                                if (ret == false) {
                                    bannedURL();
                                    return true;//表示我已經處理過了
                                } else {
                                    mWebView.loadUrl(url);
                                    return super.shouldOverrideUrlLoading(view, url);
                                }
                            }
                        });
                        mWebView.loadUrl(rootUrl);
                    }

                }).setNegativeButton("取消", null).show();
    }

    public void bannedURL() {
        AlertDialog.Builder bannedUrl = new AlertDialog.Builder(this);
        bannedUrl.setTitle("禁用網站")
                .setMessage("此網站不含您設定的關鍵字，確定要使用嗎? \n 選取「確定」會停止計時")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (GeneralTimerActivity.getIsCounting()) {
                            GeneralTimerActivity.getActivity().finishCounting();
                        } else {
                            TomatoClockActivity.getTomatoClockActivity().finishCounting();
                        }
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intentHome = null;
                        if (GeneralTimerActivity.getIsCounting()) {
                            intentHome = new Intent(getApplicationContext(), GeneralTimerActivity.class);
                        } else {
                            intentHome = new Intent(getApplicationContext(), TomatoClockActivity.class);
                        }
                        startActivity(intentHome);
                        finish();
                    }
                })
                .show();
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("確定", null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();
            result.confirm();
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };

    @JavascriptInterface
    public void getClient(String str) {
        Log.i("ansen", "html調用用戶端:" + str);
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
