package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;



/**
 * Created by jae on 8/21/14.
 */
public class HomeMenuActivity extends Activity   {

    TextView meView;
    TextView newsfeedView;
    TextView goalView;
    TextView friendView;


    static boolean start = true;

    public HomeMenuActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // set content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);

        View meView = findViewById(R.id.me);
        meView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeMenuActivity.this, MyStatusActivity.class);
                startActivity(intent);
            }
        });

        View messageView = findViewById(R.id.message);
        messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "01063606934");
                smsIntent.putExtra("sms_body","어머님 건강하십니까?");
                startActivity(smsIntent);
            }
        });


        View callView = findViewById(R.id.call);
        callView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "01063606934"));
                startActivity(intent);
            }
        });

        View settingView = findViewById(R.id.setting);
        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeMenuActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        setupUI();

        
    }


    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {



        // create an Intent to take you over to a new DetailActivity
        Intent mainIntent = new Intent(this, MyTestActivity.class);

        // start the next Activity using your prepared Intent
        startActivity(mainIntent);

    }


    public void setupUI() {

        if(start) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            start = false;
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadNotificationList() {


    }


}
