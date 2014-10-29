package net.nightingalecare.canarymountains.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * Created by jae on 8/22/14.
 */
public class CBActivity extends Activity {

    protected static final String IMAGE_URL_BASE = "http://128.199.145.40/advise/app/webroot/img/";

    public static final String MyPREFERENCES = "CBPrefs" ;

    public static final String normalUserDefaultId = "01099999999";
    public static final String normalUserDefaultId1 = "01155555555";
    public static final String expertlUserDefaultId = "01088888888";
    public static final String govUserDefaultId = "01066666666";

    public static final String normalUserDefaultPwd = "123456";
    public static final String expertlUserDefaultPwd = "123456";
    public static final String govUserDefaultPwd = "123456";

    public CBActivity(){}

    private String userName;
    private String userId;
    private String loginId;
    private String password;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loadFromSharePreference();
    }

    public boolean containsUserPref(){
        SharedPreferences mPrefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        return mPrefs.contains("userName");
    }

    public void loadFromSharePreference(){
        SharedPreferences mPrefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (mPrefs.contains("userName")) {
            userName = mPrefs.getString("userName","");
        }
        if (mPrefs.contains("userId")) {
            userId = mPrefs.getString("userId","");
        }
        if (mPrefs.contains("loginId")) {
            loginId = mPrefs.getString("loginId","");
        }
        if (mPrefs.contains("password")) {
            password = mPrefs.getString("password","");
        }
    }

    public void removeFromSharePreference(){

        SharedPreferences mPrefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.commit();
    }

    public void setIntoSharePreference(String userId, String userName, String loginId, String password){

        SharedPreferences mPrefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putString("userId", userId);
        editor.putString("userName", userName);
        editor.putString("loginId", loginId);
        editor.putString("password", password);
    }


    protected Handler handler = new Handler(){
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
        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
    }

    private void OK(String id) {
        Toast.makeText(this, " Success: Created" + id, Toast.LENGTH_LONG).show();
    }
/*
    protected void whoAmI(){
        NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        client.getWhoAmI(params, handler);
    }
*/

    /* HACK please remove*/
    protected void defaultSignIn() {

        NitingaleHttpRestClient client = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        params.add("phone", "01099999999");
        params.add("password", "123456");
        client.postSignIn(params, handler);
    }


    protected void hidekeyboard(View view){

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    Log.d("Add Event", "Touch outside");
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            CBActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(CBActivity.this.getCurrentFocus().getWindowToken(), 0);
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

    private void popupToast(String str)
    {
        Toast.makeText(getApplicationContext(),
                str,
                Toast.LENGTH_LONG).show();

    }


    protected boolean checkSignedIn() {

        PersistentCookieStore store = new PersistentCookieStore(this);
        List<Cookie> cookieList = store.getCookies();
        Cookie cookie;

        for(int i = 0; i < cookieList.size() ; i++ )
        {
            cookie = cookieList.get(i);
            if(cookie.getName().equals("Canary Bell")) {
                return true;
            }
        }

        return false;
    }
}
