package net.nightingalecare.canarymountains.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.Model.User;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Jagur
 * Date: 14. 8. 19
 * Time: 오후 4:09
 * To change this template use File | Settings | File Templates.
 */
public class NitingaleHttpRestClient {

    private static final String SIGNATURE = "Canaria";
    private static final String BASE_URL = "http://210.122.7.84/ntdemo/";
    private static final String IMAGE_URL = "http://210.122.7.84/ntdemo/app/webroot/img/";

    public static final String SUMMARY_URL = "users/me/sensors.json";
    public static final String USER_URL = "users.json";
    public static final String SUBSCRIBE_URL = "users/watch.json";
    public static final String LOGOUT_URL = "users/logout.json";
    public static final String LOGIN_URL = "users/login.json";
    public static final String ME_URL = "users/me.json";

    public static final String SENSOR_REGISTER_URL = "sensors.json";
    public static final String SENSOR_EVENT_URL = "sensors/event.json";

    private static NitingaleHttpRestClient instance;

    private static AsyncHttpClient client;

    public static synchronized NitingaleHttpRestClient getInstance() {
        if (instance == null) {
            instance = new NitingaleHttpRestClient();
        }
        return instance;
    }

    private NitingaleHttpRestClient(){
        client = new AsyncHttpClient();
    };

    public AsyncHttpClient getAsyncHttpClient(){ return client;}

    public void setCookieStore(PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
    }

    public void get(String relativeUrl, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(relativeUrl), params, responseHandler);
    }

    public void post(String relativeUrl, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(relativeUrl), params, responseHandler);
    }

    public void post(String relativeUrl, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(relativeUrl), responseHandler);
    }

    public void delete(String relativeUrl, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(relativeUrl), responseHandler);
    }

    public void delete(Context context, String relativeUrl, Header[] headers,  RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(context, getAbsoluteUrl(relativeUrl), headers, params,responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static String getAbsoluteRestUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static String getAbsoluteImageUrl(String relativeUrl) {
        return IMAGE_URL + relativeUrl;
    }

    public void postSignIn(RequestParams params, final Handler handler){

        String loginURLAbsolute = getAbsoluteUrl(LOGIN_URL);
        client.post(loginURLAbsolute, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                User user = new User();
                Log.d(SIGNATURE, "sign in : " + Integer.toString(statusCode));

                try {
                    JSONObject resultObject= jsonObject.getJSONObject("result");
                    JSONObject userObject = resultObject.getJSONObject("User");
                    user.setId(userObject.getInt("id"));
                    user.setPhone(userObject.getString("phone"));
                    user.setName(userObject.optString("name"));
                    user.setUnreadNotifications(userObject.optString("unreadNotifications"));
                } catch (JSONException e) {
                    Message msg = handler.obtainMessage(NitingaleMessage.SIGN_IN_FAIL);
                    handler.sendMessage(msg);
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    return;
                }

                Message msg = handler.obtainMessage(NitingaleMessage.SIGN_IN_OK, user);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {

                throwable.printStackTrace();
                if(object != null) Log.d("Canaria", object.toString());
                Message msg = handler.obtainMessage(NitingaleMessage.SIGN_IN_FAIL);
                handler.sendMessage(msg);

            }
        });
    }

    public void postSignOut(RequestParams params, final Handler handler) {
        client.post(getAbsoluteUrl(LOGOUT_URL), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Log.d(SIGNATURE, "sign out success: " + Integer.toString(statusCode));
                Message msg = handler.obtainMessage(NitingaleMessage.CREATED);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.d(SIGNATURE, "sign out failed: " + Integer.toString(statusCode));
                Message msg = handler.obtainMessage(NitingaleMessage.FAILED);
                handler.sendMessage(msg);
            }
        });
    }


    public void postSignUp(RequestParams params, final Handler handler){
        client.post(getAbsoluteUrl("users.json"), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Log.d(SIGNATURE, "Sign Up user : " + Integer.toString(statusCode));
                try {
                    JSONObject resultObject= jsonObject.getJSONObject("result");
                    Message msg = handler.obtainMessage(NitingaleMessage.CREATED, resultObject.getString("id"));
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    Message msg = handler.obtainMessage(NitingaleMessage.FAILED);
                    handler.sendMessage(msg);
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Message msg = handler.obtainMessage(NitingaleMessage.FAILED);
                handler.sendMessage(msg);
            }
        });
    }


}
