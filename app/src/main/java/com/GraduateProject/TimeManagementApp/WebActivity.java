package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private Button edit;
    private String rootUrl = "https://www.google.com.tw/";
    private String textt;
    private boolean ret;
    private String[] split;
    private ImageButton btnPrev;
    private final String[] bannedCat = {"facebook","購","玩","instagram","遊","旅","演","唱","play","song", "travel", "celebrity","漫畫", "anime", "角色","character"};
    private final List<String> bannedBrowser = Arrays.asList(bannedCat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.webview_show);
        edit = findViewById(R.id.edit);
        alert_edit();

        edit.setOnClickListener(v -> alert_edit());
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);

        // 設定 ImageButton 元件 onClick 事件監聽器
        btnPrev.setOnClickListener(btnPrevListener);
    }

    public void alert_edit() {
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("添加允許搜尋的關鍵字: \n(請用逗號區隔單字)")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setCancelable(false)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下確定鍵後的事件
                        mWebView = findViewById(R.id.webview_show);
                        textt = et.getText().toString();
                        textt = textt.toLowerCase();
                        split = textt.split(",");
                        for (int j = 0; j < split.length; j++) {
                            System.out.println(split[j]);
                        }


                        //對webView的設置
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
                                Callable<String> myCallable = () -> {
                                    Crawler crawler = new Crawler();
                                    String words = crawler.webGet(url);
                                    return words;
                                };
                                // 2.由上面的callable物件創建一個FutureTask物件
                                FutureTask<String> oneTask = new FutureTask<String>(myCallable);
                                // 3.由FutureTask創建一個Thread物件
                                Thread t = new Thread(oneTask);
                                // 4.開啟執行緒
                                t.start();

                                outer:
                                for (String s : split) {
                                    boolean retval = false;
                                    boolean banned = false;
                                    try {
                                        retval = oneTask.get().contains(s);
                                        if (!retval) {
                                            Log.e("RESULT", "not allowed");
                                            ret = false;
                                            break;
                                        } else {
                                            for (int j = 0; j < bannedBrowser.size(); j++) {
                                                Log.e("RESULT", "checking " + bannedBrowser.get(j));
                                                banned = oneTask.get().contains(bannedBrowser.get(j));
                                                if (banned) {
                                                    Log.e("RESULT", "include banned url");
                                                    ret = false;
                                                    banned = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (ExecutionException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (retval && !banned) {
                                        Log.e("RESULT", "not include banned url");
                                        ret = true;
                                        //break outer;
                                    }
                                }
                                Log.i("ansen", "攔截url:" + url);
                                if (!ret) {
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
        WindowBannedBrowserClass windowBannedBrowser = new WindowBannedBrowserClass(getApplicationContext());
        windowBannedBrowser.open();
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

    //  btnPrev 按鈕的 onClick() 方法
    private ImageButton.OnClickListener btnPrevListener=new ImageButton.OnClickListener(){
        public void onClick(View v){
            startActivity(new Intent(WebActivity.this, GeneralTimerActivity.class));
        }
    };
}