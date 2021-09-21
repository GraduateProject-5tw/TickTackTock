package com.GraduateProject.TimeManagementApp.Crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    public String webGet(String url) throws IOException {
        String words = "", title = "";
        String wordsToLowerCase = "";

        try {
            Document doc = Jsoup.connect(url).get();    //connect to the link
            Elements elements = doc.body().select("*");     //select all elements in the website's body
            for (Element ele : elements)//get all elements
                if (!ele.ownText().equals(""))              //if the text in a element is not ""
                    words += ' ' + ele.ownText();           //then add it to words
        } catch (IOException e) {
            e.printStackTrace();
        }
        wordsToLowerCase = words.substring(600, words.length()-1000);
        System.out.println("爬蟲結果fetch words from " + title + ':');
        System.out.println("Cr class:"+wordsToLowerCase);

        return wordsToLowerCase.toLowerCase();
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    private static String translate(String text) throws IOException {
        // INSERT YOU URL HERE

        String urlStr = "https://your.google.script.url" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + "en" +
                "&source=" + "zh-CN";
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
