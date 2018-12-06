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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tcss450.uw.edu.team8app.chat.ChatListFragment;
import tcss450.uw.edu.team8app.chat.ChatSessionFragment;
import tcss450.uw.edu.team8app.chat.ConversationFragment;
import tcss450.uw.edu.team8app.connections.OnConnectionInteractionListener;
import tcss450.uw.edu.team8app.home.LandingPageFragment;
import tcss450.uw.edu.team8app.model.Connection;
import tcss450.uw.edu.team8app.model.Conversation;
import tcss450.uw.edu.team8app.settings.ChangePasswordFragment;
import tcss450.uw.edu.team8app.settings.ChangeUserFragment;
import tcss450.uw.edu.team8app.settings.SettingsFragment;
import tcss450.uw.edu.team8app.connections.ConnectionsFragment;
import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.model.Message;
import tcss450.uw.edu.team8app.settings.ChangeThemeFragment;
import tcss450.uw.edu.team8app.utils.GcmKeepAlive;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.Themes;
import tcss450.uw.edu.team8app.utils.WaitFragment;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WaitFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, ChangeThemeFragment.OnFragmentInteractionListener,
        ConnectionsFragment.OnListFragmentInteractionListener, ChangePasswordFragment.OnFragmentInteractionListener,
        ChangeUserFragment.OnFragmentInteractionListener,
        ConversationFragment.OnListFragmentInteractionListener, ChatSessionFragment.ChatSessionListener {

    static boolean active = false;
    Toolbar toolbar;
    private Location mLocation;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPrefEditor;
    private boolean mUpdateWeather = false;
    private Credentials mCredentials;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GcmKeepAlive gka = new GcmKeepAlive(this);
        gka.broadcastIntents();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPreferences != null) {
            if (mPreferences.getString(Themes.TAG, "") != null) {
                String theme = mPreferences.getString(Themes.TAG, "");

                this.setTheme(Themes.getTheme(theme).getId());
            } else {
                mPreferences.edit().putString(Themes.TAG, Themes.Default.toString()).apply();
            }
        }
        //this.setTheme(Themes.getTheme("FruitSalad").getId());
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.e("GET TOKEN TEST", FirebaseInstanceId.getInstance().getToken());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        askPermission();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        Credentials credentials = (Credentials) getIntent().getExtras().get(Credentials.CREDIT_TAG);
        mCredentials = credentials;
        TextView username = header.findViewById(R.id.textView_nav_header_username);
        if (!credentials.getUsername().isEmpty()) {
            username.setText(credentials.getUsername());
        } else {
            username.setText(credentials.getFirstName() + " " + credentials.getLastName());
        }
        TextView email = header.findViewById(R.id.textView_nav_header_email);
        email.setText(credentials.getEmail());

        if (getIntent().getBooleanExtra("from_connection_notification", false)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("from_connection_notification", true);
            ConnectionsFragment frag = new ConnectionsFragment();
            frag.setArguments(bundle);
            loadFragment(frag);
        } else {
            if (checkPermission()) {
                toolbar.setTitle(getResources().getString(R.string.nav_item_home));
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                getLastLocation();
//            sendMyLocation();
            } else {
                toolbar.setTitle(getResources().getString(R.string.nav_item_home));
                loadFragment(new LandingPageFragment());
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        GcmKeepAlive gka = new GcmKeepAlive(this);
        gka.broadcastIntents();
    }

    public static boolean isActive() {
        return active;
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
                    toolbar.setTitle(getResources().getString(R.string.nav_item_home));
                    loadFragment(new LandingPageFragment());
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
            msg.put("zipcode", geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1).get(0).getPostalCode());
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

    public void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    public boolean checkPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void handleHomeOnPostExecute(final String result) {
        Bundle args = new Bundle();
        args.putString("weather", result);
        args.putDouble("lat", mLocation.getLatitude());
        args.putDouble("lng", mLocation.getLongitude());
        Fragment frag = new LandingPageFragment();
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
    public void onSuccessChangeUsername(String newUsername) {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        prefs.edit().putString(getString(R.string.keys_prefs_username), newUsername).apply();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView username = header.findViewById(R.id.textView_nav_header_username);

        mCredentials = new Credentials.Builder(mCredentials.getEmail(), mCredentials.getPassword())
                .addFirstName(mCredentials.getFirstName())
                .addLastName(mCredentials.getFirstName())
                .addUsername(newUsername)
                .build();

        username.setText(newUsername);

        if (checkPermission()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            getLastLocation();
        } else {
            loadFragment(new LandingPageFragment());
        }

        DialogFragment fragment = new DisplayMessageDialog();
        Bundle args = new Bundle();
        args.putSerializable(DisplayMessageDialog.TAG, getString(R.string.change_username_success));
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), DisplayMessageDialog.TAG);
    }

    @Override
    public void clickedChangePassword() {
        Fragment fragment = new ChangePasswordFragment();
        Bundle args = new Bundle();
        args.putSerializable(Credentials.CREDIT_TAG, mCredentials);
        fragment.setArguments(args);
        loadFragment(fragment);
    }

    @Override
    public void clickedChangeUsername() {
        Fragment fragment = new ChangeUserFragment();
        loadFragment(fragment);
    }

    @Override
    public void changePasswordSuccess(String newPassword) {
        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_password), newPassword).apply();

        toolbar.setTitle(getResources().getString(R.string.nav_item_home));

        if (checkPermission()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            getLastLocation();
        } else {
            loadFragment(new LandingPageFragment());
        }

        DialogFragment fragment = new DisplayMessageDialog();
        Bundle args = new Bundle();
        args.putSerializable(DisplayMessageDialog.TAG, getString(R.string.change_password_success));
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), DisplayMessageDialog.TAG);
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
        // automatically handle clicks on the LandingPageFragment/Up button, so long
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
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                getLastLocation();
//                sendMyLocation();
            } else {
                loadFragment(new LandingPageFragment());
            }
        } else if (id == R.id.nav_item_connections) {
            loadFragment(new ConnectionsFragment());
        } else if (id == R.id.nav_item_messages) {
            onCreateChat();
        } else if (id == R.id.nav_item_settings) {
            loadFragmentNoBackStack(new SettingsFragment());
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

    private void loadFragmentNoBackStack(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, frag);
        // Commit the transaction
        transaction.commit();
    }

    public void logout() {
        new DeleteTokenAsyncTask().execute();
    }

    //callAsyncTaskGetConnectionMessages(item.getEmail());
    private void callAsyncTaskGetConnectionMessages(final String email) {
        onWaitFragmentInteractionShow();

        Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_add))
                .build();

        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("token", FirebaseInstanceId.getInstance().getToken());
            messageJson.put("theirEmail", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionMessagesGetOnPostExecute)
                .onCancelled(error -> Log.e("ERROR!", error))
                .build().execute();
    }

    private void handleConnectionMessagesGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            JSONObject data = null;
            if (root.getBoolean("success")) {
                List<Message> messages = new ArrayList<>();
                if (root.has("data")) {
                    data = root.getJSONObject("data");
                    if (data.has("messages")) {
                        JSONArray dataMessages = data.getJSONArray("messages");
                        for (int index = 0; index < dataMessages.length(); index++) {
                            JSONObject jsonMsg = dataMessages.getJSONObject(index);
                            messages.add(new Message.Builder(jsonMsg.getString("username"),
                                    jsonMsg.getString("message"),
                                    jsonMsg.getString("timestamp"))
                                    .build());
                        }
                    }
                }
                Message[] messagesAsArray = new Message[messages.size()];
                messagesAsArray = messages.toArray(messagesAsArray);
                Bundle args = new Bundle();
                args.putSerializable(ChatSessionFragment.ARG_MESSAGE_LIST, messagesAsArray);
                if (data != null && data.has("chatId")) {
                    args.putSerializable(ChatSessionFragment.TAG, data.getString("chatId"));
                } else {
                    throw new JSONException("chatId has no value.");
                }
                Fragment frag = new ChatSessionFragment();
                frag.setArguments(args);
                onWaitFragmentInteractionHide();
                loadFragment(frag);
                //toolbar.setTitle();
            } else {
                Log.e("ERROR!", "Connections did not succeed in post");
                onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onConversationInteraction(Conversation item) {
        Uri uri = new Uri.Builder()
            .scheme(getString(R.string.ep_scheme))
            .encodedAuthority(getString(R.string.ep_base_url))
            .appendPath(getString(R.string.ep_chats))
            .appendPath(getString(R.string.ep_messaging_base)).appendPath(getString(R.string.ep_get_all))
            .build();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("token", FirebaseInstanceId.getInstance().getToken());
            messageJson.put("chatId", item.getChatID());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleConnectionMessagesGetOnPostExecute)
                .onCancelled(error -> Log.e("ERROR!", error))
                .build().execute();
    }

    @Override
    public void onCreateChat() {
            Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
              .encodedAuthority(getString(R.string.ep_base_url)).appendPath(getString(R.string.ep_chats))
         .appendPath(getString(R.string.ep_details))
         .build();
         JSONObject messageJson = new JSONObject();
         try {
         messageJson.put("token", FirebaseInstanceId.getInstance().getToken());
         } catch (JSONException e) {
         e.printStackTrace();
         Log.e("ERROR!", e.getMessage());
         }
         new SendPostAsyncTask.Builder(uri.toString(), messageJson)
         .onPreExecute(this::onWaitFragmentInteractionShow)
         .onPostExecute(this::handleMessageListGetOnPostExecute)
         .onCancelled(error -> Log.e("ERROR!", error))
         .build().execute();
    }

    /**private void handleIdChatGetOnPostExecute(final String result) {
        try {

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            onWaitFragmentInteractionHide();
        }
    }*/

    private void handleMessageListGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if(root.has("success") && root.getBoolean("success")) {
                if(root.has("data")) {
                    JSONArray jsonArray = root.getJSONArray("data");
                    List<Conversation> conversations = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String chatid = object.getString("chatid");
                        JSONArray users = object.getJSONArray("users");
                        List<String> userString = new ArrayList<>();
                        for(int j = 0; j < users.length(); j++) {
                            if(!users.getString(j).equals(mCredentials.getUsername())
                                    && !users.getString(j).equals(mCredentials.getEmail())) {
                                userString.add(users.getString(j));
                            }
                        }
                        String lastMessage = object.getString("recentMessage");
                        Conversation conversation = new Conversation(chatid, userString, lastMessage);
                        conversations.add(conversation);
                    }
                    Bundle bundle = new Bundle();
                    Conversation[] conversationsAsArray = new Conversation[conversations.size()];
                    conversationsAsArray = conversations.toArray(conversationsAsArray);
                    bundle.putSerializable(ConversationFragment.TAG, conversationsAsArray);
                    Fragment frag = new ConversationFragment();
                    frag.setArguments(bundle);
                    onWaitFragmentInteractionHide();
                    getSupportActionBar().setTitle("Messages");
                    loadFragment(frag);
                }
            }

        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onStartChatInteraction(Connection connection) {
        callAsyncTaskGetConnectionMessages(connection.getEmail());
    }

    @Override
    public void returnToChatList() {
        onCreateChat();
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

            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //remove the saved credentials from StoredPrefs
            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

            //close the app
            //finishAndRemoveTask();
            //or close this activity and bring back the Login
            Intent i = new Intent(getApplication(), MainActivity.class);
            startActivity(i);
            //End this Activity and remove it from the Activity back stack.
            finish();
        }
    }

    @Override
    public void clickedChangeTheme() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_home_container, new ChangeThemeFragment());
        transaction.commit();
    }

    public void selectTheme(Themes theme) {
        //SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        //prefs.edit().putString(Themes.TAG, theme.toString());
        if (mPreferences != null) {
            mPreferences.edit().putString(Themes.TAG, theme.toString()).apply();
        }
        /**FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
         .replace(R.id.frame_home_container, new LandingPageFragment());
         transaction.commit();*/
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Credentials.CREDIT_TAG, mCredentials);
        startActivity(intent);
        //End this activity and remove it from the Activity back stack.
        finish();
    }


    /**
     * Global chat menu option
     **/
    private void callAsyncTaskGetMessages(final String chatId) {
        final String TAG = "getAllMessages";
        onWaitFragmentInteractionShow();

        Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_get_all))
                .build();

        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatId", chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPostExecute(this::handleConnectionMessagesGetOnPostExecute)
                .onCancelled(error -> Log.e(TAG, error))
                .build().execute();
    }

    private void handleMessagesGetOnPostExecute(final String result) {
        //toolbar.setTitle("Global Chat (Test)");
        //loadFragment(new ChatSessionFragment());

        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("messages")) {
                JSONArray data = root.getJSONArray("messages");
                List<Message> messages = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonMsg = data.getJSONObject(i);
                    messages.add(new Message.Builder(jsonMsg.getString("email"),
                            jsonMsg.getString("message"),
                            jsonMsg.getString("timestamp"))
                            .build());
                }
                Message[] messagesAsArray = new Message[messages.size()];
                messagesAsArray = messages.toArray(messagesAsArray);
                Bundle args = new Bundle();
                args.putSerializable(ChatSessionFragment.ARG_MESSAGE_LIST, messagesAsArray);
                Fragment frag = new ChatSessionFragment();
                frag.setArguments(args);
                onWaitFragmentInteractionHide();
                toolbar.setTitle("Global Chat (Test)");
                loadFragment(frag);
            } else {
                Log.e("ERROR!", "No data array");
                //notify user
                onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }


}
