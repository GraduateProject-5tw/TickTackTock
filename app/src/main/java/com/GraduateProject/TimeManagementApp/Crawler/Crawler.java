package com.GraduateProject.TimeManagementApp.Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Crawler {
    public String webGet(String url) {
        StringBuilder words = new StringBuilder();
        String title = "";
        String wordsToLowerCase;
        String userAgent = System.getProperty("http.agent");

        try {
            System.out.println("傳過去的URL： " + url);
            Document doc = Jsoup.connect(url).userAgent(userAgent).get();    //connect to the link
            Elements elements = doc.body().select("*");     //select all elements in the website's body
            for (Element ele : elements)//get all elements
                if (!ele.ownText().equals(""))              //if the text in a element is not ""
                    words.append(' ').append(ele.ownText());           //then add it to words
        } catch (IOException e) {
            e.printStackTrace();
        }
        wordsToLowerCase = words.substring(600, words.length()-1000);
        System.out.println("爬蟲結果fetch words from " + title + ':');
        System.out.println("Cr class:"+wordsToLowerCase);

        return wordsToLowerCase.toLowerCase();
    }
}
