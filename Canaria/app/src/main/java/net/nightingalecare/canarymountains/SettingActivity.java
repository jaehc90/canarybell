package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


/**
 * Created by jae on 8/21/14.
 */
public class SettingActivity extends Activity {

    TextView mButton0;
    TextView mButton1;
    TextView mButton2;
    TextView mButton3;
    TextView mButton4;
    TextView mButton5;

    public SettingActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        /*
        *   SetUp
        * */
        setupUI();



    }

    public void setupUI() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mButton0 = (TextView) findViewById(R.id.setting_button0);
        mButton1 = (TextView) findViewById(R.id.setting_button1);
        mButton2 = (TextView) findViewById(R.id.setting_button2);
        mButton3 = (TextView) findViewById(R.id.setting_button3);
        mButton4 = (TextView) findViewById(R.id.setting_button4);
        mButton5 = (TextView) findViewById(R.id.setting_button5);

        //hidekeyboard(mPhonenum);
        //hidekeyboard(mPassword);
        //hidekeyboard(mGender);

        mButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ProfileActivity.class);
                startActivity(intent);

            }
        });

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, FriendsActivity.class);
                startActivity(intent);

            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, CheckDeviceInventoryActivity.class);
                startActivity(intent);

            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, SigninActivity.class);
                startActivity(intent);

            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingActivity.this, SigninActivity.class);
                startActivity(intent);

            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(SettingActivity.this, SignupActivity.class);
                startActivity(intent);

            }
        });

    }



}
