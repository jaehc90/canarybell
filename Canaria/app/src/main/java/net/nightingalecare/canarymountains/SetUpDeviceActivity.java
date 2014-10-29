package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.Model.User;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;
import net.nightingalecare.canarymountains.utilities.BluetoothLeService;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;

import org.apache.http.Header;
import org.json.JSONObject;

public class SetUpDeviceActivity extends Activity{

    public static final String TAG = "CANARIA";

    String mDeviceName;
    String mAddress;
    int mState;

    TextView mRegister;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;

    // Code to manage Service lifecycle.
    private BluetoothLeService mBluetoothLeService;
    public  ServiceConnection  mServiceConnection;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAddress = intent.getStringExtra("DEVICE_ADDRESS");
        mDeviceName = intent.getStringExtra("DEVICE_NAME");
        mState = intent.getIntExtra("DEVICE_STATE", -1);

        setUpUI();
        LoadUI();

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected");
                mBluetoothLeService = null;
            }
        };

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void setUpUI() {

        getActionBar().setTitle(R.string.title_devices);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sensor_view);

         /* ListView */
        // mainEditText = (EditText) V.findViewById(R.id.title);
        TextView bind = (TextView) findViewById(R.id.device_bind);
        TextView unbind = (TextView) findViewById(R.id.device_unbind);
        TextView register = (TextView) findViewById(R.id.device_register);
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        if(bd.contains(mAddress)) register.setVisibility(TextView.INVISIBLE);

        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
                bd.putDevice(mAddress, mDeviceName,  mState);
                bd.commitToPreference(getSharedPreferences(User.USER_DATA_PREFERENCE, Context.MODE_PRIVATE));

                mBluetoothLeService.connect(mAddress);

                final Intent intent = new Intent(SetUpDeviceActivity.this, MyStatusActivity.class);
                startActivity(intent);
            }
        });

        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBluetoothLeService.disconnect(mAddress);
                BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
                bd.removeDevice(mAddress);
                bd.commitToPreference(getSharedPreferences(User.USER_DATA_PREFERENCE, Context.MODE_PRIVATE));

                final Intent intent = new Intent(SetUpDeviceActivity.this, MyStatusActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(SetUpDeviceActivity.this);
                alert.setTitle("Please input a location");
                alert.setMessage("bathroom/bedroom/refrigerator/wearable");
                final EditText input = new EditText(SetUpDeviceActivity.this);
                alert.setView(input);
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        input.setText("canceled");
                    }
                });
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String location = input.getText().toString();
                        SharedPreferences sharedPreferences = getSharedPreferences (User.USER_DATA_PREFERENCE, Context.MODE_PRIVATE );
                        String userId = sharedPreferences.getString("userId","");
                        //
                        //  owner	Required	사용자 id
                        //  type	Required	센서 종류(int)
                        //  location	Optional	설치장소(문자열 40바이트)
                        //  deviceAddress	Optioanl	기기 고유 주소(문자열 90바이트)
                        //  deviceName	Optional	기기 명(문자열 80바이트
                        //
                        String url = NitingaleHttpRestClient.SENSOR_REGISTER_URL;
                        RequestParams params = new RequestParams();
                        params.add("location", location);
                        params.add("owner",userId);
                        params.add("type", String.valueOf(0));
                        params.add("deviceAddress", mAddress);
                        params.add("deviceName", mDeviceName);

                        Header headers[] = new Header[0] ;
                        NitingaleHttpRestClient.getInstance().post(url, params, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(JSONObject jsonObject) {
                                Intent intent = new Intent(SetUpDeviceActivity.this, MyStatusActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                                Log.e("Canaria", statusCode + " " + throwable.getMessage());

                            }
                        });

                    }
                });
                alert.show();

                return;

                /*


                */

            }
        });
    }

    private void LoadUI(){

        TextView name = (TextView) findViewById(R.id.device_name);
        TextView addr = (TextView) findViewById(R.id.device_address);
        TextView state = (TextView) findViewById(R.id.device_connected);

        name.setText(mDeviceName);
        addr.setText(mAddress);

        if (mState == BluetoothDevices.STORED)
            state.setText("connecting ...");
        else if(mState == BluetoothDevices.GATT_CONNECTED)
            state.setText("connected");
        else if(mState == BluetoothDevices.GATT_DISCONNECTED)
            state.setText("disconnected");
        else if(mState == BluetoothDevices.AVAILABLE)
            state.setText("available");
        else
            state.setText("unknown");



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setup_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                final Intent intent1 = new Intent(this, ScanActivity.class);
                startActivity(intent1);
                break;
            case R.id.menu_refresh:
                final Intent intent2 = new Intent(this, MyStatusActivity.class);
                startActivity(intent2);
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.

        if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mLeDeviceListAdapter.clear();
    }

}
