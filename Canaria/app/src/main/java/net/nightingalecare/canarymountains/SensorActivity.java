package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.adapter.SensorDataListAdapter;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import org.apache.commons.lang.time.DateUtils;

/**
 * Created by jae on 8/21/14.
 */
public class SensorActivity extends Activity implements AdapterView.OnItemClickListener {

    private EditText mainEditText;
    private Button searchButton;

    ListView sensorDataListView;
    SensorDataListAdapter mSensorDataListAdapter;

    JSONArray mSensorDataArray;

    // Create a client to perform networking
    private static AsyncHttpClient client = null;

    public final static String SENSORTYPE_PARAM = "sensor_type";
    public final static String SENSORID_PARAM = "sensor_id";

    public final static String MAJOREVENT = "majorevent";
    public final static String GOING_OUT = "goingout";
    public final static String EATING = "eating";
    public final static String SLEEP = "sleep";
    public final static String BATHROOM = "bathroom";
    public final static String WEARABLE = "wearable";
    public final static String PEDO = "pedometer";

    private static final String Server_URL_Home = "http://128.199.145.40/advise/";
    private static final String IMG_URL_Home = "hhttp://128.199.145.40/advise/app/webroot/img/";
    String[] categoryName = {"기타 응급/사고 상황","재난","분실","실종","반려동물", "Wrong Category"};

    String[] dummyEatingData ={"냉장고 문을 여셨습니다.","냉장고 문을 여셨습니다.","냉장고 문을 여셨습니다.","밥솥을 여셨습니다.", "밥솥을 여셨습니다."};
    String[] dummyBathroomData ={"화장실을 사용하였습니다.","화장실을 사용하였습니다.","화장실을 사용하였습니다.","화장실을 사용하였습니다.", "화장실을 사용하였습니다."};
    String[] dummyDoorData ={"문을 사용하셨습니다","문을 사용하셨습니다.","문을 사용하셨습니다.","문을 사용하셨습니다.",  "문을 사용하셨습니다."};
    String[] dummyData ={"냉장고 문을 여셨습니다.","냉장고 문을 여셨습니다.","냉장고 문을 여셨습니다.","밥솥을 여셨습니다.", "밥솥을 여셨습니다."};
    String[] timeStampData ={"2014-10-29 7:00:00","2014-10-29 7:30:00","2014-10-29 8:00:00","2014-10-29 9:00:00", "2014-10-29 12:00:00"};

    String WEARABLE_TYPE = "0";
    String PIR_TYPE = "1";
    String FALL_PIR_TYPE = "2";
            
    public SensorActivity(){}

    /*
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NitingaleMessage.CREATED:
                    String id = (String) msg.obj;
                    OK(id);
                    break;
                case NitingaleMessage.FAILED:
                    Fail();
                    break;
                default:
                    break;
            }
        }
    };
    */

    private void Fail() {
        Toast.makeText(this, "get notification fail", Toast.LENGTH_LONG).show();
    }

    private void OK(String id) {
        Toast.makeText(this, id + "  OK", Toast.LENGTH_LONG).show();
    }

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


