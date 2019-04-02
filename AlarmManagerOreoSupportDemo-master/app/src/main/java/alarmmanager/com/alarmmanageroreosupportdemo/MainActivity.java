package alarmmanager.com.alarmmanageroreosupportdemo;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import alarmmanager.com.alarmmanageroreosupportdemo.data.DBManager;
import alarmmanager.com.alarmmanageroreosupportdemo.data.Hero;
import alarmmanager.com.alarmmanageroreosupportdemo.data.ListViewAdapter;
import alarmmanager.com.alarmmanageroreosupportdemo.data.OffersViewModel;

public class MainActivity extends AppCompatActivity {

    //private OffersViewModel viewModel;
    //Pending intent instance
    private PendingIntent pendingIntent;

    private RadioButton secondsRadioButton, minutesRadioButton, hoursRadioButton;

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

        dbManager = new DBManager(this);
        dbManager.open();



        listView = (ListView) findViewById(R.id.listView);
        heroList = new ArrayList<>();

        new FetchRecords().execute();

        //viewModel = ViewModelProviders.of(this, new OffersViewModel.OffersViewModelFactory(this)).get(OffersViewModel.class);
        //adapter = new ListViewAdapter(heroList, MainActivity.this);
        //adding the adapter to listview
        //listView.setAdapter(adapter);

        secondsRadioButton = (RadioButton) findViewById(R.id.seconds_radio_button);
        minutesRadioButton = (RadioButton) findViewById(R.id.minutes_radio_button);
        hoursRadioButton = (RadioButton) findViewById(R.id.hours_radio_button);

        /* Retrieve a PendingIntent that will perform a broadcast */
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

        //Find id of Edit Text
        final EditText editText = (EditText) findViewById(R.id.input_interval_time);

        //Set On CLick over start alarm button
        btnSetAlarmAtSpecificTime = findViewById(R.id.start_at_specific_time);
        btnSetAlarmAtSpecificTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startAlarmAtSpecifcTime();
                showTimePickerDialogAndSetAlarm();
            }
        });
        btnSetRepeatingAlarm = findViewById(R.id.start_alarm_button);
        btnSetRepeatingAlarm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String getInterval = editText.getText().toString().trim();//get interval from edittext
                //check interval should not be empty and 0
                //if (!getInterval.equals("") && !getInterval.equals("0"))
                    //finally trigger alarm manager
                    startAlarmRepeating();

            }
        });
        //set on click over stop alarm button
        findViewById(R.id.stop_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop alarm manager
                stopAlarmManager();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, JobScheduleService.class);
        startService(serviceIntent);
    }
    //get time interval to trigger alarm manager
    private int getTimeInterval(String getInterval) {
        int interval = Integer.parseInt(getInterval);//convert string interval into integer
        //Return interval on basis of radio button selection
        if (secondsRadioButton.isChecked())
            return interval;
        if (minutesRadioButton.isChecked())
            return interval * 60;//convert minute into seconds
        if (hoursRadioButton.isChecked()) return interval * 60 * 60;//convert hours into seconds

        //else return 0
        return 0;
    }
    //Trigger alarm manager with entered time interval
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAlarmRepeating() {
        // get a Calendar object with current time
        //Calendar cal = Calendar.getInstance();
        // add alarmTriggerTime seconds to the calendar object
        //cal.add(Calendar.SECOND, alarmTriggerTime);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        //manager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);//set alarm manager with entered timer by converting into milliseconds
        /*manager.setInexactRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime() + 10000,
                10000, pendingIntent);*/
        //manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 20000, pendingIntent);

        //Toast.makeText(this, " Alarm Set for " + alarmTriggerTime + " seconds.", Toast.LENGTH_SHORT).show();

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
        //stopService(new Intent(MainActivity.this, AlarmSoundService.class));

        //remove the notification from notification tray
        /*NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AlarmNotificationService.NOTIFICATION_ID);

        Toast.makeText(this, "Alarm Canceled/Stop by User.", Toast.LENGTH_SHORT).show();*/
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
    private class FetchRecords extends AsyncTask<String, String, String> {
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

}
