package net.nightingalecare.canarymountains.utilities;

import java.util.HashMap;

public class SampleGattAttributes {

    private static HashMap<String, String> attributes = new HashMap();
    public static String RSC_MEASUREMENT = "00002a53-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_SERVICE = "0000ef00-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_MEASUREMENT = "0000ef01-0000-1000-8000-00805f9b34fb";
    // public static String PEDOMETER_READENABLE = "0000ef02-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_TIMERSET = "0000ef02-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_ACCSET = "0000ef03-0000-1000-8000-00805f9b34fb";
    public static String BATTER_READ = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String FALL_DETECT_SERVICE ="0000ef04-0000-1000-8000-00805f9b34fb";


    static {
    	
    	//GATT profile
    	
        attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence Service");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name Characteristic");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance Characteristic");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "PPCP");
        
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed Characteristic");
        
        attributes.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        attributes.put("00002a54-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence Feature characteristic");

        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery service");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level characteristic");
        
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        
        attributes.put(PEDOMETER_SERVICE, "Pedometer Service");
        attributes.put(PEDOMETER_MEASUREMENT, "Pedometer Measurement");
        attributes.put(PEDOMETER_TIMERSET, "Pedometer Timer Set");
        attributes.put(PEDOMETER_ACCSET, "Pedometer accelerometer Set");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
