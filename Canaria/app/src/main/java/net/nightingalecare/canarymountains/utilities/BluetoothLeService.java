package net.nightingalecare.canarymountains.utilities;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service{
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<BluetoothGatt> bluetoothGattsList = new ArrayList<BluetoothGatt>();
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String PEDO_DATA =
            "com.example.bluetooth.le.PEDO_DATA";
    public final static String PEDO_TYPE =
            "com.example.bluetooth.le.PEDO_TYPE";
    public final static String PEDO_SDATA =
            "com.example.bluetooth.le.PEDO_SDATA";
    public final static String UUID_STRING =
            "com.example.bluetooth.le.UUID_STRING";

    public final static UUID UUID_RSC_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.RSC_MEASUREMENT);

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public final static UUID UUID_BATTER_READ =
            UUID.fromString(SampleGattAttributes.BATTER_READ);
    public final static UUID UUID_BATTERY_SERVICE =
            UUID.fromString(SampleGattAttributes.BATTERY_SERVICE);

    public final static UUID UUID_PEDOMETER_SERVICE =
            UUID.fromString(SampleGattAttributes.PEDOMETER_SERVICE);
    public final static UUID UUID_PEDOMETER_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.PEDOMETER_MEASUREMENT);
     public final static UUID UUID_FALLCHECK_COMSTATE=
            UUID.fromString(SampleGattAttributes.FALL_DETECT_SERVICE);

    public final static UUID UUID_PEDOMETER_TIMERSET =
            UUID.fromString(SampleGattAttributes.PEDOMETER_TIMERSET);
    public final static UUID UUID_PEDOMETER_ACCSET =
            UUID.fromString(SampleGattAttributes.PEDOMETER_ACCSET);

    SimpleDateFormat CurTimerFormat = new SimpleDateFormat("yyyyMMddHHMM00");

    public static final int READ_BATTERY = 2;
    public static final int READ_MEASUREMENT = 10;
    public static final int READ_SENSORDATA = 11;
    public static final int READ_FALL = 13;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.d(TAG, "onConnectionStateChange.");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(gatt, intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(gatt, intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered.");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(gatt, ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead.");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(gatt, ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged.");
            broadcastUpdate(gatt, ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(BluetoothGatt gatt, final String action) {
        Log.d(TAG, "broadcastUpdate.");
        final Intent intent = new Intent(action);
        intent.putExtra("deviceAddr", gatt.getDevice().getAddress());
        intent.putExtra("deviceName", gatt.getDevice().getName());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(BluetoothGatt gatt,
                                 final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("deviceAddr", gatt.getDevice().getAddress());
        intent.putExtra("deviceName", gatt.getDevice().getName());
        Log.d(TAG, "broadcastUpdate for device:" + gatt.getDevice().getAddress());

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_RSC_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();

            int format = -1;
            format = BluetoothGattCharacteristic.FORMAT_UINT16;

            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
            intent.putExtra(PEDO_DATA, heartRate);
            intent.putExtra(PEDO_TYPE, 1);
            intent.putExtra(UUID_STRING, SampleGattAttributes.HEART_RATE_MEASUREMENT);

        }else if(UUID_PEDOMETER_MEASUREMENT.equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(PEDO_DATA, data);
                intent.putExtra(PEDO_TYPE, 10);
                intent.putExtra(UUID_STRING, SampleGattAttributes.PEDOMETER_MEASUREMENT);

                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        } else if(UUID_FALLCHECK_COMSTATE.equals(characteristic.getUuid())){

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(PEDO_DATA, data);
                intent.putExtra(PEDO_TYPE, READ_FALL);
                intent.putExtra(UUID_STRING, SampleGattAttributes.FALL_DETECT_SERVICE);

                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }

        } else if(UUID_PEDOMETER_TIMERSET.equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(PEDO_DATA, data);
                intent.putExtra(PEDO_TYPE, 11);
                intent.putExtra(UUID_STRING, SampleGattAttributes.PEDOMETER_TIMERSET);
             }
        } else if(UUID_PEDOMETER_ACCSET.equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(PEDO_DATA, data);
                intent.putExtra(PEDO_TYPE, 12);
                intent.putExtra(UUID_STRING, SampleGattAttributes.PEDOMETER_ACCSET);
            }
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                intent.putExtra(PEDO_DATA, data);
                intent.putExtra(PEDO_TYPE, 2);
                intent.putExtra(UUID_STRING, SampleGattAttributes.PEDOMETER_SERVICE);
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            Log.d(TAG, "LocalBinder.");
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onBind.");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind.");
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.d(TAG, "initialize.");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        Log.d(TAG, "connect: "+ address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        /*
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        */

        BluetoothGatt gatt;
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        for(int i=0; i<bluetoothGattsList.size();i++) {
            gatt = bluetoothGattsList.get(i);
            if (gatt.getDevice().getAddress().equals(address)) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (gatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
        }

        // reinitialize device
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect: " + address);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to true.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        bluetoothGattsList.add(mBluetoothGatt);

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    protected void disconnect() {
        Log.d(TAG, "disconnect.");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void disconnect(String addr) {
        Log.d(TAG, "disconnect: " + addr);
        BluetoothGatt gatt;
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        for(int i=0; i<bluetoothGattsList.size();i++)
        {
            gatt = bluetoothGattsList.get(i);
            if(gatt.getDevice().getAddress().equals(addr))
            {
                gatt.disconnect();
                bluetoothGattsList.remove(gatt);
            }
        }

    }

    public void close() {
        Log.d(TAG, "close.");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "readCharacteristic.");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        Log.d(TAG, "setCharacteristicNotification.");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_RSC_MEASUREMENT.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);

        } else if (UUID_PEDOMETER_MEASUREMENT.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if( enabled == true){
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
            }else{
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(descriptor);

        } else if (UUID_PEDOMETER_TIMERSET.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.PEDOMETER_TIMERSET));
/*
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            String strCurDate = CurTimerFormat.format(date);
            byte[] bytes = strCurDate.getBytes();
            descriptor.setValue(bytes);
            */
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);

        }
    }

    public void setCharacteristicTimer(BluetoothGattCharacteristic characteristic,
                                       int day, int minit) {
        Log.d(TAG, "setCharacteristicTimer.");
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
        //               UUID.fromString(SampleGattAttributes.PEDOMETER_TIMERSET));

        byte[] buffer = new byte[4];

        buffer[0] = (byte)(day &0xff);
        buffer[1] = (byte)((day >> 8) & 0xff);
        buffer[2] = (byte)(minit &0xff);
        buffer[3] = (byte)((minit >> 8) & 0xff);

        characteristic.setValue(buffer);
        characteristic.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE);

    }

    public List<BluetoothGattService> getSupportedGattServices() {
        Log.d(TAG, "getSupportedGattServices.");
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    private BluetoothGattCharacteristic mBatteryCharacteritsic;
    private boolean isBatteryServiceFound = false;

    public void onBatteryRead() {
        // TODO Auto-generated method stub
    }
}
