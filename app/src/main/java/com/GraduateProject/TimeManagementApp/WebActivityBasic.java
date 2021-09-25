package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AppCompatActivity;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class WebActivityBasic extends AppCompatActivity {

    private WebView mWebView;
    private final String rootUrl = "https://www.google.com.tw/";
    private String textt;
    private boolean ret;
    private String[] split;
    private final String[] bannedCat = {"facebook","購","玩","instagram","遊","旅","演","唱","play","song", "travel", "celebrity","漫畫", "anime", "角色","character"};
    private final List<String> bannedBrowser = Arrays.asList(bannedCat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_basic);
        mWebView = (WebView) findViewById(R.id.webview_show);
        alert_edit();
    }

    public void alert_edit() {
        final EditText et = new EditText(this);

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
            public boolean shouldOverrideUrlLoading(WebView view, String url) { Callable<String> myCallable = () -> {
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

                boolean banned = false;
                try {
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
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (!banned) {
                    Log.e("RESULT", "not include banned url");
                    ret = true;
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

    public void bannedURL() {
        WindowBannedBrowser windowBannedBrowser = new WindowBannedBrowser(getApplicationContext());
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


}