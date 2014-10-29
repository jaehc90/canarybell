package net.nightingalecare.canarymountains;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import net.nightingalecare.canarymountains.fragments.BaseFragment;
import net.nightingalecare.canarymountains.fragments.SimplePhotoGalleryListFragment;


public class MyTestActivity1 extends Activity implements BaseFragment.OnFragmentInteractionListener {

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        container = (LinearLayout)findViewById(R.id.container);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View locationView = layoutInflater.inflate(R.layout.layout_location, null);
        final View picHorizonView = layoutInflater.inflate(R.layout.layout_picture_horizontal, null);
        final View picVerticalView = layoutInflater.inflate(R.layout.layout_picture_vertical, null);

        container.addView(locationView);
        container.addView(picHorizonView);
        container.addView(picVerticalView);

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        BaseFragment targetFragment1 = null;
        targetFragment1 = SimplePhotoGalleryListFragment.newInstance();
        BaseFragment targetFragment2 = null;
        targetFragment2 = SimplePhotoGalleryListFragment.newInstance();

        // Select the fragment.
        fragmentManager.beginTransaction()
                .replace(R.id.frag_container1, targetFragment1)
                .commit();

        // Select the fragment.

        fragmentManager.beginTransaction()
                .replace(R.id.frag_container2, targetFragment2)
                .commit();

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
