package com.GraduateProject.TimeManagementApp.Crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    public boolean webGet(String url, String[] split) {
        String words = "", title = "";
        boolean ret = false;
        try {
            Document doc = Jsoup.connect(url).get();    //connect to the link
            Elements elements = doc.body().select("*");     //select all elements in the website's body
            title = doc.title();                            //get the website's title
            for (Element ele : elements)                    //get all elements
                if (!ele.ownText().equals(""))              //if the text in a element is not ""
                    words += ' ' + ele.ownText();           //then add it to words
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("爬蟲結果fetch words from " + title + ':');
        System.out.println(words);
        outer:
        for (int i = 0; i < split.length; i++) {
            boolean retval = words.contains(split[i]);
            if (retval) {
                ret = true;
                break outer;
            }
        }
        return ret;
    }
}
