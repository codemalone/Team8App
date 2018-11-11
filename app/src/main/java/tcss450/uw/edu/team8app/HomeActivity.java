package tcss450.uw.edu.team8app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Credentials;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WaitFragment.OnFragmentInteractionListener {

    Toolbar toolbar;
    private Location mLocation;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPrefEditor;
    private boolean mUpdateWeather = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        askPermission();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        tcss450.uw.edu.team8app.model.Credentials credentials = (tcss450.uw.edu.team8app.model.Credentials) getIntent().getExtras().get(tcss450.uw.edu.team8app.model.Credentials.CREDIT_TAG);
        TextView username = header.findViewById(R.id.textView_nav_header_username);
        Log.e("test", credentials.getEmail());
        Log.e("test", credentials.getFirstName());
        Log.e("test", credentials.getUsername());
        if (!credentials.getUsername().isEmpty()) {
            username.setText(credentials.getUsername());
        } else {
            username.setText(credentials.getFirstName() + " " + credentials.getLastName());
        }
        TextView email = header.findViewById(R.id.textView_nav_header_email);
        email.setText(credentials.getEmail());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEditor = mPreferences.edit();
        //Long oldTimestamp = Long.valueOf(0);
        Long oldTimestamp = mPreferences.getLong("timestamp", 0);
        Long newTimestamp = System.currentTimeMillis();
        mPrefEditor.putLong("timestamp", newTimestamp);
        mPrefEditor.apply();
//        if (newTimestamp - oldTimestamp > 3600000) {
            mUpdateWeather = true;
//        }

        if (checkPermission()) {
            toolbar.setTitle(getResources().getString(R.string.nav_item_home));
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //if (mUpdateWeather) {
            getLastLocation();
//            sendMyLocation();
            //} else {
            //   handleHomeOnPostExecute(mPreferences.getString("weatherData", null));
            //}
        } else {
            toolbar.setTitle(getResources().getString(R.string.nav_item_home));
            loadFragment(new HomeFragment());
        }

    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
//        String msg = "Updated Location: " +
//                Double.toString(location.getLatitude()) + "," +
//                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        mLocation = location;
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        onLocationChanged(location);
                        sendMyLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
    }

    private void sendMyLocation() {
        if (mLocation == null) return;

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .build();
        //build the JSONObject
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        JSONObject msg = new JSONObject();
        try {
            msg.put("latitude", mLocation.getLatitude());
            msg.put("longitude", mLocation.getLongitude());
            msg.put("zipcode",geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1).get(0).getPostalCode());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleHomeOnPostExecute)
                //.onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    public void askPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    public boolean checkPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void handleHomeOnPostExecute(final String result) {
        mPrefEditor.putString("weatherJSON", result);
        mPrefEditor.apply();
        Bundle args = new Bundle();
        args.putString("weather", result);
        Fragment frag = new HomeFragment();
        frag.setArguments(args);
        onWaitFragmentInteractionHide();
        loadFragment(frag);
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeFragment/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_item_home) {
            if (checkPermission()) {
                toolbar.setTitle(getResources().getString(R.string.nav_item_home));
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                getLastLocation();
//                sendMyLocation();
            } else {
                toolbar.setTitle(getResources().getString(R.string.nav_item_home));
                loadFragment(new HomeFragment());
            }
        } else if (id == R.id.nav_item_connections) {
            toolbar.setTitle(getResources().getString(R.string.nav_item_connections));
            loadFragment(new ConnectionsFragment());
        } else if (id == R.id.nav_item_messages) {
            toolbar.setTitle(getResources().getString(R.string.nav_item_messages));
            loadFragment(new MessagesFragment());
        } else if (id == R.id.nav_item_global_chat) {
            toolbar.setTitle("Global Chat (Test)");
            loadFragment(new ChatFragment());
        } else if (id == R.id.nav_item_settings) {
            toolbar.setTitle(getResources().getString(R.string.nav_item_settings));
            loadFragment(new SettingsFragment());
        } else if (id == R.id.nav_item_logout) {
            logout();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, frag)
                .addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    public void logout() {
//        SharedPreferences prefs =
//                getSharedPreferences(
//                        getString(R.string.keys_shared_prefs),
//                        Context.MODE_PRIVATE);
//        //remove the saved credentials from StoredPrefs
//        prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
//        prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
//
//        //close the app
////        finishAndRemoveTask();
//
//        //or close this activity and bring back the Login
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
//        //End this Activity and remove it from the Activity back stack.
//        finish();
        new DeleteTokenAsyncTask().execute();
    }

    // Deleting the InstanceId (Firebase token) must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onWaitFragmentInteractionShow();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
            try {
                //this call must be done asynchronously.
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                Log.e("FCM", "Delete error!");
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
            //finishAndRemoveTask();
            //or close this activity and bring back the Login
            Intent i = new Intent(getApplication(), MainActivity.class);
            startActivity(i);
            //End this Activity and remove it from the Activity back stack.
            finish();
        }
    }

}
