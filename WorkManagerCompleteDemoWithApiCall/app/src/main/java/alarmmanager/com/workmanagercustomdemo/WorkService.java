package alarmmanager.com.workmanagercustomdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import alarmmanager.com.workmanagercustomdemo.data.DBManager;
import alarmmanager.com.workmanagercustomdemo.data.DatabaseHelper;
import androidx.work.Data;
import androidx.work.Worker;

public class WorkService extends Worker {

    public static String url = "https://us-central1-zoftino-stores.cloudfunctions.net/";
    DBManager dbManager;
    @NonNull
    @Override
    public Worker.Result doWork() {


        //read input argument
        String workType = getInputData().getString("workType");
        Log.i("refresh cpn work", "type of work request: "+workType);

        Context context = getApplicationContext();

        dbManager = new DBManager(context);
        dbManager.open();
        //get coupon and update local db using okhttp and room
        //run on a separate thread
        CouponsAPI couponsAPI = new CouponsAPI(url, context,dbManager);
        try {
            couponsAPI.callService();
        }catch (Exception e){
            Log.e("refresh cpn work", "failed to refresh coupons");
        }

        //sending data to the caller
        Data refreshTime = new Data.Builder()
                .putString("refreshTime", ""+System.currentTimeMillis())
                .build();
        setOutputData(refreshTime);

        //sending work status to caller
        return Worker.Result.SUCCESS;
    }

}
