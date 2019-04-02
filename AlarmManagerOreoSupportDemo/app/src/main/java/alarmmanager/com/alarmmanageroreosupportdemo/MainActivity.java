package alarmmanager.com.alarmmanageroreosupportdemo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import alarmmanager.com.alarmmanageroreosupportdemo.alarm.AlarmNotificationService;
import alarmmanager.com.alarmmanageroreosupportdemo.alarm.AlarmReceiver;
import alarmmanager.com.alarmmanageroreosupportdemo.alarm.AlarmSoundService;
import alarmmanager.com.alarmmanageroreosupportdemo.data.DBManager;
import alarmmanager.com.alarmmanageroreosupportdemo.data.Hero;
import alarmmanager.com.alarmmanageroreosupportdemo.data.ListViewAdapter;
import alarmmanager.com.alarmmanageroreosupportdemo.database.DatabaseClient;
import alarmmanager.com.alarmmanageroreosupportdemo.database.LocationPoint;
import alarmmanager.com.alarmmanageroreosupportdemo.jobservice.JobScheduleService;
import alarmmanager.com.alarmmanageroreosupportdemo.location.LocationResultHelper;

public class MainActivity extends AppCompatActivity implements  SharedPreferences.OnSharedPreferenceChangeListener{

    /* for location udpate */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    private IncomingMessageHandler mHandler;

    //Pending intent instance
    private PendingIntent pendingIntent;

    //Alarm Request Code
    private static final int ALARM_REQUEST_CODE = 133;

    Button btnSetAlarmAtSpecificTime, btnSetRepeatingAlarm;
    ListViewAdapter adapter;
    ListView listView;
    List<Hero> heroList;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new IncomingMessageHandler();
        requestPermissions();

        dbManager = new DBManager(this);
        dbManager.open();

        listView = (ListView) findViewById(R.id.listView);
        heroList = new ArrayList<>();

        new FetchRecordsFromDatabase().execute();

        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

