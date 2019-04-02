package com.om.firebasejobdispatcherdemo;

import android.app.job.JobParameters;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyService extends JobService {
    BackgroundTask backgroundTask;
    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        backgroundTask = new BackgroundTask(){
            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(getApplicationContext(),"Message from Background task :"+s,Toast.LENGTH_SHORT).show();
               // jobFinished(job,true);
            }
        };
        backgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }

    public static class BackgroundTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            return "Hello from background job";
        }
    }
}
