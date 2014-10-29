package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.Model.User;
import net.nightingalecare.canarymountains.utilities.NitingaleHttpRestClient;
import net.nightingalecare.canarymountains.utilities.NitingaleMessage;

public class SplashActivity extends Activity {

    private static final int DELAY = 1000;
    private static final String projectId = "904802783443";
    public static String token = null;
    private boolean updateFlag = false;

    public final static String USER_DATA_PREFERENCE = "UserPref";
    String devices;
    String deviceAddresses[];

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        String SENDER_ID = projectId;
        String regId = GCMRegistrar.getRegistrationId(this);

        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
            token = GCMRegistrar.getRegistrationId(this);
            Log.d("INFO", "REGID : " + token);
            updateFlag = true;
        } else {
            Log.d("INFO", "Already registered");
            Log.d("INFO", "REGID : " + regId);
            token = regId;
        }

        PersistentCookieStore store = new PersistentCookieStore(getApplicationContext());
        store.clear();

        NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        params.add("phone", "01011112223");
        params.add("password", "1234567890");
        client.postSignIn(params, signInHandler);

        new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				final Intent intent = new Intent(SplashActivity.this, MyStatusActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				finish();
			}
		}, DELAY);
	}

    @Override
	public void onBackPressed() {
		// do nothing. Protect from exiting the application when splash screen is shown
	}

    protected Handler signInHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case NitingaleMessage.SIGN_IN_OK:
                    User user = (User) msg.obj;
                    SharedPreferences sharedPreferences = getSharedPreferences ("UserPref", Context.MODE_PRIVATE );
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userId", String.valueOf(user.getId()) );
                    editor.putString("name", String.valueOf(user.getName()) );
                    editor.putString("phone", String.valueOf(user.getPhone()) );
                    editor.commit();
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
        Toast.makeText(this,  " sign in ok", Toast.LENGTH_LONG).show();
        Log.d("Social Siren", "User Sign OK" + user.toString() );
        // updateToken(token);
    }

}
