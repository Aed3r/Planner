package com.planner.aeder.planner;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.planner.aeder.planner.Calendar;
import com.planner.aeder.planner.Activity;
import com.planner.aeder.planner.Stats;


public class MainActivity extends AppCompatActivity implements Calendar.OnFragmentInteractionListener, Activity.OnFragmentInteractionListener, Stats.OnFragmentInteractionListener, oteSetupFragment.OnFragmentInteractionListener {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_calendar:
                    fragment = new Calendar();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_activity:
                    fragment = new Activity();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_stats:
                    fragment = new Stats();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment(new Calendar());
    }

    /**
     * loading fragment into FrameLayout
     *
     * @param fragment
     */
    public void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameContainer, fragment);
        transaction.addToBackStack(fragment.toString());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
