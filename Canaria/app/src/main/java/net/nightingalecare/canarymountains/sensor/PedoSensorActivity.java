package net.nightingalecare.canarymountains.sensor;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.nightingalecare.canarymountains.Pedometer.StepService;
import net.nightingalecare.canarymountains.R;

public class PedoSensorActivity extends Activity implements SensorEventListener
{

    private final static String TAG = "StepDetectorEventListener";
    private float   mLimit = 10;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    LocationManager mLocationManager;

    //private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();


    /** Called when the activity is first created. */
	
	SensorManager sm;
	Sensor accSensor; //
	Sensor rotSensor; //

    public static TextView mNumTextView;
    static int numStep;

	static ArrayList<Double> xData;
	static ArrayList<Double> yData;
	static ArrayList<Double> zData;

    TwistAnalysis twistAnalysis;

    private android.location.LocationListener locListener;



    public PedoSensorActivity() {
        int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 3)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        numStep =0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pedo_report);

        mNumTextView = (TextView) findViewById(R.id.num_steps);

        mNumTextView.setText("Created");
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);


        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // ���ӵ�
        rotSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE); // ���̷�
        // stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        xData = new ArrayList<Double>();
        yData = new ArrayList<Double>();
        zData = new ArrayList<Double>();


        this.locListener = new LocationListener();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3 * 60 *1000, 10, locListener);
        // every 5 minutes, every 10 meter.

        // �ǽð� ���� ������ �м��� ���� GearAnalysis Ŭ���� �ν��Ͻ� �� 
        //twistAnalysis = new TwistAnalysis();


        // 10000 (0.01 delay) , sampling rate 100

        //sm.registerListener(this, rotSensor, 10000);

    }
    
	@Override
	protected void onResume() 
	{
		super.onResume();
       // sm.registerListener(mSensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // sm.registerListener(mSensorEventListener, rotSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // sm.registerListener(mSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
        sm.unregisterListener(this);
	}


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		// not need to define here
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{

	}
/*
    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        private float mStepOffset;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            // data is given
            synchronized (this)
            {
                double var0 = event.values[0];
                double var1 = event.values[1];
                double var2 = event.values[2];
                long time = event.timestamp;

                switch (event.sensor.getType())
                {
                    case Sensor.TYPE_STEP_COUNTER:
                        if (mStepOffset == 0) {
                            mStepOffset = event.values[0];
                        }
                        Log.d("Step Counting:", Float.toString(event.values[0] - mStepOffset));

                        mNumTextView.setText(Float.toString(event.values[0] - mStepOffset));

                        return;

                    case Sensor.TYPE_ACCELEROMETER:
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        xData.add(var0);
                        yData.add(var1);
                        zData.add(var2);

                        if(xData.size() == 300)
                        {
                            Log.d("sensor data","xData 300");
                            Toast.makeText(getApplicationContext(), "X data 300 reached", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    default:
                        Log.d("sensor data","unknown sensor type");
                }
            }
        }
    };
*/

    // TODO: unite all into 1 type of message

    /*
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
    };

    private static final int STEPS_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    numStep = (int)msg.arg1;
                    mNumTextView.setText("" + numStep);
                    break;
                 default:
                    super.handleMessage(msg);
            }
        }

    };
    */


    public class LocationListener implements android.location.LocationListener {
        final String LOG_LABEL = "Location Listener>>";

        @Override
        public void onLocationChanged(Location location) {
            Log.d("LOG", LOG_LABEL + "Location Changed");
            if (location != null) {
                double longitude = location.getLongitude();
                Log.d("LOG", LOG_LABEL + "Longitude:" + longitude);
                Toast.makeText(getApplicationContext(), "Long::" + longitude, Toast.LENGTH_SHORT).show();
                double latitude = location.getLatitude();
                Toast.makeText(getApplicationContext(), "Lat::" + latitude, Toast.LENGTH_SHORT).show();
                Log.d("LOG", LOG_LABEL + "Latitude:" + latitude);

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    }

    void cleanUp()
    {
        // This needs to be done to stop getting the location data and save the
        // battery power.
        if (null != this.locListener && null != mLocationManager)
        {
            mLocationManager.removeUpdates(this.locListener);
            this.locListener = null;
        }
    }
}