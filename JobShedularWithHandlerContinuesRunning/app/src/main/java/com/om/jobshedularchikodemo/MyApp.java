package com.om.jobshedularchikodemo;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MyService.class));
        } else {
            startService(new Intent(this, MyService.class));
        }*/
//        startS ervice(new Intent(this, MyService.class));
    }
}
