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

import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.utilities.CBActivity;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;
import net.nightingalecare.canarymountains.utilities.NitingaleMessage;
import net.nightingalecare.canarymountains.Model.User;


/**
 * Created by jae on 8/21/14.
 */
public class SigninActivity extends CBActivity {


    EditText mPhonenum;
    EditText mPassword;
    TextView mSigninButton;
    TextView mDefaultAccount;
    TextView mDefaultAccount1;
    TextView mDefaultExpertAccount;
    TextView mDefaultGovAccount;

    public SigninActivity(){}


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
        setContentView(R.layout.activity_signin);
        final NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        client.postSignOut(params, signOutHandler);
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
                params.add("phone", phoneInput.getText().toString());
                params.add("password", passwordInput.getText().toString());

                // NOTE: maybe this is not a good policy!
                // client.postSignOut(params,handler);
                client.postSignIn(params, signInHandler);
            }
        });


    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mPhonenum = (EditText) findViewById(R.id.phone_number);
        mPassword = (EditText) findViewById(R.id.password);
        mSigninButton = (TextView) findViewById(R.id.signin_button);

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
                            SigninActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(SigninActivity.this.getCurrentFocus().getWindowToken(), 0);
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
