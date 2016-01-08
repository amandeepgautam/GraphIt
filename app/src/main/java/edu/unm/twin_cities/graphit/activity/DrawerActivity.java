package edu.unm.twin_cities.graphit.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.fragments.AddDeviceFormFragment;
import edu.unm.twin_cities.graphit.fragments.AddDeviceFragment;
import edu.unm.twin_cities.graphit.fragments.AddSensorFragment;
import edu.unm.twin_cities.graphit.fragments.PlotFragment;
import lombok.Data;

public class DrawerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private DrawerLayout drawerLayout;

    private ListView navList;

    private FragmentManager fragmentManager;

    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(Color.CYAN);

        navList = (ListView) findViewById(R.id.left_drawer);

        String[] navigationOptions = getResources().getStringArray(R.array.nav_actions);
        navList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navigationOptions));
        navList.setOnItemClickListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                null, R.string.app_name, R.string.app_name);

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        fragmentManager = getSupportFragmentManager();

        loadFragment(Fragments.PLOT_FRAGMENT, null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navList)){
                drawerLayout.closeDrawer(navList);
            } else {
                drawerLayout.openDrawer(navList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadFragment(Fragments fragmentId, Bundle bundle) {
        Fragment fragment = null;
        switch (fragmentId) {
            case PLOT_FRAGMENT:
                fragment = new PlotFragment();
                break;
            case ADD_DEVICE:
                fragment = new AddDeviceFragment();
                break;
            case ADD_SENSOR:
                fragment = new AddSensorFragment();
                break;
            case ADD_DEVICE_FORM:
                fragment = new AddDeviceFormFragment();
                break;
            default:
                throw new IllegalArgumentException("Not a valid fragment");
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        // pass the information sent from another fragment.
        if (bundle != null)
            fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        loadFragment(Fragments.getFragmentType(position), null);
        drawerLayout.closeDrawer(navList);
    }

    /**
     * The numbers of enum should be unique and in sync with the position on
     * the drawer navigation list, if present on the list.
     */
    public enum Fragments {
        PLOT_FRAGMENT (10),
        ADD_DEVICE_FORM(11),
        ADD_DEVICE(0),
        ADD_SENSOR(1);

        private int position;

        private Fragments(int position) {
            this.position = position;
        }

        public static Fragments getFragmentType(int position) {
            for(Fragments fragment: Fragments.values()) {
                if(fragment.position == position) {
                    return fragment;
                }
            }
            return null;// not found
        }
    }
}
