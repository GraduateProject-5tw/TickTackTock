package com.GraduateProject.TimeManagementApp;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebBlock extends WebViewClient {


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Uri.parse(url).getHost().contains("www.youtube.com")) {
            return true;
        }
        // This is your not youtube site, so do not override; let your WebView load the page

        return false;
    }


}
