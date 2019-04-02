package alarmmanager.com.workmanagercustomdemo;


import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import alarmmanager.com.workmanagercustomdemo.data.DBManager;
import alarmmanager.com.workmanagercustomdemo.data.Hero;
import alarmmanager.com.workmanagercustomdemo.data.OffersViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private OffersViewModel viewModel;
    Button btnSchedule;
    Button btnGetRecord;
    ListViewAdapter adapter;
    ListView listView;
    List<Hero> heroList;
    private DBManager dbManager;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this);
        dbManager.open();

        //initializing listview and hero list
        listView = (ListView) findViewById(R.id.listView);
        heroList = new ArrayList<>();

        viewModel = ViewModelProviders.of(this, new OffersViewModel.OffersViewModelFactory(this)).get(OffersViewModel.class);
        adapter = new ListViewAdapter(heroList, MainActivity.this);
        //adding the adapter to listview
        listView.setAdapter(adapter);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                heroList =  viewModel.getLatestData();
                if(heroList.size() > 0) {
                    adapter = new ListViewAdapter(heroList, MainActivity.this);
                    //adding the adapter to listview
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        }).start();*/

        /*viewModel.callService().observe(this, lcpn -> {
            if(lcpn != null) {
                heroList = lcpn;
            }
        });*/


        btnGetRecord=(Button)findViewById(R.id.get_record);
        btnGetRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new FetchRecords().execute();
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        heroList =  viewModel.getLatestData();
                    }
                }).start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(heroList.size() > 0) {
                            adapter = new ListViewAdapter(heroList, MainActivity.this);
                            //adding the adapter to listview
                            listView.setAdapter(adapter);
                            //adapter.notifyDataSetChanged();
                        }
                    }
                });*/

            }

        });
        btnSchedule=(Button)findViewById(R.id.schedule_periodic_job);
        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //define constraints
                /*CouponsAPI couponsAPI = new CouponsAPI(WorkService.url,MainActivity.this,dbManager);
                try {
                    couponsAPI.callService();
                }catch (Exception e){
                    Log.e("refresh cpn work", "failed to refresh coupons");
                }*/

                Constraints myConstraints = new Constraints.Builder()
                       // .setRequiresDeviceIdle(false)
                       // .setRequiresCharging(false)
                       // .setRequiredNetworkType(NetworkType.CONNECTED)
                       // .setRequiresBatteryNotLow(false)
                       // .setRequiresStorageNotLow(false)
                        .build();

                Data source = new Data.Builder()
                        .putString("workType", "PeriodicTime")
                        .build();

                PeriodicWorkRequest refreshCpnWork =
                        new PeriodicWorkRequest.Builder(WorkService.class, 15, TimeUnit.MINUTES)
                                .setConstraints(myConstraints)
                                .setInputData(source)
                                .build();

                WorkManager.getInstance().enqueue(refreshCpnWork);
            }
        });

    }
    private class FetchRecords extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            //heroList =  viewModel.getLatestData();
            heroList = dbManager.fetch();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(heroList.size() > 0) {
                adapter = new ListViewAdapter(heroList, MainActivity.this);
                //adding the adapter to listview
                listView.setAdapter(adapter);
                //adapter.notifyDataSetChanged();
            }
            super.onPostExecute(s);
        }
    }

}
