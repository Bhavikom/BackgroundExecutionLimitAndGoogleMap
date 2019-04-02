package alarmmanager.com.workmanagercustomdemo;

import android.content.Context;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import alarmmanager.com.workmanagercustomdemo.data.DBManager;
import alarmmanager.com.workmanagercustomdemo.data.Hero;
import alarmmanager.com.workmanagercustomdemo.data.LocalRepository;
import alarmmanager.com.workmanagercustomdemo.data.OfferDAO;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;


public class CouponsAPI {
    private static final String JSON_URL = "https://simplifiedcoding.net/demos/view-flipper/heroes.php";
    private String TAG = "CouponsAPI";
    private static final String couponService = "LatestCoupons";
    private String couponsServiceUrl = "";
    DBManager dbManager;
    private Context context;

    public CouponsAPI(String url, Context ctx, DBManager dbManager){
        couponsServiceUrl = url+couponService;
        context = ctx;
        this.dbManager = dbManager;

    }
    //get latest coupons from remote service using okhttp
    public void callService() {

        OkHttpClient httpClient = new OkHttpClient();

        HttpUrl.Builder httpBuilder =
                HttpUrl.parse(JSON_URL).newBuilder();

        okhttp3.Request request = new okhttp3.Request.Builder().
                url(httpBuilder.build()).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "failed to get coupons");
            }
            @Override public void onResponse(okhttp3.Call call, okhttp3.Response response){
                //hiding the progressbar after completion
                //progressBar.setVisibility(View.INVISIBLE);
                try {
                    String result = null;
                    try {
                        result = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //getting the whole json object from the response
                    JSONObject obj = new JSONObject(String.valueOf(result));
                    //we have the array named hero inside the object
                    //so here we are getting that json array
                    JSONArray heroArray = obj.getJSONArray("heroes");

                    //now looping through all the elements of the json array
                    for (int i = 0; i < heroArray.length(); i++) {
                        //getting the json object of the particular index inside the array
                        JSONObject heroObject = heroArray.getJSONObject(i);
                        //creating a hero object and giving them the values from json object
                        Hero hero = new Hero(heroObject.getString("name"), heroObject.getString("imageurl"));

                        OfferDAO offerDAO = LocalRepository.getOfferDatabase(context).offerDAO();
                        //offerDAO.deleteOffers();

                        offerDAO.insertLatestOffer(hero);

                        dbManager.insert(heroObject.getString("name"), heroObject.getString("imageurl"));

                        //adding the hero to herolist
                        // heroList.add(hero);
                    }
                    //creating custom adapter object
                    //ListViewAdapter adapter = new ListViewAdapter(heroList, getApplicationContext());

                    //adding the adapter to listview
                    //listView.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        /*StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        //progressBar.setVisibility(View.INVISIBLE);
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray heroArray = obj.getJSONArray("heroes");

                            //now looping through all the elements of the json array
                            for (int i = 0; i < heroArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject heroObject = heroArray.getJSONObject(i);
                                //creating a hero object and giving them the values from json object
                                Hero hero = new Hero(heroObject.getString("name"), heroObject.getString("imageurl"));

                                OfferDAO offerDAO = LocalRepository.getOfferDatabase(context).offerDAO();
                                offerDAO.deleteOffers();

                                offerDAO.insertLatestOffer(hero);

                                //adding the hero to herolist
                               // heroList.add(hero);
                            }
                            //creating custom adapter object
                            //ListViewAdapter adapter = new ListViewAdapter(heroList, getApplicationContext());

                            //adding the adapter to listview
                            //listView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        //adding the string request to request queue
        requestQueue.add(stringRequest);*/
        /*OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder httpBuilder = HttpUrl.parse(couponsServiceUrl).newBuilder();
        Request request = new Request.Builder().url(httpBuilder.build()).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "failed to get coupons");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                String resp = "";
                if (!response.isSuccessful()) {
                    Log.e(TAG, "failed response");
                }else {
                    try {
                        resp = responseBody.string();
                       // saveOffer(resp);
                    } catch (IOException e) {
                        Log.e(TAG, "failed to read response " + e);
                    }
                }
            }
        });*/
    }

    /*//save latest coupon to room db on the device
    private void saveOffer(String resp){
        OfferDAO offerDAO = LocalRepository.getOfferDatabase(context).offerDAO();
        offerDAO.deleteOffers();

        offerDAO.insertLatestOffer(prepareDataObj(resp));
    }

    private Hero prepareDataObj(String resp){
        String[] offer = resp.trim().split("\\|");
        return new Hero(offer[0], offer[1]);
    }*/
}
