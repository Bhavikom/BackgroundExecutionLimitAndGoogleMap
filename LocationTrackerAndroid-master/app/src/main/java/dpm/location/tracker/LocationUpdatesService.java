package dpm.location.tracker;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
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

import dpm.location.database.DatabaseClient;
import dpm.location.database.LocationPoint;

import static dpm.location.tracker.JobServiceDemoActivity.MESSENGER_INTENT_KEY;


/**
 * location update service continues to running and getting location information
 */
public class LocationUpdatesService extends JobService implements LocationUpdatesComponent.ILocationProvider {

    private static final String TAG = LocationUpdatesService.class.getSimpleName();
    public static final int LOCATION_MESSAGE = 9999;

    private Messenger mActivityMessenger;

    private LocationUpdatesComponent locationUpdatesComponent;

    public LocationUpdatesService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob....");
//        Utils.scheduleJob(getApplicationContext(), LocationUpdatesService.class);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob....");

        locationUpdatesComponent.onStop();

        return false;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "created...............");

        locationUpdatesComponent = new LocationUpdatesComponent(this);

        locationUpdatesComponent.onCreate(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Service started");
        if (intent != null) {
            mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        }
        //hey request for location updates
        locationUpdatesComponent.onStart();

      /*  HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());*/

        //This thread is need to continue the service running
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    Log.i(TAG, "thread... is running...");
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/

        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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

        //adding to local database
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .locationDao()
                        .insert(locationPoint);
            }
        }).start();


        /* adding to server database using web api*/
        for (int i = 0;i<locations.size();i++) {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            callVolley(this, String.valueOf(locations.get(i).getLatitude())+ " : "+String.valueOf(locations.get(i).getLongitude()) + " Time : "+currentDateTimeString);
        }

        Toast.makeText(getApplicationContext()," Location is changed and stored in both database : "+location.getLatitude()+" : "+location.getLongitude(),Toast.LENGTH_SHORT).show();
        sendMessage(LOCATION_MESSAGE, location);
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
                    new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String Response) {
                            // get response
                            Toast.makeText(context.getApplicationContext()," location added in database : ",Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    e.printStackTrace();
                    Toast.makeText(context.getApplicationContext()," location added in database : ",Toast.LENGTH_SHORT).show();
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

}