package com.om.alarmwithlocationupdate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnStartAlarm, btnStopAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartAlarm = (Button) findViewById(R.id.btnStart);
        btnStopAlarm = (Button) findViewById(R.id.btnStop);



        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        btnStartAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, getPendingIntent());
            }
        });

        btnStopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmManager.cancel(getPendingIntent());
            }
        });
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
