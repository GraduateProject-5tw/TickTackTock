package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private boolean ret;
    private final String[] bannedCat = {"facebook","購買","玩","instagram","遊戲","旅行","演唱","明星","play","song", "travel", "celebrity","漫畫", "anime", "角色","movie", "buy"};
    private final List<String> bannedBrowser = Arrays.asList(bannedCat);
    private static WebActivityBasic webPage;
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_basic);
        mWebView = findViewById(R.id.webview_show);
        alert_edit();
        webPage = this;
    }

    public static WebActivityBasic getWebPage(){
        return webPage;
    }

    public void alert_edit() {

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest webResourceRequest) { Callable<String> myCallable = () -> {
                Crawler crawler = new Crawler();
                url = webResourceRequest.getUrl().toString();
                Uri uri = Uri.parse(url);
                String key = uri.getQueryParameter("q");
                return crawler.webGet("https://tw.search.yahoo.com/search?p=" + key);
            };
            // 2.由上面的callable物件創建一個FutureTask物件
                FutureTask<String> oneTask = new FutureTask<>(myCallable);
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
                    return super.shouldOverrideUrlLoading(view, webResourceRequest);
                }
            }
        });
        String rootUrl = "https://www.google.com.tw/";
        mWebView.loadUrl(rootUrl);
    }

    public void bannedURL() {
        WindowBannedBrowser windowBannedBrowser = new WindowBannedBrowser(getApplicationContext());
        windowBannedBrowser.open();
    }

    public static String getUri(){
        return url;
    }

    private final WebChromeClient webChromeClient = new WebChromeClient() {
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
        mWebView.setOnKeyListener((v, keyCode, keyEvent) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) { //只處理一次
                    myLastUrl();
                }
                return true;
            }
            else{
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) { //只處理一次
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {  //當按back按紐時
        myOnclick();
    }

    @Override
    public void onPause() {
        super.onPause();
        finishAndRemoveTask();
        if(!WindowBannedBrowser.getSearch()){
            startForegroundService(new Intent(this,CheckFrontBrowser.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stopService(new Intent(this,CheckFrontBrowser.class));
    }

    /**
     * 拿到上一頁的路徑
     */
    protected void myLastUrl() {
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