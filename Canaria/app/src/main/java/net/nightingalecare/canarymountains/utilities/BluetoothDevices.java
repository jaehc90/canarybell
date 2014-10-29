package net.nightingalecare.canarymountains.utilities;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by jae on 10/16/14.
 */
public class BluetoothDevices {

    static BluetoothDevices blDevices;

    protected final static String BLUETOOTHDEVICES_TAG ="stored_devices";
    // state variable

    public static final int AVAILABLE = 0;
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECTED = 2;
    public static final int STORED = 3;            // Device is stored and is about to be connected
    public static final int BONDED = 4;

    // private members
    HashMap<String, ArrayList<String>> states;
    SharedPreferences mSharedPreference = null;

    public static BluetoothDevices getBluetoothDevices(){

        if(blDevices != null){
            return blDevices;
        }
        blDevices = new BluetoothDevices();
        return blDevices;

    }

    protected BluetoothDevices(){
        states = new HashMap<String, ArrayList<String>>();
    }

    public void clear(){
        states.clear();
    }

    public boolean contains(String addr){
        return states.containsKey(addr);
    }

    public void putDevice(String addr, String name, int state){
        ArrayList<String> attr = new ArrayList<String>();
        attr.add(name);
        attr.add((String.valueOf(state)));
        if(states.containsKey(addr)) {
            states.remove(addr);
        }
        states.put(addr, attr);
    }

    public int getDeviceState(String addr){
        ArrayList<String> attr =  states.get(addr);
        Integer integer = Integer.parseInt(attr.get(1));
        return integer.intValue();
    }

    public String getDeviceName(String addr){
        ArrayList<String> attr =  states.get(addr);
        return attr.get(0);
    }

    public void removeDevice(String addr){
        states.remove(addr);
    }

    public void  commitToPreference(SharedPreferences sharedpreferences){

        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        final String addresses[] = bd.getDeviceAddresses();
        String devices = "";
        String device;

        for(int i=0; i < addresses.length; i++)
        {
            device = addresses[i] + "," + getDeviceName(addresses[i]);
            if(devices.equals(""))
                devices = device;
            else
                devices = devices + ";" + device;

        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(BLUETOOTHDEVICES_TAG, devices);
        editor.commit();

    }

    public void initFromPreferences(SharedPreferences sharedpreferences){

        String devices;
        String deviceArray[];
        String attr[];

        mSharedPreference = sharedpreferences;

        if (sharedpreferences.contains(BLUETOOTHDEVICES_TAG))
        {
            devices = sharedpreferences.getString(BLUETOOTHDEVICES_TAG, "");
            deviceArray = devices.split(";");
        }
        else
        {
            deviceArray = new String[0];
        }

        for(int i=0; i<deviceArray.length; i++)
        {
            attr = deviceArray[i].split(",");
            String addr = attr[0];

            BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
            if(addr != null && !addr.equals(""))
                bd.putDevice(addr, attr[1], BluetoothDevices.STORED);
        }
    }


    public  void clearFormPreference(SharedPreferences sharedpreferences){
        BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
        bd.clear();
        if (sharedpreferences.contains(BLUETOOTHDEVICES_TAG))
        {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove(BLUETOOTHDEVICES_TAG);
            editor.commit();
        }
    }

    public void initFromDB(JSONObject result){

        String devices;
        JSONObject attr;
        JSONArray deviceArray;

        try {

            deviceArray = result.getJSONArray("Sensors");

            for(int i=0; i<deviceArray.length(); i++)
            {
                attr = deviceArray.getJSONObject(i);
                String addr = attr.getString("deviceAddress");
                String name = attr.getString("deviceName");

                BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
                if(addr != null && !addr.equals(""))
                    bd.putDevice(addr, name, BluetoothDevices.STORED);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void connectGatt(){

    }

    public void disconnect(BluetoothLeService service, String addr){
        service.disconnect(addr);

    }
    public void connect(BluetoothLeService service, String addr){
        service.connect(addr);

    }

    public void disconnectAll(BluetoothLeService service){
        String addresses[] = getDeviceAddresses();
        for(String addr: addresses)
            service.disconnect(addr);
    }

    public String[] getDeviceAddresses(){
        Set<String> keys = states.keySet();
        String array[]  = keys.toArray(new String[keys.size()]);
        return array;
    }

    public int size(){
        return states.size();
    }

    @Override
    public String toString() {
        return states.toString();
    }

}
