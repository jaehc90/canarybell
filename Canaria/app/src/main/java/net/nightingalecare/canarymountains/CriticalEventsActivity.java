package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.Model.User;
import net.nightingalecare.canarymountains.adapter.SensorDataListAdapter;
import net.nightingalecare.canarymountains.utilities.DateUtil;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.apache.commons.lang.time.DateUtils;

/**
 * Created by jae on 8/21/14.
 */
public class CriticalEventsActivity extends Activity implements AdapterView.OnItemClickListener {

    ListView sensorDataListView;
    SensorDataListAdapter mSensorDataListAdapter;

    // Create a client to perform networking
    private static AsyncHttpClient client = null;

    public final static String SENSORTYPE_PARAM = "sensor_type";
    public final static String SENSORID_PARAM = "sensor_id";

    String[] timeStampData ={"2014-10-29 7:00:00","2014-10-29 7:30:00","2014-10-29 8:00:00","2014-10-29 9:00:00", "2014-10-29 12:00:00"};
    String[] dummyData ={"낙상하셨습니다.","낙상하셨습니다.","낙상하셨습니다.","낙상하셨습니다.", "낙상하셨습니다."};

    String WEARABLE_TYPE = "0";
    String PIR_TYPE = "1";
    String FALL_PIR_TYPE = "2";

    public CriticalEventsActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data);

        setupUI();

        // create listener
        sensorDataListView.setOnItemClickListener(this);

        TextView today = (TextView) findViewById(R.id.todayDate);
        SimpleDateFormat sdf = new SimpleDateFormat("M월 dd일 EEEE");
        String todayString = sdf.format(new Date());
        today.setText(todayString);
        today.requestFocus();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        String emergencyTime = mSensorDataListAdapter.getItem(position,"created");
        String id = String.valueOf(mSensorDataListAdapter.getItemId(position));
        String deleted = String.valueOf(mSensorDataListAdapter.getItem(position,"deleted"));

        if(deleted.equals("false"))
            updateEmergency(id, DateUtil.convertDateStr(emergencyTime, "yyyy-MM-dd HH:mm:ss", "dd일     HH시 mm분"));

    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setupSensorList();
        setupScreen();

        JSONArray array = getDummies(dummyData, timeStampData);
        mSensorDataListAdapter.updateData(array, SensorActivity.MAJOREVENT);

        getCriticalEvent();


    }

    private void setupScreen() {

        final Intent intent = getIntent();
        String type = intent.getStringExtra(CriticalEventsActivity.SENSORTYPE_PARAM);
        String sensorId = intent.getStringExtra(CriticalEventsActivity.SENSORID_PARAM);


    }

    private void displayData(int num) {

        ImageView image = (ImageView) findViewById(R.id.sensor_image);
        image.setImageResource(R.drawable.attention);
        TextView text1 = (TextView) findViewById(R.id.sensor_content);
        text1.setText("응급 상황");
        TextView text2 = (TextView) findViewById(R.id.sensor_number);
        text2.setText(String.valueOf(num) + "번 발생");

        setListViewHeightBasedOnChildren( (ListView) sensorDataListView);
    }

    private void getCriticalEvent() {

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();

        /*
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-1);
        Date yesterday = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)+2);
        Date tomorrow = cal.getTime();

        params.add("startTime", sdf.format(yesterday));
        params.add("endTime", sdf.format(tomorrow));
        */


        String url = NitingaleHttpRestClient.SUMMARY_URL;
        NTGClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(CriticalEventsActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                displayCriticalEvent(jsonObject);
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(CriticalEventsActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });


        /*
        String url = "sensors/critical.json";

        NTGClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(CriticalEventsActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                if (jsonObject.has("result")) {

                    try {
                        // String location = jsonObject.getJSONObject("result").getJSONObject("Sensor").getString("location"); //Todo: location update in the database
                        JSONArray array = jsonObject.getJSONArray("result");

                        mSensorDataListAdapter.updateData(array, SensorActivity.MAJOREVENT);
                        displayData();
                        // mSensorDataListAdapter.updateData(array, location);
                        // mSensorDataArray = array;

                        setListViewHeightBasedOnChildren( (ListView) sensorDataListView);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                //updateLastKnownActivity();
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(CriticalEventsActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });

        */

    }


    private void displayCriticalEvent(JSONObject jsonObject) {

        if (jsonObject.has("result")) {

            JSONArray object = null;
            try {

                JSONArray array = jsonObject.getJSONObject("result").getJSONArray("CriticalEvents");
                if(array.length() == 0) return;

                mSensorDataListAdapter.updateData(array, SensorActivity.MAJOREVENT);
                displayData(array.length());


                /*


                int numEventToHandle = 0;
                for (int i=0; i<array.length() ; i++){
                    event = array.optJSONObject(i);
                    boolean deleted = event.optBoolean("deleted");
                    if(!deleted) numEventToHandle ++;
                }

                // display the most recent event.
                event = array.getJSONObject(0);
                int type = event.getInt("type");
                String time = event.getString("created");
                if(type == 0)
                {
                    //updateEmergency(DateUtil.convertDateStr(time, "yyyy-MM-dd HH:mm:ss", "dd일     HH시 mm분"), true, numEventToHandle);
                }
                */

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private void setupSensorList() {

        /* ListView */
        sensorDataListView = (ListView) findViewById(R.id.sensor_data_listview);
        // View header = getLayoutInflater().inflate(R.layout.friends_header, null);
        // View footer = getLayoutInflater().inflate(R.layout.footer, null);
        //sensorDataListView.addHeaderView(header);

        // Create a JSONAdapter for the ListView; Set the ListView to use the ArrayAdapter
        mSensorDataListAdapter = new SensorDataListAdapter(this, getLayoutInflater());
        sensorDataListView.setAdapter(mSensorDataListAdapter);

    }

    public JSONArray getDummies(String [] dummyData, String [] timeStampData) {

        JSONArray dummies = new JSONArray();
        for(int i=0; i < dummyData.length; i++) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("detail",dummyData [i]);
                obj.put("created", timeStampData[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dummies.put(obj) ;
        }
        return dummies;

    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void updateEmergency(final String id, String timeStamp) {

        TextView view = (TextView) findViewById(R.id.majorevent_detail);
        TextView view2 = (TextView) findViewById(R.id.majorevent_level);
        LinearLayout view3 = (LinearLayout) findViewById(R.id.majorevent_layout);
        ImageView view4 = (ImageView) findViewById(R.id.majorevent_img);
        View button = findViewById(R.id.event_clear_button);
        View button1 = findViewById(R.id.call_button);

        view.setText("응급상황 낙상! 시간:" + timeStamp);
        view2.setText("응급");
        //int color = Integer.parseInt("FFFFFF",16);
        int color = Color.parseColor("#000000");
        view3.setBackgroundColor(color);

        Drawable drawable = getResources().getDrawable( R.drawable.majorevent );
        view4.setImageDrawable(drawable);
        view3.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            SharedPreferences sharedPreferences = getSharedPreferences (User.USER_DATA_PREFERENCE, Context.MODE_PRIVATE );
            String userId = sharedPreferences.getString("userId","");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("emergency", "");
            editor.commit();

            String url = "sensors/critical.json";
            RequestParams params = new RequestParams();
            params.add("criticalEvent", id);
            params.add("user",userId);

            Header headers[] = new Header[0] ;
            NitingaleHttpRestClient.getInstance().delete(CriticalEventsActivity.this.getApplicationContext(), url, headers, params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {

                        Toast.makeText(CriticalEventsActivity.this, "Success!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(CriticalEventsActivity.this, MyStatusActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                        Toast.makeText(CriticalEventsActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        // Log error message to help solve any problems
                            Log.e("Canaria", statusCode + " " + throwable.getMessage());
                        // update the view
                    }
                });


            }
        });

        button1.setVisibility(View.VISIBLE);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "01063606934"));
                startActivity(intent);

            }
        });
    }

    /*
    * GET /sensors/:id
    * */
