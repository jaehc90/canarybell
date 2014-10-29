package net.nightingalecare.canarymountains.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.nightingalecare.canarymountains.R;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;

import java.util.ArrayList;

/**
 * Created by jae on 9/22/14.
 */
public  class LeDeviceListStoredAdapter extends BaseAdapter {
        public ArrayList<ArrayList<String>> mLeDevices;
        private LayoutInflater mInflator;

        public static final int ADDRESS_INDEX = 0;
        public static final int NAME_INDEX = 1;
        public static final int STATE_INDEX = 2;

        public LeDeviceListStoredAdapter(LayoutInflater inflater) {
            super();

            if(mLeDevices == null) {
                mLeDevices = new ArrayList<ArrayList<String>>();
            }
            mInflator = inflater;
        }

        public void addDevice(String addr, String name, int state) {

            ArrayList<String> attr = new ArrayList<String>();
            attr.add(ADDRESS_INDEX, addr);
            attr.add(NAME_INDEX, name);
            attr.add(STATE_INDEX, String.valueOf(state));
            mLeDevices.add(attr);

        }

        public void clear() {
            mLeDevices.clear();
        }

        public String getAddress(int i){
            ArrayList<String> attr = mLeDevices.get(i);
            return attr.get(ADDRESS_INDEX);
        }
        public String getName(int i){
            ArrayList<String> attr = mLeDevices.get(i);
            return attr.get(NAME_INDEX);
        }
        public int getState(int i){
            ArrayList<String> attr = mLeDevices.get(i);
            return Integer.parseInt(attr.get(STATE_INDEX));
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.activity_scan, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.connected = (TextView) view.findViewById(R.id.device_connected);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final String deviceAddress = getAddress(i);
            if (deviceAddress == null || deviceAddress.length() == 0)
            {
                viewHolder.deviceName.setText("Error: device list corrupted");
                return view;
            }
            else
            {
                viewHolder.deviceAddress.setText(deviceAddress);
            }

            final String deviceName = getName(i);
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);

            int state = getState(i);
            if (state == BluetoothDevices.STORED)
                viewHolder.connected.setText("connecting ...");
            else if(state == BluetoothDevices.GATT_CONNECTED)
                viewHolder.connected.setText("connected");
            else if(state == BluetoothDevices.GATT_DISCONNECTED)
                viewHolder.connected.setText("disconnected");
            else
                viewHolder.connected.setText("available");

            return view;
        }

        public void update(BluetoothDevices bd)
        {
            String addresses[] = bd.getDeviceAddresses();
            for (String address: addresses)
            {
                addDevice(address, bd.getDeviceName(address), bd.getDeviceState(address));
            }
        }


    public void updateFromPreference(SharedPreferences sharedpreferences){
            BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();
            bd.initFromPreferences(sharedpreferences);
            update(bd);
        }

    @Override
    public String toString() {
        return  mLeDevices.toString();
    }


    class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
            TextView connected;
        }


}

