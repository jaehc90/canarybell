package net.nightingalecare.canarymountains.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.nightingalecare.canarymountains.R;
import net.nightingalecare.canarymountains.SplashActivity;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Jagur
 * Date: 13. 4. 24
 * Time: 오후 10:03
 * To change this template use File | Settings | File Templates.
 */
public class GCMIntentService extends GCMBaseIntentService {

    @Override
    protected void onMessage(Context context, Intent intent) {
        //To change body of implemented methods use File | Settings | File Templates.
        Bundle b = intent.getExtras();
        Iterator<String> iterator = b.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            String value = b.get(key).toString();
            if(key.equals("Head")){
                setNotification(context,key,value);
            }
            Log.d("INFO", "onMessage. " + key + " : " + value);
        }
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    }
    private void setNotification(Context context, String title, String message) {
        NotificationManager notificationManager = null;
        Notification notification = null;
        try {
            notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notification = new Notification(R.drawable.ic_launcher,
                    message, System.currentTimeMillis());
            notification.setLatestEventInfo(context, title, message, null);
            notificationManager.notify(0, notification);
        } catch (Exception e) {
            Log.e("INFO", "[setNotification] Exception : " + e.getMessage());
        }
    }

    @Override
    protected void onError(Context context, String errorId) {
        //To change body of implemented methods use File | Settings | File Templates.
        Log.d("GCMIntentService","Registration Failed");
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        //To change body of implemented methods use File | Settings | File Templates.
        SplashActivity.token = registrationId;
        updateToken(registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private void updateToken(String token) {

        NitingaleHttpRestClient NTGClient = NitingaleHttpRestClient.getInstance();
        RequestParams params = new RequestParams();
        params.add("deviceToken", token);
        String url = "users/me/gcm.json";

        NTGClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                Log.d("Canaria: Update Token Success!", jsonObject.toString());

            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error) {

                // Log error message to help solve any problems
                Log.e("Canaria: Update Error", statusCode + " " + throwable.getMessage() + error.toString());

            }
        });
    }
}
