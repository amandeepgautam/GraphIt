package edu.unm.twin_cities.graphit.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.fragments.AddDeviceFragment;
import edu.unm.twin_cities.graphit.fragments.AddSensorFragment;
import edu.unm.twin_cities.graphit.fragments.FileBrowserFragment;
import edu.unm.twin_cities.graphit.fragments.PlotFragment;
import edu.unm.twin_cities.graphit.fragments.UpdateFragment;

public class DrawerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private DrawerLayout drawerLayout;

    private ListView navList;

    private FragmentManager fragmentManager;

    private FragmentTransaction fragmentTransaction;

    NavigationView nvDrawer;

    Fragments currFragment;

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_with_spinner);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(Color.CYAN);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        // There is usually only 1 header view.
        // Multiple header views can technically be added at runtime.
        // We can use navigationView.getHeaderCount() to determine the total number.
        View headerLayout = nvDrawer.getHeaderView(0);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                null, R.string.drawer_open,  R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        fragmentManager = getSupportFragmentManager();

        if (savedBundleInstance == null) {
            loadFragment(Fragments.PLOT_FRAGMENT, null, false);
        } else {
            //TODO: check SO on saving fragments.
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragments fragment;
        switch(menuItem.getItemId()) {
            case R.id.add_device_fragment:
                fragment = Fragments.ADD_DEVICE;
                break;
            case R.id.add_sensor_fragment:
                fragment = Fragments.ADD_SENSOR;
                break;
            case R.id.download_new_data:
                fragment = Fragments.UPDATE_GRAPH;
                break;
            case R.id.graphs:
                fragment = Fragments.PLOT_FRAGMENT;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported menu item. Expected fragment does not exist.");
        }
        loadFragment(fragment, null, false);
        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        //setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        /*if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navList)){
                drawerLayout.closeDrawer(navList);
            } else {
                drawerLayout.openDrawer(navList);
            }
        }*/
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadFragment(Fragments fragmentId, Bundle bundle, boolean getFromBackStack) {
        Fragment fragment = null;
        int entry = 0;

        fragmentTransaction = fragmentManager.beginTransaction();
        String tag = fragmentId.name();

        if (getFromBackStack) {
            entry = fragmentManager.getBackStackEntryCount();
            String backEntryName;
            do {
                --entry;
                FragmentManager.BackStackEntry backEntry = fragmentManager
                        .getBackStackEntryAt(entry);
                backEntryName = backEntry.getName();
                if (backEntryName.equals(fragmentId.name()))  {
                    break;
                }
            } while ( entry > 0);

            if (entry != 0) {
                fragment =  fragmentManager.findFragmentByTag(backEntryName);
                fragmentTransaction.replace(R.id.content_frame, fragment, tag);
            }
        }
        if (entry == 0) {
            switch (fragmentId) {
                case PLOT_FRAGMENT:
                    fragment = new PlotFragment();
                    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
                    break;
                case ADD_DEVICE:
                    fragment = new AddDeviceFragment();
                    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
                    break;
                case ADD_SENSOR:
                    fragment = new AddSensorFragment();
                    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
                    break;
                case FILE_BROWSER:
                    fragment = new FileBrowserFragment();
                    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
                    break;
                case UPDATE_GRAPH:
                    fragment = new UpdateFragment();
                    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
                    break;
                default:
                    throw new IllegalArgumentException("Not a valid fragment");
            }
        }

        fragmentTransaction.addToBackStack(tag);

        // pass the information sent from another fragment.
        if (bundle != null) {
            Bundle existingBundlle = fragment.getArguments();
            existingBundlle.putAll(bundle);
        }

        fragmentTransaction.commit();
        currFragment = fragmentId;
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        loadFragment(Fragments.getFragmentType(position), null, false);
        drawerLayout.closeDrawer(navList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
//        fragmentManager.putFragment(outState, "mContent", currFragment);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                // Do not do anything on first click outside edit text. Treat this as a way to
                // remove the soft keypad.
                return true;
            }
        }
        return super.dispatchTouchEvent( event );
    }

    /**
     * The numbers of enum should be unique and in sync with the position on
     * the drawer navigation list, if present on the list.
     */
    public enum Fragments {
        PLOT_FRAGMENT (10),
        FILE_BROWSER(11),
        ADD_DEVICE(0),
        ADD_SENSOR(1),
        UPDATE_GRAPH(2);

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
