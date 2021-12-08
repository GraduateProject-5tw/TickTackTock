package com.GraduateProject.TimeManagementApp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CheckNetwork {
    private final Context context;
    private final Thread loadingThread;
    private final Thread loadingThreadCustom;
    private final Thread loadingThreadDefault;
    private boolean exist;
    private static boolean networkStatus;

    // You need to pass the context when creating the class
    public CheckNetwork(Context context, Thread thread1, Thread thread2, Thread thread3, boolean exist) {

        this.context = context;
        loadingThread = thread1;
        loadingThreadCustom = thread2;
        loadingThreadDefault = thread3;
        this.exist = exist;
        networkStatus = false;
    }

    public static boolean isNetworkStatus() {
        return networkStatus;
    }

    // Network Check
    public void registerNetworkCallback()
    {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                                                                   @Override
                                                                   public void onAvailable(Network network) {
                                                                       networkStatus = true;
                                                                       Log.e("NETWORK", "available");
                                                                   }
                                                                   @Override
                                                                   public void onLost(Network network) {
                                                                       Log.e("NETWORK", "not available");
                                                                   }
                                                               }

            );
    }
}
