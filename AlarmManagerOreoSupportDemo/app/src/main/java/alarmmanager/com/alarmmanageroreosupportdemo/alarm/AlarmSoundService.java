package alarmmanager.com.alarmmanageroreosupportdemo.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import alarmmanager.com.alarmmanageroreosupportdemo.R;

/**
 * Created by sonu on 10/04/17.
 */

public class AlarmSoundService extends Service {
    private MediaPlayer mediaPlayer;
    String url = "http://httpbin.org/get?param1=hello";
    private NotificationManager alarmNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Start media player
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.start();

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());

                        sendNotification(response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response",error.toString());
                        sendNotification(error.toString());
                    }
                }
        );

// add it to the RequestQueue
        queue.add(getRequest);
        // prepare the Request
        //mediaPlayer.setLooping(true);//set looping true to run it infinitely
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //On destory stop and release the media player
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
    private void sendNotification(String msg) {
        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
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
                .setContentTitle(" background done ")
                .setContentText(msg);
        alarmNotificationManager.notify(notificationId, mBuilder.build());

    }
}
