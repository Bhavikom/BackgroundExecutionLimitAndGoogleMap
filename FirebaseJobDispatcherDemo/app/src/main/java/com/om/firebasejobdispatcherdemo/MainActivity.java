package com.om.firebasejobdispatcherdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class MainActivity extends AppCompatActivity {

    Button btnStart,btnStop;
    private static final String Job_Tag = "my_job_tag";
    private FirebaseJobDispatcher jobDispatcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        btnStart = findViewById(R.id.start);
        btnStop = findViewById(R.id.stop);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
startJob();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
stopJob();
            }
        });
    }
    public void startJob(){
            Job job = jobDispatcher.newJobBuilder().
                    setService(MyService.class).
                    setLifetime(Lifetime.FOREVER).
                    setRecurring(true).
                    setTag(Job_Tag).
                    setTrigger(Trigger.executionWindow(10,15)).
                    setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).
                    setReplaceCurrent(false).
                    setConstraints(Constraint.ON_ANY_NETWORK)
                    .build();

            jobDispatcher.mustSchedule(job);

        Toast.makeText(this,"Job Scheduled : ",Toast.LENGTH_SHORT).show();
    }
    public void stopJob(){
        jobDispatcher.cancel(Job_Tag);
        Toast.makeText(this,"Job Canceled : ",Toast.LENGTH_SHORT).show();

    }


}
