package com.jutanium.tododrawer;

import android.app.Application;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.util.Log;

public class MyApp extends Application {
    //Singleton needed to allow NotificationHelper to create notifications
    public static MyApp instance;

    public MyApp() { instance = this; }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyApp", "gotCreated");
        //to fix a bug where the service would be created twice


    }

    public static MyApp getContext() { return instance; }
}
