package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.Model.CriticalEvents;
import net.nightingalecare.canarymountains.Model.SensorData;
import net.nightingalecare.canarymountains.adapter.LeDeviceListAdapter;
import net.nightingalecare.canarymountains.fragments.BaseFragment;
import net.nightingalecare.canarymountains.fragments.SimplePhotoGalleryListFragment;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;
import net.nightingalecare.canarymountains.utilities.BluetoothLeService;
import net.nightingalecare.canarymountains.utilities.DateUtil;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;
import net.nightingalecare.canarymountains.utilities.SampleGattAttributes;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MyStatusActivity extends Activity implements BaseFragment.OnFragmentInteractionListener {


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String TAG = "CANARIA";

    public final static UUID UUID_BATTER_READ =
            UUID.fromString(SampleGattAttributes.BATTER_READ);
    public final static UUID UUID_BATTERY_SERVICE =
            UUID.fromString(SampleGattAttributes.BATTERY_SERVICE);

    public final static UUID UUID_PEDOMETER_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.PEDOMETER_MEASUREMENT);

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private SensorData mSensorData;
    private CriticalEvents mCriticalEvents;

    BluetoothGattCharacteristic pedoMeasurementCharacteristic, pedoTimeSetCharacteristic, batteryReadCharacteristic, fallDetectCharacteristic ;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    LeDeviceListAdapter leDevices;

    private String mNextDeviceAddress;

    LinearLayout container;
    TextView todayView;
    TextView todayStatusView;
    TextView connectionStateView;
    ImageView contactView;
    TextView activityTimeStampView;

    View majorevent;
    View eating;
    View goingout;
    View sleep;
    View bathroom;

    static private int pedo_stepcount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_my_status3);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        todayView = (TextView) findViewById(R.id.todayDate);
        SimpleDateFormat sdf = new SimpleDateFormat("M월 dd일 EEEE");
        String todayString = sdf.format(new Date());
        todayView.setText(todayString);
        todayStatusView = (TextView)findViewById(R.id.todayStatus);
        activityTimeStampView = (TextView)findViewById(R.id.activityTimestamp);
        connectionStateView = (TextView) findViewById(R.id.connection_state);
        connectionStateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if(mBluetoothLeService != null) mBluetoothLeService.disconnect();
                // Intent intent = new Intent(this, this.getClass());

                try {
                    // thread to sleep for 1000 milliseconds
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e);
                }

                final Intent intent = new Intent(MyStatusActivity.this, MyStatusActivity.class);
                intent.putExtra(MyStatusActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(MyStatusActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);

                startActivity(intent);
            }
        });

        majorevent = layoutInflater.inflate(R.layout.layout_majorevent, null);
        eating = layoutInflater.inflate(R.layout.layout_eating, null);
        goingout = layoutInflater.inflate(R.layout.layout_goingout, null);
        sleep = layoutInflater.inflate(R.layout.layout_sleep, null);
        bathroom = layoutInflater.inflate(R.layout.layout_bathroom, null);

        container = (LinearLayout)findViewById(R.id.container0);
        container.addView(majorevent);
        container.addView(eating);
        container.addView(goingout);
        container.addView(sleep);
        container.addView(bathroom);

        majorevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(MyStatusActivity.this, CriticalEventsActivity.class);
                intent.putExtra(SensorActivity.SENSORTYPE_PARAM, SensorActivity.MAJOREVENT);

                startActivity(intent);

            }
        });

        eating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(MyStatusActivity.this, SensorActivity.class);
                intent.putExtra(SensorActivity.SENSORTYPE_PARAM, SensorActivity.EATING);
                startActivity(intent);
            }
        });

        goingout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(MyStatusActivity.this, SensorActivity.class);
                intent.putExtra(SensorActivity.SENSORTYPE_PARAM, SensorActivity.GOING_OUT);
                startActivity(intent);
            }
        });

        bathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(MyStatusActivity.this, SensorActivity.class);
                intent.putExtra(SensorActivity.SENSORTYPE_PARAM, SensorActivity.BATHROOM);
                intent.putExtra(SensorActivity.SENSORID_PARAM, "5");

                startActivity(intent);
            }
        });

        sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(MyStatusActivity.this, SensorActivity.class);
                intent.putExtra(SensorActivity.SENSORTYPE_PARAM, SensorActivity.SLEEP);
                startActivity(intent);
            }
        });


        contactView = (ImageView) findViewById(R.id.contact_button);
        contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyStatusActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        BaseFragment targetFragment1 = null;
        targetFragment1 = SimplePhotoGalleryListFragment.newInstance();

        // Select the fragment.
        fragmentManager.beginTransaction()
                .replace(R.id.frag_container1, targetFragment1)
                .commit();

        getDailyEventSummary();
        getTodayStatus();

            // Select the fragment.
