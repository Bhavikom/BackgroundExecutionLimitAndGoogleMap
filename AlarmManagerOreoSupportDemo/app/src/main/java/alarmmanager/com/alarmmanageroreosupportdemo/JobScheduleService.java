package alarmmanager.com.alarmmanageroreosupportdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import alarmmanager.com.alarmmanageroreosupportdemo.data.DBManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobScheduleService extends JobService {

    public static String url = "https://us-central1-zoftino-stores.cloudfunctions.net/";
    DBManager dbManager;
    private NotificationManager alarmNotificationManager;
    MediaPlayer mediaPlayer;
    private static final String TAG = JobScheduleService.class.getSimpleName();
    private Messenger mActivityMessenger;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob");

        dbManager = new DBManager(this);
        dbManager.open();

        mediaPlayer=MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.start();
        sendNotification("Time is Up! Wake up!");

        CouponsAPI couponsAPI = new CouponsAPI(url, this,dbManager);
        try {
            couponsAPI.callService();
        }catch (Exception e){
            Log.e("refresh cpn work", "failed to refresh coupons");
        }


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob");
        return false;
    }
    private void sendNotification(String msg) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) System.currentTimeMillis();
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            alarmNotificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Alarm Done")
                .setContentIntent(resultPendingIntent)
                .setContentText(msg);

        alarmNotificationManager.notify(notificationId, mBuilder.build());

    }
}
