package com.om.jobshedularchikodemo;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mJobScheduler;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitt);

        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder( 1,
                    new ComponentName( getPackageName(), MyService.class.getName() ) );

            builder.setMinimumLatency(3 * 1000); // wait at least
            builder.setOverrideDeadline(3 * 1000); // maximum delay
            mJobScheduler.schedule( builder.build());


        } else {
            //TODO("VERSION.SDK_INT < LOLLIPOP")
        }
    }
}
