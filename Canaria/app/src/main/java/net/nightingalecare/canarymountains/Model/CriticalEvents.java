package net.nightingalecare.canarymountains.Model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by jae on 10/22/14.
 */
public class CriticalEvents {

    User user;

    final public static String DEVICE_ID_INDEX = "sensor";
    final public static String DEVICE_TYPE_INDEX = "type";
    final public static String DEVICE_SUMMARY_INDEX = "dailysummary";

    JSONArray mEventsArray;

    HashMap <String, JSONObject> deviceList = new HashMap<String, JSONObject> ();

    /**
     *
     *  /*
     /*
     "CriticalEvents": [
     {

     "id": "1",
     "sensor": "1",
     "type": "2",
     "deleted": false,
     "description": "a",
     "deletedUser": null,

     "created": "2014-10-19 18:03:00",
     "modified": "2014-10-19 18:03:00"
     }
     ]
     */

    public CriticalEvents(JSONObject object)
    {
        if(object.has("result")) {
            JSONArray jsonArray = null;
            try {

                jsonArray = (object.getJSONObject("result")).getJSONArray("CriticalEvents");
                if(jsonArray != null) {
                    Log.d("Canaria", jsonArray.toString());
                    init(jsonArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public CriticalEvents(JSONArray array)
    {
        init(array);
    }

    private void init(JSONArray array){
        mEventsArray = array;
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        return mEventsArray.getJSONObject(index);
    }

}
