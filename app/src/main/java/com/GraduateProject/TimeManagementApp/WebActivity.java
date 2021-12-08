package com.GraduateProject.TimeManagementApp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private final String rootUrl = "https://www.google.com.tw/";
    private String textt;
    private boolean ret;
    private boolean banned = false;
    private String[] split;
    private final String[] bannedCat = {"facebook","購買","玩","instagram","遊戲","旅行","演唱","明星","play","song", "travel", "celebrity","漫畫", "anime", "角色","movie", "buy"};
    private final List<String> bannedBrowser = Arrays.asList(bannedCat);
    private static WebActivity webPage;
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = findViewById(R.id.webview_show);
        alert_edit();

        Toolbar myToolbar = findViewById(R.id.key_toolbar);
        TextView add = findViewById(R.id.add_key);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        add.setOnClickListener(v -> alert_edit());
        webPage = this;
    }

    protected static WebActivity getWebPage(){
        return webPage;
    }

    protected static String getUrl(){
        return url;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishAndRemoveTask();
            startActivity(new Intent(WebActivity.this, GeneralTimerActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void alert_edit() {
        final Dialog add = new Dialog(this);
        add.requestWindowFeature(Window.FEATURE_NO_TITLE);
        add.setCanceledOnTouchOutside(true);
        add.setCancelable(true);
        add.setContentView(R.layout.activity_popup_edittext_onebutton);

        TextView title = add.findViewById(R.id.txt_tit);
        title.setText("添加允許搜尋的關鍵字: \n(請用逗號區隔單字)");

        EditText editText = add.findViewById(R.id.editText);
        add.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Button right = add.findViewById(R.id.btn_yes);
        right.setText("確 定");
        right.setOnClickListener(v1 -> {
            if (editText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "允許搜尋關鍵詞若為空白 \n 將無法進行搜尋", Toast.LENGTH_SHORT).show();
            } else {
                //按下確定鍵後的事件
                add.dismiss();
                mWebView = findViewById(R.id.webview_show);
                textt = editText.getText().toString();
                textt = textt.toLowerCase();
                split = textt.split(",");
                for (String s : split) {
                    System.out.println(s);
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

                        for (String s : split) {
                            boolean retval = false;
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
                            if(banned){
                                bannedURLPlay();
                            }
                            else{
                                bannedURL();
                            }
                            return true;//表示我已經處理過了
                        } else {
                            mWebView.loadUrl(url);
                            return super.shouldOverrideUrlLoading(view, webResourceRequest);
                        }
                    }
                });
                mWebView.loadUrl(rootUrl);
            }
        });
        add.show();
    }

    public void bannedURL() {
        WindowBannedBrowserClass windowBannedBrowser = new WindowBannedBrowserClass(getApplicationContext());
        windowBannedBrowser.open();
    }

    public void bannedURLPlay() {
        WindowBannedBrowser_extremeplay windowBanned = new WindowBannedBrowser_extremeplay(getApplicationContext());
        windowBanned.open();
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

    @Override
    public void onBackPressed() {  //當按back按紐時
        myOnclick();
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
            return false;
        });
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