/*
        BaseFragment targetFragment2 = null;
        targetFragment2 = SimplePhotoGalleryListFragment.newInstance();

        fragmentManager.beginTransaction()
                .replace(R.id.frag_container2, targetFragment2)
                .commit();
*/

    }

    private void getTodayStatus() {


        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        params.add("startTime", sdf.format(new Date()));
        String url = NitingaleHttpRestClient.SUMMARY_URL;

        NTGClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(MyStatusActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                if (jsonObject.has("result")) {
                    JSONArray object = null;
                    try {

                        JSONArray array = jsonObject.getJSONObject("result").getJSONArray("Sensors");
                        for(int i=0; i<array.length(); i++)
                        {
                            JSONObject sensorData = array.getJSONObject(i);
                            if( "F9:91:29:54:DF:E9".equals(sensorData.getString("deviceAddress"))) {
                                JSONArray jsonArray = sensorData.getJSONArray(SensorData.DEVICE_SUMMARY_INDEX);
                                if (jsonArray.length() == 0) return;
                                JSONObject object2 = jsonArray.getJSONObject(0);
                                int stepnum = object2.getInt("maxCumulativeValue");
                                if (stepnum != 0) {
                                    todayStatusView.setText("" + (stepnum / 3) + "m     " + stepnum + "보");
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                //updateLastKnownActivity();
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(MyStatusActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });


    }

    private void getDailyEventSummary() {

        // get stored last known activity and number of steps
        SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
        String activity = sharedPreferences.getString("LastKnownActivity", "");
        String init_steps = sharedPreferences.getString("totalStep", "4000");
        String emergencyTime = sharedPreferences.getString("emergency", "");

        if(!activity.equals("")) { activityTimeStampView.setText(activity);}

        int stepnum;

        if(pedo_stepcount == 0) {
            stepnum = Integer.parseInt(init_steps);
        } else {
            stepnum = pedo_stepcount;
        }

        todayStatusView.setText("" + (stepnum / 3) + "m     " + stepnum + "보");
        renderGraph(stepnum);

        if(!emergencyTime.equals("")) {updateEmergency(emergencyTime, true);}

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        String url = NitingaleHttpRestClient.SUMMARY_URL;
        NTGClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(MyStatusActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                mSensorData = new SensorData(jsonObject);
                mCriticalEvents = new CriticalEvents(jsonObject);

                try {
                    BluetoothDevices.getBluetoothDevices().initFromDB(jsonObject.getJSONObject("result"));
                    setUPBle();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayLastKnownActivity(jsonObject);
                displaySensorData();
                // todo:
                displayCriticalEvent(jsonObject);
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(MyStatusActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });

    }


    private void displayCriticalEvent(JSONObject jsonObject) {

        if (jsonObject.has("result")) {
            JSONArray object = null;
            try {

                JSONArray array = jsonObject.getJSONObject("result").getJSONArray("CriticalEvents");
                if(array.length() == 0) return;
                JSONObject event;
                JSONObject mostRecentEvent = null;

                int numEventToHandle = 0;
                for (int i=0; i<array.length() ; i++){
                   event = array.optJSONObject(i);
                   boolean deleted = event.optBoolean("deleted");
                   if(!deleted)
                   {
                       numEventToHandle ++;
                       if(mostRecentEvent == null) mostRecentEvent = event;
                   }
                }

                // display the most recent event.
                int type = mostRecentEvent.getInt("type");
                String time = mostRecentEvent.getString("created");
                if(type == 0)
                {
                    updateEmergency(DateUtil.convertDateStr(time, "yyyy-MM-dd HH:mm:ss", "dd일     HH시 mm분"), true, numEventToHandle);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private void displaySensorData() {


        /*
        if (jsonObject.has("result")) {
            JSONArray object = null;
            try {

                String timeStr = jsonObject.getJSONObject("result").getJSONArray("Sensors").getJSONArray("lastKnownEventTime");dfdfdfdf
                String timeStamp = DateUtil.convertDateStr(timeStr, "yyyy-MM-dd HH:mm:ss", "M월 dd일 HH시 mm분");
                activityTimeStampView.setText(timeStamp);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        */


    }



    private void displayLastKnownActivity(JSONObject jsonObject) {

        if (jsonObject.has("result")) {
            JSONArray object = null;
            try {

                String timeStr = jsonObject.getJSONObject("result").getJSONObject("LastKnownEvent").getString("lastKnownEventTime");
                String timeStamp = DateUtil.convertDateStr(timeStr, "yyyy-MM-dd HH:mm:ss", "M월 dd일 HH시 mm분");
                activityTimeStampView.setText(timeStamp);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void renderGraph(int stepnum) {

        int goal = stepnum * 100 / 5000;
        ImageView image = (ImageView) findViewById(R.id.activity_graph);
        Drawable drawable;
        if(goal > 95)
        {
            drawable = getResources().getDrawable( R.drawable.graph100 );
            image.setImageDrawable(drawable);
        } else if(goal > 90)
        {
            drawable = getResources().getDrawable( R.drawable.graph95 );
            image.setImageDrawable(drawable);
        } else if(goal > 85)
        {
            drawable = getResources().getDrawable( R.drawable.graph90 );
            image.setImageDrawable(drawable);
        } else if(goal > 80)
        {
            drawable = getResources().getDrawable( R.drawable.graph85 );
            image.setImageDrawable(drawable);
        } else if(goal > 75)
        {
            drawable = getResources().getDrawable( R.drawable.graph80 );
            image.setImageDrawable(drawable);
        } else if(goal > 70)
        {
            drawable = getResources().getDrawable( R.drawable.graph75 );
            image.setImageDrawable(drawable);
        } else if(goal > 65)
        {
            drawable = getResources().getDrawable( R.drawable.graph70 );
            image.setImageDrawable(drawable);
        } else if(goal > 60)
        {
            drawable = getResources().getDrawable( R.drawable.graph65 );
            image.setImageDrawable(drawable);
        } else if(goal > 55)
        {
            drawable = getResources().getDrawable( R.drawable.graph60 );
            image.setImageDrawable(drawable);
        } else if(goal > 50)
        {
            drawable = getResources().getDrawable( R.drawable.graph55 );
            image.setImageDrawable(drawable);
        } else if(goal > 45)
        {
            drawable = getResources().getDrawable( R.drawable.graph50 );
            image.setImageDrawable(drawable);
        } else if(goal > 40)
        {
            drawable = getResources().getDrawable( R.drawable.graph45 );
            image.setImageDrawable(drawable);
        } else if(goal > 35)
        {
            drawable = getResources().getDrawable( R.drawable.graph40 );
            image.setImageDrawable(drawable);
        } else if(goal > 30)
        {
            drawable = getResources().getDrawable( R.drawable.graph35 );
            image.setImageDrawable(drawable);
        } else if(goal > 25)
        {
            drawable = getResources().getDrawable( R.drawable.graph30 );
            image.setImageDrawable(drawable);
        } else if(goal > 20)
        {
            drawable = getResources().getDrawable( R.drawable.graph25 );
            image.setImageDrawable(drawable);
        } else if(goal > 15)
        {
            drawable = getResources().getDrawable( R.drawable.graph20 );
            image.setImageDrawable(drawable);
        } else if(goal > 10)
        {
            drawable = getResources().getDrawable( R.drawable.graph15 );
            image.setImageDrawable(drawable);
        } else if(goal > 5)
        {
            drawable = getResources().getDrawable( R.drawable.graph10 );
            image.setImageDrawable(drawable);
        } else
        {
            drawable = getResources().getDrawable( R.drawable.graph5 );
            image.setImageDrawable(drawable);
        }

    }

    public void updateSensorCount(String deviceAddr, int quantity, int cumulativeCount)
    {

        String sensorId = getSensorId(deviceAddr);
/*
    type	    Required	이벤트 유형(int)
    sensor	    Required	이벤트가 발생한 센서의 id
    interval	Optional	발생 간격
    quantity	Optional	발생 횟수
    variables	Optional	기타(문자열 90자)
    cumulativeCount	    Optional	일일 누적 데이터
    created	    Optional	시작 시각(미 기재시 서버의 현재시각) yyyy-MM-dd HH:mm:ss
*/
        RequestParams params = new RequestParams();
        params.add("sensor", sensorId);
        params.add("deviceAddress", deviceAddr);
        params.add("quantity", String.valueOf(quantity));
        params.add("type", "0");                                    //default type 0
        params.add("cumulativeCount", String.valueOf(cumulativeCount));
        NitingaleHttpRestClient.getInstance().post(NitingaleHttpRestClient.SENSOR_EVENT_URL,
                params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        Log.d("Canaria", "Success: Posting Sensor Data  " + Integer.toString(statusCode));

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                        Log.d("Canaria", throwable.toString());
                    }

                }
        );
    }

    private String getSensorId(String deviceAddr) {

        if(mSensorData != null){
            return mSensorData.getId(deviceAddr);
        }
        return null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_noti) {
            return true;
        }
        if (id == R.id.action_open_menu) {
            Intent intent = new Intent(this, HomeMenuActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        /*
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mGattUpdateReceiver != null) unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        bd.commitToPreference(getSharedPreferences("UserPref", Context.MODE_PRIVATE));
        if (mServiceConnection != null) unbindService(mServiceConnection);
        mBluetoothLeService = null;

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onFragmentInteraction(int actionId) {

    }

    /***
     * BLE specific functions
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.d(TAG, "makeGattUpdateIntentFilter");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private boolean ensureBLEExists() {
        Log.d(TAG, "ensureBLEExists");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Code to manage Service lifecycle.
    public  ServiceConnection  mServiceConnection;

    private void setUPBle() {

          /*
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if(mDeviceAddress == null || mDeviceAddress.equals("")) { return;}

        leDevices = ScanActivity.getLeDeviceListAdapter();
        if(leDevices == null) {return;}
        */

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // Automatically connects to the device upon successful start-up initialization.
                connectDevices();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected");
                mBluetoothLeService = null;
            }
        };
        // serviceConnections.add(mServiceConnection);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }


    private void connectDevices(){
        if (mBluetoothLeService == null)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectDevices();
        }
             /*change*/
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        final String addresses[] = bd.getDeviceAddresses();

        for(int i=0; i < addresses.length; i++) {

            mDeviceAddress = addresses[i];
            if(mDeviceAddress != null && !mDeviceAddress.equals("null") ) {
                if(bd.getDeviceState(mDeviceAddress) != BluetoothDevices.GATT_CONNECTED) {
                    mBluetoothLeService.connect(mDeviceAddress);
                }
                else
                {
                    Log.d("Test","already connected, no attempt to reconnect: " + mDeviceAddress);
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void connectDevice(){
        if (mBluetoothLeService != null)
        {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "BroadcastReceiver");

            final String action = intent.getAction();
            String deviceName = intent.getStringExtra("deviceName");
            String deviceAddr = intent.getStringExtra("deviceAddr");
            String UUIDStr = intent.getStringExtra(BluetoothLeService.UUID_STRING);
            BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                mConnected = true;
                connectionStateView.setText("Connected: " + deviceAddr); /**/
                bd.putDevice(deviceAddr, deviceName,
                        BluetoothDevices.GATT_CONNECTED); /*change*/
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                mConnected = false;
                connectionStateView.setText("disconnected: " +deviceAddr);
                bd.getBluetoothDevices().putDevice(deviceAddr, deviceName,
                        BluetoothDevices.GATT_DISCONNECTED); /*change*/
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                updateGattServices(mBluetoothLeService.getSupportedGattServices());
                // turnOnPedoListeningMode(deviceAddr);

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                // update view
                connectionStateView.setText("Data Received from " + deviceAddr ); /**/
                // update persistent data
                updateLastKnownActivity();

                BluetoothDevices.getBluetoothDevices().putDevice(deviceAddr, deviceName,
                        BluetoothDevices.GATT_CONNECTED);

                int i = intent.getIntExtra(BluetoothLeService.PEDO_TYPE, 0);

                if (i == 1) {

                } else if (i == BluetoothLeService.READ_BATTERY) {

                    final byte[] batter = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);
                    int batteryValue = batter[0];
                    // ((TextView)findViewById(R.id.batter_read)).setText( ""+ batteryValue + "%" );

                } else if (i == BluetoothLeService.READ_MEASUREMENT) {

                    byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);

                    if (tmp.length == 8) {

                        // YY/MM/DD/HH/MM
                        ByteBuffer buffer = ByteBuffer.wrap(tmp, 0, 2);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        int day = buffer.getShort();

                        buffer = ByteBuffer.wrap(tmp, 2, 2);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        int minute = buffer.getShort();

                        buffer = ByteBuffer.wrap(tmp, 4, 2);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        int step = buffer.getShort();

                        buffer = ByteBuffer.wrap(tmp, 6, 2);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        int totalStep = buffer.getShort();

                        if(totalStep > 0) {

                            Toast.makeText(MyStatusActivity.this, "Device:"+ deviceAddr + ", stepcount: " + totalStep, Toast.LENGTH_LONG).show();
                            updateSensorCount(deviceAddr, -1, totalStep);
                            displayData(deviceAddr,totalStep);


                        }

                    /*
                        updateDataBase();
                    */
                        // commit
                        //bd.commitToPreference(sharedPreferences);
                    }

                } else if (i == BluetoothLeService.READ_FALL) {

                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);
                    byte bt = data[0];
                    boolean fall = (bt & 0x40) > 0 ;

                    if(deviceAddr.equals("E1:18:38:EE:93:7C")) { //Todo: remove

                        Toast.makeText(MyStatusActivity.this, "Device:"+ deviceAddr + ", fall?: " + fall, Toast.LENGTH_LONG).show();
                        handleFall(data);
                    }

                } else if (i == BluetoothLeService.READ_SENSORDATA) {

                    final byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);

                } else  {

                    final byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);
                    //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        }
    };

    private void handleFall(byte [] data) {

            byte bt = data[0];
            boolean fall = (bt & 0x40) > 0 ;
            boolean active = (bt &0x20)  > 0;

            SimpleDateFormat sdf = new SimpleDateFormat("M월 d일    hh시 mm분");
            sdf = new SimpleDateFormat("hh시 mm분");
            String now = sdf.format(new Date());

            updateBathroomEmergency(now, fall);

            SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if(fall) {
                editor.putString("emergency", now);
                // Todo: generate critical event and generate sensor data
                updateCriticalData();

            } else {
                editor.remove("emergency");
            }

            editor.commit();
    }

    private void updateCriticalData() {
        //updateSensorData(deviceAddr, TYPE_CRITICAL);
    }

    private void updateLastKnownActivity() {

        // update last known activity

        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일    hh시 mm분");
        String todayString = sdf.format(new Date());
        activityTimeStampView.setText(todayString);

        SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LastKnownActivity", todayString);
        editor.commit();
    }

    /*

    private void displayLastKnownActivity() {

        // update last known activity

        mSensorData.getData(, )
    }
    */


    private void displayData(String deviceAddr, int totalStep) {

        SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SimpleDateFormat sdf;

        if(deviceAddr.equals("F9:91:29:54:DF:E9"))
        {
            pedo_stepcount = totalStep;

            todayStatusView.setText("" + (totalStep / 3) + "m    " + totalStep + "보");
            renderGraph(totalStep);
            editor = sharedPreferences.edit();
            editor.putString("totalStep", "" + totalStep);
            editor.commit();

        }
        else if(deviceAddr.equals("F5:A0:A3:A7:BB:4B"))
        {
            sdf = new SimpleDateFormat("hh시 mm분");
            String now = sdf.format(new Date());

            updateEmergency(now, true);
            updateBathroom(totalStep);
            editor = sharedPreferences.edit();
            editor.putString("emergency", now);
            editor.commit();
            //updateSensorData(deviceAddr, -1, totalStep);
        }
        else if(deviceAddr.equals("C4:E0:9A:A2:3E:0C"))
        {
            updateEating(totalStep);
            //updateSensorData(deviceAddr, -1, totalStep);
        }
        else if(deviceAddr.equals("E1:18:38:EE:93:7C"))
        {
            sdf = new SimpleDateFormat("hh시 mm분");
            String now = sdf.format(new Date());
            updateBathroomEmergency(now, true);
            editor = sharedPreferences.edit();
            editor.putString("emergency", now);
            editor.commit();
        }
        else
        {
            //updateSensorData(deviceAddr, -1, totalStep);
        }

    }


    private void updateGattServices(List<BluetoothGattService> gattServices) {

        Log.d(TAG, "displayGattServices");
        connectionStateView.setText("displayGattServices"); /**/

        if (gattServices == null)  {
            Log.d("MyStatusActivity: updateGattServices()", "gattServices is null" );
            return;
        }
        else if( gattServices.size() == 0 ) {
            Log.d("MyStatusActivity: updateGattServices()", "gattServices is zero" );
            return;
        }

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            uuid = gattService.getUuid().toString();
            Log.d("BluetoothGattService","Service UUID:" + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals("0000ef04-0000-1000-8000-00805f9b34fb"))
                   fallDetectCharacteristic = gattCharacteristic;
                Log.d("BluetoothGattCharacteristics","Char UUID:" + uuid);
            }

            mGattCharacteristics.add(charas);
        }
    }

    BluetoothGattCharacteristic mPedocharacteristic;

    private void turnOnPedoListeningMode(String addr) {

        if (mGattCharacteristics != null) {

            // choose a 3rd service and characteristic 0 => pedo meter

            final BluetoothGattCharacteristic mPedocharacteristic;


            if(addr.equals("E1:18:38:EE:93:7C")){

                mNotifyCharacteristic = fallDetectCharacteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        fallDetectCharacteristic, true);

            }
            else
            {
                mPedocharacteristic =
                        mGattCharacteristics.get(2).get(0);
                mNotifyCharacteristic = mPedocharacteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        mPedocharacteristic, true);
            }
            //


            Log.d(TAG, "Wait for Transmission");

            connectionStateView.setText("wait for Transmission:" + addr); /**/

        }

    }

    private void updateEating(int count) {

        int mod = count % 100;

        // LinearLayout view1 = (LinearLayout) findViewById(R.id.eating_layout);
        TextView view2 = (TextView) findViewById(R.id.eating_level);
        view2.setText("" + mod + "%");
    }

    private void updateBathroom (int count) {
        // LinearLayout view1 = (LinearLayout) findViewById(R.id.eating_layout);
        TextView view2 = (TextView) findViewById(R.id.bathroom_level);
        view2.setText("" + count + "번");
    }

    private void updateEmergency(String now, boolean fall) {


        TextView view = (TextView) findViewById(R.id.majorevent_detail);
        TextView view2 = (TextView) findViewById(R.id.majorevent_level);
        LinearLayout view3 = (LinearLayout) findViewById(R.id.majorevent_layout);
        ImageView view4 = (ImageView) findViewById(R.id.majorevent_img);

        if(fall) {

            view.setText("응급상황 낙상!: " + now);
            view2.setText("응급");
            //int color = Integer.parseInt("FFFFFF",16);
            int color = Color.parseColor("#000000");
            view3.setBackgroundColor(color);

            Drawable drawable = getResources().getDrawable(R.drawable.majorevent);
            view4.setImageDrawable(drawable);

            // TODO: commit to the persistent memory, be it database or preference

        } else {

            view.setText("특별한 응급상황 감지되지 않았습니다.");
            view2.setText("양호");
            //int color = Integer.parseInt("FFFFFF",16);
            int color = Color.parseColor("#FFFFFF");
            view3.setBackgroundColor(color);

            Drawable drawable = getResources().getDrawable(R.drawable.majorevent_invert);
            view4.setImageDrawable(drawable);

        }

    }


    private void updateEmergency(String now, boolean fall, int numEmergency) {

        updateEmergency(now, fall);
        TextView view2 = (TextView) findViewById(R.id.majorevent_level);
        view2.setText("응급 이벤트 " + numEmergency + "개");

    }

    private void updateBathroomEmergency(String now, boolean fall) {

        TextView view = (TextView) findViewById(R.id.majorevent_detail);
        TextView view2 = (TextView) findViewById(R.id.majorevent_level);
        LinearLayout view3 = (LinearLayout) findViewById(R.id.majorevent_layout);
        ImageView view4 = (ImageView) findViewById(R.id.majorevent_img);

        if(fall) {

            view.setText("응급상황 화장실 낙상! 시간:" + now);
            view2.setText("응급");

            //int color = Integer.parseInt("FFFFFF",16);
            int color = Color.parseColor("#000000");
            view3.setBackgroundColor(color);

            Drawable drawable = getResources().getDrawable(R.drawable.majorevent);
            view4.setImageDrawable(drawable);

            // TODO: commit to the persistent memory, be it database or preference

        } else {

            view.setText("특별한 응급상황 감지되지 않았습니다.");
            view2.setText("양호");

            //int color = Integer.parseInt("FFFFFF",16);
            int color = Color.parseColor("#FFFFFF");
            view3.setBackgroundColor(color);

            Drawable drawable = getResources().getDrawable(R.drawable.majorevent_invert);
            view4.setImageDrawable(drawable);

        }

    }

    private void connectionRecycle() {

        mBluetoothLeService.disconnect(mDeviceAddress);
        mBluetoothLeService.connect(mNextDeviceAddress);
        mDeviceAddress = mNextDeviceAddress;
    }


    void readSensorData() {

        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(2).get(0);

            Log.d(TAG, "characteristic " + characteristic);
            final int charaProp = characteristic.getProperties();
            Log.d(TAG, "charaProp " + charaProp);

            if (UUID_PEDOMETER_MEASUREMENT.equals(characteristic.getUuid())) {

                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }

            }


        }
    }

    void readBatteryData() {

        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(3).get(0);

            Log.d(TAG, "characteristic " + characteristic);
            final int charaProp = characteristic.getProperties();
            Log.d(TAG, "charaProp " + charaProp);

            if (UUID_BATTER_READ.equals(characteristic.getUuid())) {
                mBluetoothLeService.readCharacteristic(characteristic);
            }

        }
    }

    private void updateConnectionState(final int resourceId) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStateView.setText(resourceId);
                }
            });

    }

        /**
     * Handle Incoming messages from contained fragments.
     */


    /* notification managers only */
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    static int NOTIFICATION_ID = 1;

    // Post a notification indicating connection.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
               getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyStatusActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
