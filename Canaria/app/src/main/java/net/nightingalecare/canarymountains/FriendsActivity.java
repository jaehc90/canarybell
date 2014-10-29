package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.adapter.FriendsListAdapter;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jae on 8/21/14.
 */
public class FriendsActivity extends Activity implements AdapterView.OnItemClickListener {


    private EditText mainEditText;
    private Button searchButton;

    ListView friendsListView;
    FriendsListAdapter friendsListAdapter;

    // Create a client to perform networking
    private static AsyncHttpClient client = null;

    private static final String Server_URL_Home = "http://128.199.145.40/advise/";
    private static final String IMG_URL_Home = "hhttp://128.199.145.40/advise/app/webroot/img/";
    String[] dummyNames ={"김정훈","임정연","정우진","성수정", "소유진", "필리스"};

    public FriendsActivity(){}

    private void Fail() {
        Toast.makeText(this, "get notification fail", Toast.LENGTH_LONG).show();
    }

    private void OK(String id) {
        Toast.makeText(this, id + "  OK", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setupUI();

        // create listener
        friendsListView.setOnItemClickListener(this);
        
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


        // 12. Now that the user's chosen a book, grab the cover data
        Long id = friendsListAdapter.getItemId(position);

        Intent intent = new Intent(this, FriendProfileActivity.class);
        intent.putExtra("friendId", String.valueOf(id));
        startActivity(intent);

        /*
        String image = null;
        try {
            image = friendsListAdapter.getImageName(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        // create an Intent to take you over to a new DetailActivity
        //Intent friendIntent = new Intent(this, FriendActivity.class);

        // pack away the data about the cover
        // into your Intent before you head out
        // friendIntent.putExtra("image", image);

        // start the next Activity using your prepared Intent
        // startActivity(detailIntent);

    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        loadFriendList();
    }

    private void loadFriendList() {

        /* ListView */
        // mainEditText = (EditText) V.findViewById(R.id.title);
        friendsListView = (ListView) findViewById(R.id.friends_listview);
        View header = getLayoutInflater().inflate(R.layout.friends_header, null);
        // View footer = getLayoutInflater().inflate(R.layout.footer, null);
        friendsListView.addHeaderView(header);

        // Create a JSONAdapter for the ListView; Set the ListView to use the ArrayAdapter
        friendsListAdapter = new FriendsListAdapter(this, getLayoutInflater());
        friendsListView.setAdapter(friendsListAdapter);

       // getFriends();
        JSONArray dummyarray = getDummies();
        friendsListAdapter.updateData(dummyarray);

        getFriends();

    }

    private void getFriends(){

        RequestParams params = new RequestParams();
        String url = "users/me/watchers.json";

        NitingaleHttpRestClient.getInstance().get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Toast.makeText(FriendsActivity.this, "Success!", Toast.LENGTH_LONG).show();
                Log.d("Canaria", jsonObject.toString());

                if (jsonObject.has("result")) {

                    try {
                        //String location = jsonObject.getJSONObject("result").getJSONObject("Sensor").getString("location"); //Todo: location update in the database
                        JSONArray array = jsonObject.getJSONArray("result");

                        friendsListAdapter.updateData(array);
                        // mSensorDataListAdapter.updateData(array, location);
                        // mSensorDataArray = array;

                        //setListViewHeightBasedOnChildren( (ListView) friendsListView);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                //updateLastKnownActivity();
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                Toast.makeText(FriendsActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("Canaria", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });


        }

    public JSONArray getDummies() {
        JSONArray dummies = new JSONArray();
        for(int i=0; i < dummyNames.length; i++) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name",dummyNames[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dummies.put(obj) ;
        }
        return dummies;
    }
/*
    private void getFriends(){

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        AsyncHttpClient client = NTGClient.getAsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("limit","10");
        String url = Server_URL_Home + "notifications.json";
        Log.d("Social Siren: GET", url);

        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                // Display a "Toast" message
                Toast.makeText(NotificationActivity.this, "Success!", Toast.LENGTH_LONG).show();
                // For now, just log results
                Log.d("social siren", jsonObject.toString());
                // update the view by updating adapter
                if(jsonObject.has("result")) {
                    mJSONNotificationListAdapter.updateData(jsonObject.optJSONArray("result"));
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                // Display a "Toast" message
                // to announce the failure
                Toast.makeText(NotificationActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                // Log error message to help solve any problems
                Log.e("social siren", statusCode + " " + throwable.getMessage());
                // update the view
            }
        });

    }

    private void initClient(){
        if(client == null){
            client =  new AsyncHttpClient();
        }
    }
*/
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.friend, menu);
    return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
               final Intent intent = new Intent(this, FriendAddActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_back:
                final Intent intent2 = new Intent(this, MyStatusActivity.class);
                startActivity(intent2);
                break;
        }
        return true;
    }


}