        //Set On CLick over start alarm button
        btnSetAlarmAtSpecificTime = findViewById(R.id.btn_specific_alarm);
        btnSetAlarmAtSpecificTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startAlarmAtSpecifcTime();
                showTimePickerDialogAndSetAlarm();
            }
        });
        btnSetRepeatingAlarm = findViewById(R.id.btn_repeating_alarm);
        btnSetRepeatingAlarm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                    startAlarmRepeating();

            }
        });
        //set on click over stop alarm button
        findViewById(R.id.btn_stop_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop alarm manager
                stopAlarmManager();
            }
        });
    }
    //Trigger alarm manager with entered time interval
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAlarmRepeating() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT < Build.VERSION_CODES.KITKAT){
            manager.set(AlarmManager.RTC_WAKEUP, 60000, pendingIntent);
        }
        else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M) {
            //manager.setExact(AlarmManager.RTC_WAKEUP, alarmTriggerTime, pendingIntent);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),60000, pendingIntent);
        }
        else if (SDK_INT >= Build.VERSION_CODES.M) {
            //manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTriggerTime, pendingIntent);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),60000, pendingIntent);
            Toast.makeText(MainActivity.this,"Alarm will repeating every 1 minute : ",Toast.LENGTH_SHORT).show();
        }
    }
    //Stop/Cancel alarm manager
    public void stopAlarmManager() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);//cancel the alarm manager of the pending intent
        //Stop the Media Player Service to stop sound
        stopService(new Intent(MainActivity.this, AlarmSoundService.class));
        //remove the notification from notification tray
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AlarmNotificationService.NOTIFICATION_ID);

        Toast.makeText(this, "Alarm Canceled/Stop by User.", Toast.LENGTH_SHORT).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAlarmAtTime(long time,Calendar calendar) {
        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, AlarmReceiver.class);
        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        //setting the repeating alarm that will be fired every day
        am.setExactAndAllowWhileIdle(AlarmManager.RTC, time, pi); // for oreo version

        Date timeAlarm = calendar.getTime();

        Calendar calendarCurrentTime = Calendar.getInstance();
        Date timeCurrent = calendarCurrentTime.getTime();
        Toast.makeText(this, "Alarm will fire in duration : "+calculateTimeDifference(timeAlarm,timeCurrent), Toast.LENGTH_SHORT).show();
    }
    private void showTimePickerDialogAndSetAlarm(){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Calendar calendarAlarm = Calendar.getInstance();
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    calendarAlarm.set(calendarAlarm.get(Calendar.YEAR), calendarAlarm.get(Calendar.MONTH), calendarAlarm.get(Calendar.DAY_OF_MONTH),
                            timePicker.getHour(), timePicker.getMinute(), 0);
                } else {
                    calendarAlarm.set(calendarAlarm.get(Calendar.YEAR), calendarAlarm.get(Calendar.MONTH), calendarAlarm.get(Calendar.DAY_OF_MONTH),
                            timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                }
                //calendarAlarm.add(Calendar.HOUR,selectedHour);
                //calendarAlarm.add(Calendar.MINUTE,selectedMinute);
                setAlarmAtTime(calendarAlarm.getTimeInMillis(),calendarAlarm);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Alarm Time");
        mTimePicker.show();
    }
    private String calculateTimeDifference(Date date1,Date date2){
        long diff = date1.getTime() - date2.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if(hours > 0 ){
            return String.valueOf(hours+":"+minutes+":"+seconds);
        }else if(minutes > 0){
            return String.valueOf(minutes+":"+seconds);
        }else if(seconds > 0 ){
            return String.valueOf(seconds);
        }else {
            return null;
        }
    }
    private class FetchRecordsFromDatabase extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            //heroList =  viewModel.getLatestData();
            heroList = dbManager.fetch();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (heroList.size() > 0) {
                adapter = new ListViewAdapter(heroList, MainActivity.this);
                //adding the adapter to listview
                listView.setAdapter(adapter);
                //adapter.notifyDataSetChanged();
            }
            super.onPostExecute(s);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        /*Intent serviceIntent = new Intent(this, JobScheduleService.class);
        startService(serviceIntent);*/
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
            //textViewLocation.setText(LocationResultHelper.getSavedLocationResult(this));
            getLocationFromDatabase();
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(" ", "Displaying permission rationale to provide additional context.");
            // Request permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.i(" ", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(" ", "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(" ", "User interaction was cancelled.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // can be schedule in this way also
                //  Utils.scheduleJob(this, LocationUpdatesService.class);
                //doing this way to communicate via messenger
                // Start service and provide it a way to communicate with this class.
                /*Intent startServiceIntent = new Intent(this, JobScheduleService.class);
                Messenger messengerIncoming = new Messenger(mHandler);
                startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
                startService(startServiceIntent);*/
            } else {
                // Permission denied.
                finish();
            }
        }
    }

    class IncomingMessageHandler extends Handler {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            Log.i(" ", "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            switch (msg.what) {
                case JobScheduleService.LOCATION_MESSAGE:
                    /* getting message from job service on location update */
                    Location obj = (Location) msg.obj;
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    //locationMsg.setText("LAT :  " + obj.getLatitude() + "\nLNG : " + obj.getLongitude() + "\n\n" + obj.toString() + " \n\n\nLast updated- " + currentDateTimeString);
                    break;
            }
        }
    }
    private void getLocationFromDatabase() {
        class AsynctaskGetLocation extends AsyncTask<Void, Void, List<LocationPoint>> {

            @Override
            protected List<LocationPoint> doInBackground(Void... voids) {
                List<LocationPoint> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .locationDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<LocationPoint> locationPoints) {
                super.onPostExecute(locationPoints);


                Toast.makeText(MainActivity.this," location point size : "+locationPoints.size(),Toast.LENGTH_SHORT).show();
            }
        }
        AsynctaskGetLocation gt = new AsynctaskGetLocation();
        gt.execute();
    }
}
