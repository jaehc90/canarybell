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

import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;
import net.nightingalecare.canarymountains.utilities.NitingaleMessage;


/**
 * Created by jae on 8/21/14.
 */
public class SignupActivity extends Activity {

    EditText mNickname;
    EditText mPhonenum;
    EditText mPassword;
    EditText mGender;
    TextView mSignupButton;

    public SignupActivity(){}

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NitingaleMessage.CREATED:
                    String id = (String) msg.obj;
                    OK(id);
                    break;
                case NitingaleMessage.FAILED:
                    Fail();
                    break;
                default:
                    break;
            }
        }
    };
    private void Fail() {
        Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_LONG).show();
    }

    private void OK(String id) {
        Toast.makeText(this, id + " created", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MyStatusActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /*
        *   SetUp
        * */
        setupUI();

        final NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        client.postSignOut(params, handler);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(v.getContext(), "Sinup Button Clicked", Toast.LENGTH_LONG).show();

                RequestParams params = new RequestParams();

                // need to update
                EditText phoneInput = (EditText) findViewById(R.id.phone_number);
                EditText nameInput = (EditText) findViewById(R.id.nickname);
                EditText passwordInput = (EditText) findViewById(R.id.password);
                EditText genderInput = (EditText) findViewById(R.id.gender);
                params.add("phone", phoneInput.getText().toString());
                params.add("name", nameInput.getText().toString());
                params.add("password", passwordInput.getText().toString());
                params.add("gender", genderInput.getText().toString());
                //client.postSignOut(params,handler);
                client.postSignUp(params, handler);
            }
        });
    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mNickname = (EditText) findViewById(R.id.nickname);
        mPhonenum = (EditText) findViewById(R.id.phone_number);
        mPassword = (EditText) findViewById(R.id.password);
        mGender = (EditText) findViewById(R.id.gender);
        mSignupButton = (TextView) findViewById(R.id.signup_button);

        hidekeyboard(findViewById(R.id.signup));
        //hidekeyboard(mPhonenum);
        //hidekeyboard(mPassword);
        //hidekeyboard(mGender);

    }

    public void hidekeyboard(View view)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    Log.d("Social Siren: Add User", "Touch outside");
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            SignupActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(SignupActivity.this.getCurrentFocus().getWindowToken(), 0);
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
