package net.nightingalecare.canarymountains.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.nightingalecare.canarymountains.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jae on 8/13/14.
 */
public class FriendsListAdapter extends BaseAdapter {

    private static final String IMAGE_URL_BASE = "http://128.199.145.40/advise/app/webroot/img/";

    //private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public FriendsListAdapter(Context context, LayoutInflater inflater) {
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


    public String getItemIdString(int i) {

        JSONObject obj = mJsonArray.optJSONObject(i);
        return obj.optString("id");
    }

    public String getCommentId(int position) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("Article")) {
            JSONObject t_obj = obj.optJSONObject("Article");
            return t_obj.optString("id");
        }
        return "";
    }

    public String getItem(int position, String key) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("Article")) {
            JSONObject t_obj = obj.optJSONObject("Article");
            return t_obj.optString(key);
        }
        return "";
    }


    public String getPostingId(int position) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("Article")) {
            JSONObject t_obj = obj.optJSONObject("Article");
            return t_obj.optString("id");
        }
        return "";
    }

    public JSONObject getArticle(int position) {
        JSONObject obj = (JSONObject) getItem(position);
        if (obj.has("Article")) {
            JSONObject t_obj = obj.optJSONObject("Article");
            return t_obj;
        }
        return null;
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
    public String getImageName(int position) throws JSONException {
        JSONObject article = getArticle(position);
        return (String) article.getJSONArray("images").get(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.row_friend, null);

            // CUSTOMIZE: create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.friend_image);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.friend_name);
            holder.idTextView = (TextView) convertView.findViewById(R.id.friend_id);
            // hang onto this holder for future recycle
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

        // See if there is a cover ID in the Object
        // Load the image and Setting thumbnailImageView and also catch!!!
        populateHolder(holder, jsonObject);

        return convertView;
    }

    /*CUSTOMIZE*/
    private void populateHolder(ViewHolder holder, JSONObject jsonObject) {
        holder.nameTextView.setText(jsonObject.optString("name","").toString());
    }

    private void loadUserImg(ViewHolder holder, JSONObject obj) {

        JSONArray t_array = obj.optJSONArray("images");
        String imageID = "";

        if (t_array != null)
            imageID = t_array.optString(0);

        // Construct the image URL (specific to API)
        String userImageURL = IMAGE_URL_BASE + imageID;

        // Use Picasso to load the image
        // Temporarily have a placeholder in case it's slow to load
        // load from the URL and default into a placeholder and cache and load into ()
        Picasso.with(mContext).load(userImageURL).placeholder(R.drawable.profile).into(holder.thumbnailImageView);
    }

    // this is used so you only ever have to do
    // inflation and finding by ID once ever per View
    // CUSTOMIZE
    private static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView nameTextView;
        public TextView idTextView;
    }

    public void updateData(JSONArray jsonArray) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

}
