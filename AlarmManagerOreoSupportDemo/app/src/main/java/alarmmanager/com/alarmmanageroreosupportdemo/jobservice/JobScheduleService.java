package alarmmanager.com.alarmmanageroreosupportdemo.jobservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alarmmanager.com.alarmmanageroreosupportdemo.MainActivity;
import alarmmanager.com.alarmmanageroreosupportdemo.R;
import alarmmanager.com.alarmmanageroreosupportdemo.api.CouponsAPI;
import alarmmanager.com.alarmmanageroreosupportdemo.data.DBManager;
import alarmmanager.com.alarmmanageroreosupportdemo.database.DatabaseClient;
import alarmmanager.com.alarmmanageroreosupportdemo.database.LocationPoint;
import alarmmanager.com.alarmmanageroreosupportdemo.location.LocationResultHelper;
import alarmmanager.com.alarmmanageroreosupportdemo.location.LocationUpdatesComponent;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobScheduleService extends JobService implements LocationUpdatesComponent.ILocationProvider{

    public static String url = "https://us-central1-zoftino-stores.cloudfunctions.net/";
    DBManager dbManager;
    private NotificationManager alarmNotificationManager;
    MediaPlayer mediaPlayer;
    private static final String TAG = JobScheduleService.class.getSimpleName();
    private Messenger mActivityMessenger;

    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    public static final int LOCATION_MESSAGE = 9999;
    private LocationUpdatesComponent locationUpdatesComponent;

    public JobScheduleService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Service started");
        if (intent != null) {
            mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        }
        //hey request for location updates
        locationUpdatesComponent.onStart();
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        Log.i(TAG, "created...............");
        locationUpdatesComponent = new LocationUpdatesComponent(this);
        locationUpdatesComponent.onCreate(this);
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
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob....");
        locationUpdatesComponent.onStop();
        return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationUpdate(Location location) {

        List<Location> locations = new ArrayList<>();
        locations.add(location);
        LocationResultHelper locationResultHelper = new LocationResultHelper(this, locations);
        locationResultHelper.saveResults();
        locationResultHelper.showNotification();
        final LocationPoint locationPoint = new LocationPoint();
        locationPoint.setLocation(location.getLatitude()+ " : "+location.getLongitude());

        for (int i = 0;i<locations.size();i++) {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            callVolley(this, String.valueOf(locations.get(i).getLatitude())+ " : "+String.valueOf(locations.get(i).getLongitude()) + " Time : "+currentDateTimeString);
        }

        //adding to database
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .locationDao()
                        .insert(locationPoint);
            }
        }).start();


        Toast.makeText(getApplicationContext()," Location is changed from alarm : "+location.getLatitude()+" : "+location.getLongitude(),Toast.LENGTH_SHORT).show();
        /* sending message to activity */
        sendMessage(LOCATION_MESSAGE, location);
        locationUpdatesComponent.onStop();
        locationUpdatesComponent = new LocationUpdatesComponent(this);
        locationUpdatesComponent.onCreate(this);

    }
    public void callVolley(final Context context, final String location){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String URL = "http://api.yasoka.com";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("gps", location);
            final String requestBody = jsonBody.toString();


            StringRequest strreq = new StringRequest(Request.Method.POST,
                    URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String Response) {
                            // get response
                            Toast.makeText(context.getApplicationContext()," location added in database : ",Toast.LENGTH_SHORT).show();
                            //jobService.jobFinished(jobParameters, false);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    e.printStackTrace();
                    // Toast.makeText(context.getApplicationContext()," location added in database : ",Toast.LENGTH_SHORT).show();
                }
            }){@Override
            public Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("gps", location);
                return params;
            }
            };


            requestQueue.add(strreq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy....");
    }

    /**
     * send message by using messenger
     *
     * @param messageID
     */
    private void sendMessage(int messageID, Location location) {
        // If this service is launched by the JobScheduler, there's no callback Messenger. It
        // only exists when the MainActivity calls startService() with the callback in the Intent.
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.obj = location;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }
}
