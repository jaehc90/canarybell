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
import android.content.SharedPreferences;
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
import net.nightingalecare.canarymountains.adapter.LeDeviceListStoredAdapter;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;
import net.nightingalecare.canarymountains.utilities.BluetoothLeService;

public class CheckDeviceInventoryActivity extends ListActivity{

    public static final String TAG = "CANARIA";

    public static LeDeviceListStoredAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;

    private boolean mScanning;
    private Handler mHandler;

    // Code to manage Service lifecycle.
    public  ServiceConnection  mServiceConnection;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    public static LeDeviceListStoredAdapter getLeDeviceListAdapter(){
        return mLeDeviceListAdapter;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Devices");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //setContentView(R.layout.activity_devices);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                final Intent intent1 = new Intent(this, ScanActivity.class);
                startActivity(intent1);
                break;
            case R.id.menu_clear:
                // update the list
                // clear adapter
                mLeDeviceListAdapter.clear();
                // clear static memory data
                BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
                if(mBluetoothLeService != null) bd.disconnectAll(mBluetoothLeService);
                bd.clear();
                // clear persistent data
                SharedPreferences sharedpreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
                bd.clearFormPreference(sharedpreferences);
                Intent intent2 = new Intent(this, ScanActivity.class);
                startActivity(intent2);
                break;
            case R.id.menu_refresh:
                final Intent intent3 = new Intent(this, MyStatusActivity.class);
                startActivity(intent3);
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

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListStoredAdapter(getLayoutInflater());
        setListAdapter(mLeDeviceListAdapter);
        mLeDeviceListAdapter.update(BluetoothDevices.getBluetoothDevices());
        Log.d("Test", mLeDeviceListAdapter.toString());
        mLeDeviceListAdapter.notifyDataSetChanged();

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String address = mLeDeviceListAdapter.getAddress(position);
        String name = mLeDeviceListAdapter.getName(position);
        int state = mLeDeviceListAdapter.getState(position);

        if (address == null || address.equals("")) return;

        final Intent intent = new Intent(this, SetUpDeviceActivity.class);
        intent.putExtra("DEVICE_ADDRESS", address);
        intent.putExtra("DEVICE_NAME", name);
        intent.putExtra("DEVICE_STATE", state);

        startActivity(intent);
    }

}
