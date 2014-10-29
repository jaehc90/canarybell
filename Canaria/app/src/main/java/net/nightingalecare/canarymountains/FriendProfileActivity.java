package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.utilities.CBActivity;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by jae on 8/21/14.
 */
public class FriendProfileActivity extends CBActivity {


    TextView mPhonenum;
    TextView mGender;
    TextView mCommitButton;
    TextView mName;
    CheckBox mSmsNotify;

    String friendId;
    String mMyId;

    public FriendProfileActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // need to update
        mPhonenum = (TextView) findViewById(R.id.phone_number);
        mGender = (TextView) findViewById(R.id.gender);
        mName = (TextView) findViewById(R.id.nickname);
        mSmsNotify = (CheckBox) findViewById(R.id.subscribe);

        Intent oldIntent = getIntent();
        friendId = oldIntent.getStringExtra("friendId");
        SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE);
        mMyId = sharedPreferences.getString("userId", "");
        RequestParams params = new RequestParams();

        if (friendId != null && !friendId.equals("")) {

            NitingaleHttpRestClient.getInstance().get("users/" + friendId +  ".json", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, JSONObject jsonObject) {
                    Log.d("profile activity: getting user context success", jsonObject.toString());
                    JSONObject user = null;
                    try {
                        user = jsonObject.getJSONObject("result").getJSONObject("User");
                        String phonenum = user.getString("phone");
                        String id = user.getString("id");
                        String gender = user.getString("gender");
                        String name = user.getString("name");
                        boolean smsNotify = user.getBoolean("smsNotify");

                        mName.setText(name);
                        mPhonenum.setText(phonenum);
                        mGender.setText(gender);
                        mName.setText(name);
                        mSmsNotify.setChecked(smsNotify);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject
                        object) {

                    Log.d("Canaria: error", throwable.toString() + object.toString());
                }
            });
        }

        /*
        *   SetUp
        * */
        setupUI();

        mCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final RequestParams params = new RequestParams();
                params.add("smsNotify",mSmsNotify.getText().toString());
                NitingaleHttpRestClient.getInstance().post("users/"+ friendId + ".json", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, JSONObject jsonObject) {
                        Log.d("profile activity: updated sms notify with " + params.toString(), jsonObject.toString());
                        Intent intent = new Intent(FriendProfileActivity.this, MyStatusActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject
                            object) {

                        Log.d("Canaria: error", throwable.toString() + object.toString());
                        Toast.makeText(FriendProfileActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });
            }
        });


    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mCommitButton = (TextView) findViewById(R.id.modify_button);

        hidekeyboard(findViewById(R.id.signin));


    }

    public void hidekeyboard(View view)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    Log.d("canaria: Add User", "Touch outside");
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            FriendProfileActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(FriendProfileActivity.this.getCurrentFocus().getWindowToken(), 0);
                    return false;
                }
            });

        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);
                hidekeyboard(innerView);

            }
        }
    }

}
