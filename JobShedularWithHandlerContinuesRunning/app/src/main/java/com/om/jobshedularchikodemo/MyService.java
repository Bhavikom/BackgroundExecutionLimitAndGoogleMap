package com.om.jobshedularchikodemo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("NewApi")
public class MyService extends JobService {

    private Handler handler;
    int delay = 1000; //milliseconds

    @Override
    public void onCreate() {
        super.onCreate();

        /*if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }*/
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (handler == null)
            handler = new Handler();

        Toast.makeText(getApplicationContext()," job shedular running : ",Toast.LENGTH_SHORT).show();
        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                Log.d("Service_testing", "Running");
                handler.postDelayed(this, delay);
                Toast.makeText(getApplicationContext()," job shedular running : ",Toast.LENGTH_SHORT).show();
            }
        }, delay);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (handler == null)
            handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                Log.d("Service_testing", "Running");
                handler.postDelayed(this, delay);
            }
        }, delay);

        return START_STICKY;
    }*/
}