/*
     private void getSensorData(String sensorId){

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        AsyncHttpClient client = NTGClient.getAsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("limit","10");
        String relativeUrl = "sensors/"+ sensorId + ".json";
        client.get(relativeUrl, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                Toast.makeText(SensorActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("nitingale", jsonObject.toString());
                if(jsonObject.has("result")) {
                    try {
                        updateSensorDataListAdapter(jsonObject.getJSONObject("result"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(SensorActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("nitingale", statusCode + " " + throwable.getMessage());
            }
        });

    }
*/
    /*
    *
    "success": true,
    "result": {
        "Sensor": {
            "id": "5",
            "owner": "8",
            "type": "0",
            "deviceName": "Fall PIR",
            "deviceAddress": "F5:A0:A3:A7:BB:4B",
            "location": "Wearable",
            "created": "2014-10-20 22:02:48",
            "modified": "2014-10-20 22:02:48"
        },
        "User": {
            "id": "8",
            "phone": "01011112223",
            "name": "user2",
            "gender": "MALE",
            "profileImage": "default.jpg",
            "created": "2014-10-20 18:36:52",
            "modified": "2014-10-20 18:36:52",
            "deviceType": null,
            "deviceToken": null
        },
        "SensorEvent": [],
        "CriticalEvent": []
    }
    * */



}