        // 12. Now that the user's chosen a book, grab the cover data
        Object item = mSensorDataListAdapter.getItem(position);
        /*
        String image = null;
        try {
            image = sensorDataListAdapter.getImageName(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        // create an Intent to take you over to a new DetailActivity
        //Intent friendIntent = new Intent(this, FriendActivity.class);

        // pack away the data about the cover
        // into your Intent before you head out
        // friendIntent.putExtra("image", image);

        // start the next Activity using your prepared Intent
        // startActivity(detailIntent);

    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setupSensorList();
        setupScreen();


    }

    private void setupScreen() {
        final Intent intent = getIntent();
        String type = intent.getStringExtra(SensorActivity.SENSORTYPE_PARAM);
        String sensorId = intent.getStringExtra(SensorActivity.SENSORID_PARAM);

        getSensorData(sensorId);


    }

    private void displayData(String type) {

        if(type.equals(MAJOREVENT))
        {
            ImageView image = (ImageView) findViewById(R.id.sensor_image);
            image.setImageResource(R.drawable.attention);
            TextView text1 = (TextView) findViewById(R.id.sensor_content);
            text1.setText("응급 상황");
            TextView text2 = (TextView) findViewById(R.id.sensor_number);
            text2.setText("");
            // getFriends();
            // JSONArray dummyarray = new JSONArray();
            // mSensorDataListAdapter.updateData(dummyarray,MAJOREVENT );

            // get stored last known activity and number of steps
            SharedPreferences sharedPreferences = getSharedPreferences ("Data", Context.MODE_PRIVATE );
            String emergencyTime = sharedPreferences.getString("emergency", "");

            if(!emergencyTime.equals("")) {updateEmergency(emergencyTime);}
        }
        if(type.equals(SLEEP))
        {
            ImageView image = (ImageView) findViewById(R.id.sensor_image);
            image.setImageResource(R.drawable.sleepbig);
            TextView text = (TextView) findViewById(R.id.sensor_content);
            text.setText("수면");
            TextView text2 = (TextView) findViewById(R.id.sensor_number);
            text2.setText("8시간");

            // JSONArray dummies = new JSONArray();
            // mSensorDataListAdapter.updateData(dummies, SLEEP);
        }

        if(type.equals(EATING))
        {
            ImageView image = (ImageView) findViewById(R.id.sensor_image);
            image.setImageResource(R.drawable.eatingref_big);
            TextView text = (TextView) findViewById(R.id.sensor_content);
            text.setText("냉장고 및 밥솥");
            TextView text2 = (TextView) findViewById(R.id.sensor_number);
            text2.setText("" + dummyEatingData.length + "번");

            // JSONArray dummies = getDummies(dummyEatingData, timeStampData);
            // mSensorDataListAdapter.updateData(dummies, EATING);
        }
        if(type.equals(BATHROOM))
        {
            ImageView image = (ImageView) findViewById(R.id.sensor_image);
            image.setImageResource(R.drawable.bathroombig);
            TextView text = (TextView) findViewById(R.id.sensor_content);
            text.setText("화장실");
            TextView text2 = (TextView) findViewById(R.id.sensor_number);
            text2.setText("" + mSensorDataListAdapter.getCount() + "번");

            // JSONArray dummies = getDummies(dummyBathroomData, timeStampData);
            // mSensorDataListAdapter.updateData(dummies, BATHROOM);
        }
        if(type.equals(GOING_OUT))
        {
            ImageView image = (ImageView) findViewById(R.id.sensor_image);
            image.setImageResource(R.drawable.gointoutbig);
            TextView text = (TextView) findViewById(R.id.sensor_content);
            text.setText("외출문");
            TextView text2 = (TextView) findViewById(R.id.sensor_number);
            text2.setText("" + dummyDoorData.length + "번");

            JSONArray dummies = getDummies(dummyDoorData, timeStampData);
            mSensorDataListAdapter.updateData(dummies, GOING_OUT);
        }

    }

    private void getSensorData(String id) {

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();

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

        String url = "sensors/" + id + ".json";

        NTGClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(SensorActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                if (jsonObject.has("result")) {

                    try {
                        String location = jsonObject.getJSONObject("result").getJSONObject("Sensor").getString("location"); //Todo: location update in the database
                        JSONArray array = jsonObject.getJSONObject("result").getJSONArray("SensorEvent");

                        mSensorDataListAdapter.updateData(array, location);
                        displayData(location);
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

                Toast.makeText(SensorActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });


    }

    private void setupSensorList() {

        /* ListView */
        // mainEditText = (EditText) V.findViewById(R.id.title);
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

/*
    private void getFriends(){

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        AsyncHttpClient client = NTGClient.getAsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("limit","10");
        String url = Server_URL_Home + "notifications.json";
        Log.d("nitingale: GET", url);

        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                // Display a "Toast" message
                Toast.makeText(NotificationActivity.this, "Success!", Toast.LENGTH_LONG).show();
                // For now, just log results
                Log.d("nitingale", jsonObject.toString());
                // update the view by updating adapter
                if(jsonObject.has("result")) {
                    mJSONNotificationListAdapter.updateData(jsonObject.optJSONArray("result"));
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                // Display a "Toast" message
                // to announce the failure
                Toast.makeText(NotificationActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("nitingale", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });

    }

    private void initClient(){
        if(client == null){
            client =  new AsyncHttpClient();
        }
    }
*/

    private void updateEmergency(String now) {

        TextView view = (TextView) findViewById(R.id.majorevent_detail);
        TextView view2 = (TextView) findViewById(R.id.majorevent_level);
        LinearLayout view3 = (LinearLayout) findViewById(R.id.majorevent_layout);
        ImageView view4 = (ImageView) findViewById(R.id.majorevent_img);
        View button = findViewById(R.id.event_clear_button);

        view.setText("응급상황 낙상! 시간:" + now);
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

                SharedPreferences sharedPreferences = getSharedPreferences ("Data", Context.MODE_PRIVATE );
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("emergency", "");
                editor.commit();

                Intent intent = new Intent(SensorActivity.this, MyStatusActivity.class);
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

