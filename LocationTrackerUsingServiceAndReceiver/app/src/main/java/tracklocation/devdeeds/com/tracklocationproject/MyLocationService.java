package tracklocation.devdeeds.com.tracklocationproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

public class MyLocationService extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATE = "bhavik";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null){
                    Location location = result.getLastLocation();
                    String locationStgring = new StringBuilder(""+location.getLatitude()+" : "+location.getLongitude()).toString();

                    try {
                        MainActivity.getInstance().updateTextView(locationStgring);
                        Toast.makeText(context,locationStgring,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Toast.makeText(context,locationStgring,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }
}
