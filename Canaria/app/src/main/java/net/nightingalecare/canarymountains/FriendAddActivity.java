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
public class FriendAddActivity extends CBActivity {


    EditText mPhonenum;
    EditText mGender;
    EditText mPassword;
    EditText mName;
    TextView mCommitButton;

    String mMyId;

    public FriendAddActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_new);

        SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
        mMyId = sharedPreferences.getString("userId", "");

        /*
        *   SetUp
        * */
        setupUI();

        mCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RequestParams params = new RequestParams();

                // need to update
                EditText nameInput = (EditText) findViewById(R.id.nickname);
                EditText phoneInput = (EditText) findViewById(R.id.phone_number);
                EditText passwordInput = (EditText) findViewById(R.id.password);
                EditText genderInput = (EditText) findViewById(R.id.gender);
                params.add("name", nameInput.getText().toString());
                params.add("phone", phoneInput.getText().toString());
                params.add("password", passwordInput.getText().toString());
                params.add("gender", genderInput.getText().toString());

                // NOTE: maybe this is not a good policy!
                // client.postSignOut(params,handler);

                NitingaleHttpRestClient.getInstance().post("users.json", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, JSONObject jsonObject) {
                        Log.d("profile activity: getting user context success", jsonObject.toString());

                        RequestParams params = new RequestParams();
                        try {
                            params.add("source", mMyId);
                            params.add("destination", jsonObject.getJSONObject("result").getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        NitingaleHttpRestClient.getInstance().post("users/watch.json", params, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, JSONObject jsonObject) {
                                Intent intent = new Intent(FriendAddActivity.this, MyStatusActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                                Log.d("Canaria: error", throwable.toString() + object.toString());
                                Toast.makeText(FriendAddActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject
                            object) {

                        Log.d("Canaria: error", throwable.toString() + object.toString());
                        Toast.makeText(FriendAddActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
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
                            FriendAddActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(FriendAddActivity.this.getCurrentFocus().getWindowToken(), 0);
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
