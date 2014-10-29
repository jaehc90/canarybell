package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.nightingalecare.canarymountains.utilities.BluetoothLeService;
import net.nightingalecare.canarymountains.utilities.LineGraphView;
import net.nightingalecare.canarymountains.utilities.SampleGattAttributes;

import org.achartengine.GraphicalView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DisplayActivity extends Activity{
    private final static String TAG = DisplayActivity.class.getSimpleName();

    public final static UUID UUID_BATTER_READ =
            UUID.fromString(SampleGattAttributes.BATTER_READ);
    public final static UUID UUID_BATTERY_SERVICE =
            UUID.fromString(SampleGattAttributes.BATTERY_SERVICE);

    public final static UUID UUID_PEDOMETER_SERVICE =
            UUID.fromString(SampleGattAttributes.PEDOMETER_SERVICE);
    public final static UUID UUID_PEDOMETER_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.PEDOMETER_MEASUREMENT);
    public final static UUID UUID_PEDOMETER_TIMERSET =
            UUID.fromString(SampleGattAttributes.PEDOMETER_TIMERSET);
    public final static UUID UUID_PEDOMETER_ACCSET =
            UUID.fromString(SampleGattAttributes.PEDOMETER_ACCSET);
     
    
	private GraphicalView mGraphView;
	private LineGraphView mLineGraph;
	
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	
	private TextView mTranStat;
	private TextView mStepDisplay;
	
    private TextView mConnectionState;
    private TextView mDataField;
    public static TextView mStatus;
    private TextView mSensorTime;
    private TextView mDeviceTime;
    private TextView mStepCount;
    private TextView mTotalStepCount;

    private String mDeviceName;
    private String mDeviceAddress;

    private int num_device;

    private ExpandableListView mGattServicesList;

    static public BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    static public BluetoothLeService mBluetoothLeService2;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics2 =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected2 = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic2;

    static public BluetoothLeService mBluetoothLeService3;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics3 =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected3 = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic3;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


	private int YEAR = 2000;
	private int MONTH = 1;
	private int DAY = 1;
		
	private int mCounter = 0;
	private int mTotalStep = 0;

	private int mTimerReadWrite = 0;

    TextView mBatterRead;
    Button mPedoRead;
    Button mTimerSet;

    // Code to manage Servi ce lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

	private void updateGraph(final int TotalStep, final int Counter) {
        
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
	            Log.d(TAG, "updateGraph");
//				mCounter++;
				Point point = new Point(Counter, TotalStep);
				mLineGraph.addValue(point);
				mGraphView.repaint();
			}
		});
	}

	private int CheckYUN(int parm_year){
		if((((parm_year % 4) == 0) && ((parm_year % 100) != 0) || ((parm_year % 400) == 0)))
			return 29;
		return 28;		
	}

	
	private int CheckDay(int year, int month, int day){
		int standard_year = 2000;
		int standard_month = 1;
		int differ_day = 0; // �����ϰ��� ���� 
		int month_days[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		int i,j;

		if (year >= standard_year) {
			for (i = year; i >= standard_year; --i) {
				if (i > standard_year) {				  
					month_days[1] = CheckYUN(i - 1);
					// �����̸� 366��, �ƴϸ� 365�� 
					differ_day += 337 + month_days[1];
				} else {
					month_days[1] = CheckYUN(year);
					for (j = month; j > standard_month; --j) differ_day += month_days[j - 2];
				}
			}
		}
		else {
			for (i = year; i < standard_year; ++i) {
				if (i < standard_year - 1) {
					month_days[1] = CheckYUN(i + 1);
					differ_day += 337 + month_days[1];
				} else {
					month_days[1] = CheckYUN(year);
					for (j = month; j <= 12; ++j) differ_day += month_days[j - 1];
				}          
			}
		}		
		return(differ_day + day);
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
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	    	            	
            	
            	int i;
            	i = intent.getIntExtra(BluetoothLeService.PEDO_TYPE,0);
            	if(i == 1){
            	}else if( i == 2 ){
            		final byte[] batter = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);   
					int batteryValue = batter[0];
            		((TextView)findViewById(R.id.batter_read)).setText( ""+ batteryValue + "%" );
            	}else if(i == 10){
            		byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA);

//                    if( tmp.length == 8){

//                            ByteBuffer buffer = ByteBuffer.wrap(tmp,0,2);
//                            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
//                            int numSteps = buffer.getShort();
//	                    	mStepDisplay.setText(""+numSteps);
//	        				updateGraph(numSteps);
//	                }
                    // YY/MM/DD/HH/MM

                    ByteBuffer buffer = ByteBuffer.wrap(tmp,0,2);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int day = buffer.getShort();

                    buffer = ByteBuffer.wrap(tmp,2,2);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int minute = buffer.getShort();

                    buffer = ByteBuffer.wrap(tmp,4,2);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int step = buffer.getShort();

                    buffer = ByteBuffer.wrap(tmp,6,2);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int Totalstep = buffer.getShort();
                    
                    //long basenum_2000 = 10000; // time since Epoch.
                    /*
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String dateInString = "2000";
                    Date date2000 = sdf.parse(dateInString);
                    date2000.getTime();
                    int total_minute_since_2000 = day * 1440 + minute;
                    */

                    /*
                    int day = (int)tmp[0];
                    day |= ((int)tmp[1] * 0x100);

                    int minute = (int)tmp[2];
                    minute |= ((int)tmp[3] * 0x100);
                    */
                    //int year = day / 365 + 2000;
                    //int year_day = day % 365 ;


                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/hh/mm");
                    String todayString = sdf.format(new Date());
                    String str[] = todayString.split("/");
                    int yr = Integer.parseInt(str[0]);
                    int mo = Integer.parseInt(str[1]);
                    int dy = Integer.parseInt(str[2]);
                    int hr = Integer.parseInt(str[3]);
                    int mn = Integer.parseInt(str[4]);

                    int cu_day = CheckDay(yr, mo, dy);
                    int cu_minute = (hr * mn / 10);
                    
                    
            		((TextView)findViewById(R.id.time)).setText("" + day + "/" + minute);
            		((TextView)findViewById(R.id.device_time)).setText("" + cu_day + "/" + cu_minute);
            		
            		((TextView)findViewById(R.id.steps)).setText(""+step);
            		((TextView)findViewById(R.id.totalsteps)).setText(""+Totalstep);

        			if (Totalstep > 0)
        				updateGraph(Totalstep,minute);            		
                    
            	}else if(i == 11){
            		final byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA); 
            		
            	}else if(i == 12){
            		final byte[] tmp = intent.getByteArrayExtra(BluetoothLeService.PEDO_DATA); 
            		
            	}
            	
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    Log.d(TAG, "servicesListClickListner");

                    Log.d(TAG, "groupPosition "+groupPosition+" childPosition "+childPosition);
                    
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);

                        Log.d(TAG, "characteristic "+characteristic);
                        final int charaProp = characteristic.getProperties();
                        Log.d(TAG, "charaProp "+charaProp);
                        
                        if (UUID_PEDOMETER_MEASUREMENT.equals(characteristic.getUuid())) {

                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                if (mNotifyCharacteristic != null) {
                                    mBluetoothLeService.setCharacteristicNotification(
                                            mNotifyCharacteristic, false);

                                    mNotifyCharacteristic = null;
                                    mStatus.setText("Notification Mode Off");
                                } else {
                                    mNotifyCharacteristic = characteristic;
                                    mBluetoothLeService.setCharacteristicNotification(
                                            characteristic, true);
                                    mStatus.setText("Wait for Transmission");
                                }
                            }
                            return true;                        	
                        }else if (UUID_BATTER_READ.equals(characteristic.getUuid())) {
                            mBluetoothLeService.readCharacteristic(characteristic);
                            return true;    
                        }else if (UUID_PEDOMETER_TIMERSET.equals(characteristic.getUuid())) {

                            Log.d(TAG, "UUID_PEDOMETER_TIMERSET");
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            	if( mTimerReadWrite == 0){
                            		mTimerReadWrite = 1;
    	                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/hh/mm");
    	                            String todayString = sdf.format(new Date());
    	                            String str[] = todayString.split("/");
    	                            int yr = Integer.parseInt(str[0]);
    	                            int mo = Integer.parseInt(str[1]);
    	                            int dy = Integer.parseInt(str[2]);
    	                            int hr = Integer.parseInt(str[3]);
    	                            int mn = Integer.parseInt(str[4]);
    	
    	                            int cu_day = CheckDay(yr, mo, dy);
    	                            int cu_minute = (hr * mn / 10);
    	                            
    	                            mBluetoothLeService.setCharacteristicTimer(characteristic, cu_day, cu_minute);         
                                    Log.d(TAG, " WRITE    ");  
                            	}else{                            		
	                                mBluetoothLeService.readCharacteristic(characteristic);           
	                                Log.d(TAG, " READ    ");
	                                mTimerReadWrite = 0;
                            	}
                            }               	
                            return true;    
                        }                        
                        
                    }
                    return false;
                }
    };

    private void clearUI() {
        Log.d(TAG, "clearUI");
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Log.d(TAG, "onCreate");

		
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mTranStat = (TextView) findViewById(R.id.trans_stat);
        mStepDisplay = (TextView) findViewById(R.id.steps);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

		mLineGraph = LineGraphView.getLineGraphView();
		mGraphView = mLineGraph.getView(DisplayActivity.this);
		mLineGraph.clearGraph();
		
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_pedometer);
		layout.addView(mGraphView);

		clearUI();
		mBatterRead = (TextView)findViewById(R.id.batter_read);
		mBatterRead.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                if (mGattCharacteristics != null) {
                }
			}			
		});

         mStatus = (TextView)findViewById(R.id.trans_stat);
         mSensorTime = (TextView)findViewById(R.id.time);
         mDeviceTime= (TextView)findViewById(R.id.device_time);
         mStepCount= (TextView)findViewById(R.id.steps);
         mTotalStepCount= (TextView)findViewById(R.id.totalsteps);

    }

	protected UUID getBatterUUID() {
		return BluetoothLeService.UUID_BATTER_READ;
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    

	private boolean ensureBLEExists() {
        Log.d(TAG, "ensureBLEExists");
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //unbindService(mServiceConnection);
        //mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.display, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect(mDeviceAddress);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "updateConnectionState");
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        Log.d(TAG, "displayData");
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "displayGattServices");
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                Log.d("BluetoothGatt","UUID:" + uuid);
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.d(TAG, "makeGattUpdateIntentFilter");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
}


