package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import net.nightingalecare.canarymountains.fragments.BaseFragment;
import net.nightingalecare.canarymountains.fragments.SimplePhotoGalleryListFragment;


public class MyTestActivity extends Activity implements BaseFragment.OnFragmentInteractionListener {

    LinearLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_my_status);

        container = (LinearLayout)findViewById(R.id.container0);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View aimView = layoutInflater.inflate(R.layout.layout_aim, null);

        final View aimView1 = layoutInflater.inflate(R.layout.layout_aim, null);
        final View aimView2 = layoutInflater.inflate(R.layout.layout_aim, null);
        container.addView(aimView);
        container.addView(aimView1);
        container.addView(aimView2);

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        BaseFragment targetFragment1 = null;
        targetFragment1 = SimplePhotoGalleryListFragment.newInstance();


        // Select the fragment.
        fragmentManager.beginTransaction()
                .replace(R.id.frag_container1, targetFragment1)
                .commit();

        // Select the fragment.
/*
        BaseFragment targetFragment2 = null;
        targetFragment2 = SimplePhotoGalleryListFragment.newInstance();

        fragmentManager.beginTransaction()
                .replace(R.id.frag_container2, targetFragment2)
                .commit();
*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_noti) {
            return true;
        }
        if (id == R.id.action_open_menu) {
            Intent intent = new Intent(this, HomeMenuActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * Handle Incoming messages from contained fragments.
     */

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onFragmentInteraction(int actionId) {

    }
}
