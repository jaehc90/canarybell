package net.nightingalecare.canarymountains.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.nightingalecare.canarymountains.R;
import net.nightingalecare.canarymountains.SensorActivity;
import net.nightingalecare.canarymountains.utilities.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jae on 8/13/14.
 */
public class SensorDataListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;
    private String mSensorLocation;

    public SensorDataListAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }

    @Override
    public int getCount() {
        return mJsonArray.length();
    }

    @Override
    public Object getItem(int i) {
        //mJsonArray.getJSONObject(i);
        Object obj = mJsonArray.optJSONObject(i);
        return obj;
    }

    @Override
    public long getItemId(int i) {
        JSONObject obj = mJsonArray.optJSONObject(i);
        return obj.optLong("id");
    }

    public String getItem(int position, String key) {
        JSONObject obj = (JSONObject) getItem(position);

        if (obj.has(key)) {
            return obj.optString(key);
        }
        return "";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.row_sensor_data, null);

            // CUSTOMIZE: create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.timeTextView = (TextView) convertView.findViewById(R.id.event_time);
            holder.detailTextView = (TextView) convertView.findViewById(R.id.event_detail);
            convertView.setTag(holder);

        } else {

            // skip all the expensive inflation/findViewById
            // and just ge
            // t the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }
        // More code after this
        // Get the current book's data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);

        populateHolder(holder, jsonObject);

        return convertView;
    }

    /*CUSTOMIZE*/
    private void populateHolder(ViewHolder holder, JSONObject object) {
        try {

            // "2014-10-19 18:03:00";
            String timeStr = object.getString("created");

            String timeStamp = DateUtil.convertDateStr(timeStr,"yyyy-MM-dd HH:mm:ss", "M/dd HH:mm" );

            String detail = "이벤트 발생";
            if(mSensorLocation.equals(SensorActivity.MAJOREVENT)) {
                detail = "응급 상황 이벤트";
                String deleted = object.optString("deleted", "");
                if(!deleted.equals("")){
                    if(object.optBoolean("deleted"))
                    {
                        String userName = object.optString("deletedUser");
                        detail = "유저" + userName + "가 응급상황 대처";
                    }
                }
            }
            else if(mSensorLocation.equals(SensorActivity.BATHROOM)) {
                detail = "화장실을 사용하셨습니다";
            }
            else if(mSensorLocation.equals(SensorActivity.SLEEP)) {
                detail = "침실을 사용하셨습니다";
            }
            else if(mSensorLocation.equals(SensorActivity.PEDO)) {
                detail = "Update";
            }
            else if(mSensorLocation.equals(SensorActivity.EATING)) {
                detail = "냉장고를 사용하셨습니다";
            }
            else  {
                detail = "Update";
            }
            holder.timeTextView.setText(timeStamp);
            holder.detailTextView.setText(detail);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // this is used so you only ever have to do
    // inflation and finding by ID once ever per View
    // CUSTOMIZE
    private static class ViewHolder {
        // public ImageView thumbnailImageView;
        public TextView timeTextView;
        public TextView detailTextView;
    }

    public void updateData(JSONArray jsonArray, String location) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        mSensorLocation = location;

        notifyDataSetChanged();
    }

}
