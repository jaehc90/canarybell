package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import net.nightingalecare.canarymountains.adapter.LeDeviceListAdapter;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;
import net.nightingalecare.canarymountains.utilities.BluetoothLeService;

public class SensorSetupActivity extends Activity{

    public static final String TAG = "CANARIA";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;

    private Handler mHandler;

    // Code to manage Service lifecycle.
    public  ServiceConnection  mServiceConnection;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_devices);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     }

    @Override
    protected void onPause() {
        super.onPause();
        //mLeDeviceListAdapter.clear();
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {

        // update the list
        /*
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        bd.putDevice(device.getAddress(), BluetoothDevices.STORED);

        mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(device.getAddress());

        final Intent intent = new Intent(this, SensorSetupActivity.class);
        startActivity(intent);
        */
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Canaria.ScanActivity", "onDestroy");
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        bd.commitToPreference(getSharedPreferences("UserPref", Context.MODE_PRIVATE));
    }
}
