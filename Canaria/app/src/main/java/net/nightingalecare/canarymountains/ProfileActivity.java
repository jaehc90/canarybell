package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import net.nightingalecare.canarymountains.Model.User;
import net.nightingalecare.canarymountains.utilities.CBActivity;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;
import net.nightingalecare.canarymountains.utilities.NitingaleMessage;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by jae on 8/21/14.
 */
public class ProfileActivity extends CBActivity {


    EditText mPhonenum;
    EditText mPassword;
    EditText mGender;
    TextView mSigninButton;

    public ProfileActivity(){}


    protected Handler signInHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NitingaleMessage.SIGN_IN_OK:
                    User user = (User) msg.obj;
                    signInOK(user);
                    break;
                case NitingaleMessage.SIGN_IN_FAIL:
                    signInFail();
                    break;
                default:
                    break;
            }
        }
    };

    private void signInFail() {
        Toast.makeText(this, "sign in fail", Toast.LENGTH_LONG).show();
    }

    private void signInOK(User user) {
        Toast.makeText(this, user.toString() + " sign in ok", Toast.LENGTH_LONG).show();
        Log.d("canaria", "User Sign OK");

        // create an Intent to take you over to a newMainActivity
        Intent intent = new Intent(this, MyStatusActivity.class);

        // start the next Activity using your prepared Intent
        /*
        SharedPreferences sharedPreferences = getSharedPreferences ("userPref", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String unreadNotifications = user.getUnreadNotifications();
        editor.putString("unreadNotifications", unreadNotifications);
        editor.commit();
        */

        startActivity(intent);
    }

    final public Handler signOutHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NitingaleMessage.SIGN_IN_OK:
                    User user = (User) msg.obj;
                    signOutOK(user);
                    break;
                case NitingaleMessage.SIGN_IN_FAIL:
                    signOutFail();
                    break;
                default:
                    break;
            }
        }
    };

    private void signOutFail() {
        Toast.makeText(this, "Sign Out Failed", Toast.LENGTH_LONG).show();
        Log.d("canaria", "Sign Out Failed");
    }

    private void signOutOK(User user) {
        Toast.makeText(this, user.toString() + "Sign out ok", Toast.LENGTH_LONG).show();
        Log.d("canaria", "Sign Out OK");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();

        RequestParams params = new RequestParams();
        //params.add();

        String url = "users/me.json";
        client.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, JSONObject jsonObject) {
                Log.d("profile activity: getting user context success", jsonObject.toString());
                JSONObject user = null;
                try {
                    user = jsonObject.getJSONObject("result").getJSONObject("User");
                    String phonenum = user.getString("phone");
                    String id = user.getString("id");
                    String gender = user.getString("gender");

                    mPhonenum.setText(phonenum);
                    mGender.setText(gender);

                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, org.json.JSONObject
                    object) {

                Log.d("Canaria: error", throwable.toString() + object.toString());
            }
        });
        /*
        *   SetUp
        * */
        setupUI();

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RequestParams params = new RequestParams();

                // need to update
                EditText phoneInput = (EditText) findViewById(R.id.phone_number);
                EditText passwordInput = (EditText) findViewById(R.id.password);
                EditText genderInput = (EditText) findViewById(R.id.gender);
                params.add("phone", phoneInput.getText().toString());
                params.add("password", passwordInput.getText().toString());
                params.add("gender", genderInput.getText().toString());

                // NOTE: maybe this is not a good policy!
                // client.postSignOut(params,handler);

                NitingaleHttpRestClient.getInstance().post("users/me.json", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, JSONObject jsonObject) {
                        Log.d("profile activity: getting user context success", jsonObject.toString());
                        Intent intent = new Intent(ProfileActivity.this, MyStatusActivity.class);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, org.json.JSONObject
                            object) {

                        Log.d("Canaria: error", throwable.toString() + object.toString());
                        Toast.makeText(ProfileActivity.this, "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });
            }
        });


    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mPhonenum = (EditText) findViewById(R.id.phone_number);
        mPassword = (EditText) findViewById(R.id.password);
        mGender = (EditText) findViewById(R.id.gender);
        mSigninButton = (TextView) findViewById(R.id.modify_button);

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
                            ProfileActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(ProfileActivity.this.getCurrentFocus().getWindowToken(), 0);
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
