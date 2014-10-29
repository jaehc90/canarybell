package net.nightingalecare.canarymountains.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.nightingalecare.canarymountains.R;
import net.nightingalecare.canarymountains.ScanActivity;
import net.nightingalecare.canarymountains.utilities.BluetoothDevices;

import java.util.ArrayList;

/**
 * Created by jae on 9/22/14.
 */
public  class LeDeviceListAdapter extends BaseAdapter {
        static public ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter(LayoutInflater inflater) {
            super();

            if(mLeDevices == null) {
                mLeDevices = new ArrayList<BluetoothDevice>();
            }
            mInflator = inflater;
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
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

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            final String deviceAddress = device.getAddress();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            BluetoothDevices bd = BluetoothDevices.getBluetoothDevices();

            if(!bd.contains(deviceAddress))
            {
                viewHolder.connected.setText("available");
                return view;
            } else if (bd.getDeviceState(deviceAddress) == BluetoothDevices.STORED)
                viewHolder.connected.setText("listens on service");
            else if(bd.getDeviceState(deviceAddress) == BluetoothDevices.GATT_CONNECTED)
                viewHolder.connected.setText("connected");
            else if(bd.getDeviceState(deviceAddress) == BluetoothDevices.GATT_DISCONNECTED)
                viewHolder.connected.setText("disconnected");
            else
                viewHolder.connected.setText("available");
            return view;
        }

        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
            TextView connected;
        }
}

