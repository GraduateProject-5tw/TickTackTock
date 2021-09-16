package com.GraduateProject.TimeManagementApp;

import android.util.Log;

import com.GraduateProject.TimeManagementApp.Crawler.Crawler;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Web {  //暫時存放
    private static String urll;
    private static String[] split;
    //private boolean ret;
    public boolean bannedContains(){
        Callable<String> myCallable = new Callable() {
            @Override
            public Boolean call() {
                Crawler crawler = new Crawler();
                boolean ret = crawler.webGet(urll, split);
                System.out.println(urll);
                System.out.println("ret=" + ret);
                Log.i("ansen", "攔截url:" + urll);

                return null;
            }
        };
        // 2.由上面的callable对象创建一个FutureTask对象
        FutureTask<String> oneTask = new FutureTask<String>(myCallable);
        // 3.由FutureTask创建一个Thread对象
        Thread t = new Thread(oneTask);
        // 4.开启线程
        t.start();
        return true;
    }

    public static void setUrl(String url){
        urll = url;
    }
    public static void setVocabuary(String[] spli){ split = spli; }
    //public static int getIsCustom(){ return isCustom; }

}
