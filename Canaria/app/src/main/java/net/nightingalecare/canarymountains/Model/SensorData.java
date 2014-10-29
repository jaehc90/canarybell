package net.nightingalecare.canarymountains.Model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by jae on 10/22/14.
 */
public class SensorData {

    User user;

    final public static String DEVICE_NAME_INDEX = "deviceName";
    final public static String DEVICE_ADDRESS_INDEX = "deviceAddress";
    final public static String DEVICE_ID_INDEX = "deviceName";
    final public static String DEVICE_TYPE_INDEX = "type";
    final public static String DEVICE_OWNER_INDEX = "owner";
    final public static String DEVICE_LOCATION_INDEX = "location";
    final public static String DEVICE_SUMMARY_INDEX = "dailySummary";

    final public static int TYPE_NORMAL = 0;
    final public static int TYPE_CRITICAL = 1;
    final public static int TYPE_BATHROOM = 2;
    final public static int TYPE_BEDROOM = 3;
    final public static int TYPE_DOOR = 4;
    final public static int TYPE_REFERIGERATOR = 5;

    HashMap <String, JSONObject> deviceList = new HashMap<String, JSONObject> ();

    /**
     *
     *  /*
     {
     "result": [
     {
     "id": "2",
     "owner": "1",
     "type": "2",
     "deviceName": "센서2",
     "deviceAddress": "00:11:22:33:44:55",
     "location": "방",
     "created": "2014-10-19 19:25:55",
     "modified": "2014-10-19 19:25:55",
     "dailysummary": []
     },
     {
     "id": "1",
     "owner": "1",
     "type": "0",
     "deviceName": "SENSOR1",
     "deviceAddress": "11:11:11:11:11:11",
     "location": "HOME",
     "created": "2014-10-19 18:03:00",
     "modified": "2014-10-19 18:03:00",
     "dailysummary": []
     }
     ]


     */

    public SensorData(JSONObject object)
    {
        if(object.has("result")) {
            JSONArray jsonArray = null;
            try {
                jsonArray = object.getJSONObject("result").getJSONArray("Sensors");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("Canaria", jsonArray.toString());
            init(jsonArray);
        }
    }

    public SensorData(JSONArray array)
    {
        init(array);
    }

    private void init(JSONArray array){

        JSONObject object;
        for(int i = 0; i < array.length(); i++)
        {
            try {
                object = array.getJSONObject(i);
                String addr = object.getString(DEVICE_ADDRESS_INDEX);
                deviceList.put(addr, object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public String getId(String addr){

        JSONObject jsonObject = deviceList.get(addr);
        String id = null;
        try {
            id = jsonObject.getString(DEVICE_ID_INDEX);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public int getSummaryData(String addr, int index){
        JSONObject jsonObject = deviceList.get(addr);
        JSONArray jsonArray = null;
        int summaryData = -1;

        try {
            jsonArray = jsonObject.getJSONArray(DEVICE_SUMMARY_INDEX);
            if(jsonArray.length() == 0) return -1;
            JSONObject object2 = jsonArray.getJSONObject(index);
            summaryData = object2.getInt("maxValue");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return summaryData;

    }

    public String getData(String addr, String key){
        JSONObject jsonObject = deviceList.get(addr);
        String str = null;
        int summaryData = -1;

        try {
            str = jsonObject.getString(key);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;

    }

    public String getLocation(String addr){
        return getData(addr, DEVICE_LOCATION_INDEX);
    }
}
