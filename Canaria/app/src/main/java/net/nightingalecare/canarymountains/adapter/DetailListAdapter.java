package net.nightingalecare.canarymountains.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.nightingalecare.canarymountains.R;

/**
 * Created by jae on 8/13/14.
 */
public class DetailListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public DetailListAdapter(Context context, LayoutInflater inflater) {
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
        return mJsonArray.optJSONObject(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.parseLong(getCommentId(i));
    }

    public String getCommentId(int position) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("Article")) {
            JSONObject t_obj = obj.optJSONObject("Article");
            return t_obj.optString("id");
        }
        return "";
    }
    public JSONObject getUser(int position) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("User")) {
            JSONObject t_obj = obj.optJSONObject("User");
            return t_obj;
        }
        return null;
    }

    public String optUserName(int position) {
        JSONObject obj = getUser(position);

        if(obj == null ) return "";
        return obj.optString("name", "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageViewHolder holder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.layout_picture_horizontal, null);

            // create a new "Holder" with subviews
            holder = new ImageViewHolder();
            /*
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.detail_comment_user_img);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.detail_cell_text1);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.detail_cell_text2);
            holder.bodyTextView = (TextView) convertView.findViewById(R.id.detail_cell_text3);
            */

            // hang onto this holder for future recycle
            convertView.setTag(holder);

        } else {

            // skip all the expensive inflation/findViewById
            // and just ge
            // t the holder you already made
            holder = (ImageViewHolder) convertView.getTag();
        }
        // More code after this
        // Get the current book's data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);

        // See if there is a cover ID in the Object
        // Load the image and Setting thumbnailImageView and also catch!!!
        populateHolder(holder, position, jsonObject);

        return convertView;
    }

    private void populateHolder(ImageViewHolder holder, int position, JSONObject jsonObject) {

    }

    // this is used so you only ever have to do
    // inflation and finding by ID once ever per View
    private static class ImageViewHolder {
        public ImageView thumbnailImageView;
        public TextView nameTextView;
        public TextView bodyTextView;
        public TextView timeTextView;
    }


    public void updateData(JSONArray jsonArray) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }
}
