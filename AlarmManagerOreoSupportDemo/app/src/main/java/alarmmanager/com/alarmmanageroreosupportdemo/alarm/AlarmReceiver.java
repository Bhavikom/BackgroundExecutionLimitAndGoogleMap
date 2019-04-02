package alarmmanager.com.alarmmanageroreosupportdemo.alarm;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import alarmmanager.com.alarmmanageroreosupportdemo.jobservice.JobScheduleService;

/**
 * Created by sonu on 09/04/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private NotificationManager alarmNotificationManager;
    MediaPlayer mp;
    private static int mJobId = 0;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        ComponentName componentName = new ComponentName(context, JobScheduleService.class);
        final JobInfo jobInfo = new JobInfo.Builder(++mJobId, componentName)
               .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(
                Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
        Toast.makeText(context, "New job scheduled with jobId: " + mJobId,
                Toast.LENGTH_SHORT).show();
    }
}
