package tcss450.uw.edu.team8app.home;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.Themes;
import tcss450.uw.edu.team8app.utils.WaitFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class LandingPageFragment extends Fragment {

    Set<String> savedZips;
    private TextView weatherLocation, weatherCurrentTemp, weatherCurrentMisc;
    private SharedPreferences mPreferences;
    private Geocoder mGeocoder;
    private ArrayAdapter<String> mAdapter;
    private JSONArray weather;
    private Spinner mDropdown;

    public LandingPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Home");
        if (getArguments() != null) {
            try {
                weather = new JSONArray(getArguments().getString("weather"));
                JSONObject hourlyWeather = weather.getJSONObject(1);
                JSONObject dailyWeather = weather.getJSONObject(0);
                JSONArray hourlyWeatherData = hourlyWeather.getJSONArray("data");
                JSONArray dailyWeatherData = dailyWeather.getJSONArray("data");
                JSONObject currentWeather = hourlyWeatherData.getJSONObject(0);

                String packageName = getActivity().getPackageName();
                weatherLocation = v.findViewById(R.id.textView_weather_location);
                weatherCurrentTemp = v.findViewById(R.id.textView_weather_current_temp);
                weatherCurrentMisc = v.findViewById(R.id.textView_weather_current_misc);
                weatherLocation.setText(hourlyWeather.getString("city_name") + ", " + hourlyWeather.getString("state_code"));
                weatherCurrentTemp.setText(currentWeather.getString("temp") + " °C");
                weatherCurrentMisc.setText("Precipitation: " + currentWeather.getString("pop") + " %\nHumidity: " + currentWeather.getString("rh") + " %\nWind: " + currentWeather.getString("wind_spd") + " mph");
                ImageView currentWeatherIcon = v.findViewById(R.id.imageView_weather_current_icon);
                String currentWeatherIconID = hourlyWeatherData.getJSONObject(0).getJSONObject("weather").getString("icon");
                currentWeatherIcon.setImageResource(getResources().getIdentifier(currentWeatherIconID, "drawable", packageName));
                TextView current;
                String time = hourlyWeatherData.getJSONObject(0).getString("timestamp_local").split("T", 2)[1];
                time = time.split(":")[0];
                int timeAsInteger = Integer.parseInt(time);
                for (int i = 0; i < 24; i++) {
                    current = v.findViewById(getResources().getIdentifier("textView_weather_hourly_temp" + (i + 1), "id", packageName));
                    timeAsInteger = timeAsInteger % 25;
                    if (timeAsInteger == 0) {
                        timeAsInteger++;
                    }
                    current.setText(timeAsInteger + ":00\n" + hourlyWeatherData.getJSONObject(i).getString("temp") + " °C");
                    timeAsInteger++;
                }
                for (int i = 0; i < 10; i++) {
                    current = v.findViewById(getResources().getIdentifier("textView_weather_daily_day" + (i + 1), "id", packageName));
                    String date = dailyWeatherData.getJSONObject(i).getString("valid_date");
                    String[] dateParsed = date.split("-", 3);
                    date = dateParsed[1] + "/" + dateParsed[2];
                    current.setText(date);
                    current = v.findViewById(getResources().getIdentifier("textView_weather_daily_temp" + (i + 1), "id", packageName));
                    current.setText(dailyWeatherData.getJSONObject(i).getString("temp") + " °C");
                    ImageView icon = v.findViewById(getResources().getIdentifier("imageView_weather_daily_icon" + (i + 1), "id", packageName));
                    String iconID = dailyWeatherData.getJSONObject(i).getJSONObject("weather").getString("icon");
                    icon.setImageResource(getResources().getIdentifier(iconID, "drawable", packageName));
                }

                mGeocoder = new Geocoder(getActivity(), Locale.getDefault());

                mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                savedZips = mPreferences.getStringSet("ZIPCODES", null);
                if (savedZips != null) {
                    savedZips = new HashSet<String>(savedZips);
                }
                mDropdown = v.findViewById(R.id.spinner_zipcodes);
                mDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getItemAtPosition(i).toString().equals("(SAVE)")) {
                            adapterView.setSelection(0);
                            savedZips.add(adapterView.getItemAtPosition(0).toString());
                            Toast toast = Toast.makeText(getActivity(), "Zipcode Saved", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            refreshDropDownList();
                        } else if (adapterView.getItemAtPosition(i).toString().equals("(DEL)")) {
                            adapterView.setSelection(0);
                            savedZips.remove(adapterView.getItemAtPosition(0).toString());
                            Toast toast = Toast.makeText(getActivity(), "Zipcode Removed", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            refreshDropDownList();
                        } else {
                            try {
                                if (!weather.getJSONObject(2).getString("zip").equals(adapterView.getItemAtPosition(i).toString())) {
                                    List<Address> addresses;
                                    try {
                                        addresses = mGeocoder.getFromLocationName(adapterView.getItemAtPosition(i).toString(), 1);
                                        if (addresses != null && !addresses.isEmpty()) {
                                            Uri uri = new Uri.Builder()
                                                    .scheme(getString(R.string.ep_scheme))
                                                    .encodedAuthority(getString(R.string.ep_base_url))
                                                    .appendPath(getString(R.string.ep_weather))
                                                    .build();
                                            //build the JSONObject
                                            JSONObject msg = new JSONObject();
                                            try {
                                                msg.put("latitude", addresses.get(0).getLatitude());
                                                msg.put("longitude",addresses.get(0).getLongitude());
                                                if (addresses.size() == 0) {
                                                    msg.put("zipcode", "");
                                                } else {
                                                    msg.put("zipcode", addresses.get(0).getPostalCode());
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            new SendPostAsyncTask.Builder(uri.toString(), msg)
                                                    .onPreExecute(this::onWaitFragmentInteractionShow)
                                                    .onPostExecute(this::handleHomeOnPostExecute)
                                                    .build().execute();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putStringSet("ZIPCODES", savedZips);
                        editor.apply();
                        editor.commit();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }

                    public void onWaitFragmentInteractionShow() {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                                .addToBackStack(null)
                                .commit();
                    }

                    public void onWaitFragmentInteractionHide() {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .remove(getActivity().getSupportFragmentManager().findFragmentByTag("WAIT"))
                                .commit();
                    }

                    private void handleHomeOnPostExecute(final String result) {
                        Bundle args = new Bundle();
                        args.putString("weather", result);
                        Fragment frag = new LandingPageFragment();
                        frag.setArguments(args);
                        onWaitFragmentInteractionHide();

                        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Map");
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_home_container, frag)
                                .addToBackStack(null);
                        // Commit the transaction
                        transaction.commit();
                    }
                });
                refreshDropDownList();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            ImageButton mapsButton = v.findViewById(R.id.imageButton_maps);
            mapsButton.setOnClickListener((View view) -> {
                Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Map");
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_home_container, new MapFragment())
                        .addToBackStack(null);
                // Commit the transaction
                transaction.commit();
            });
        }
        return v;
    }

    private void refreshDropDownList() {
        ArrayList<String> zipcodes = new ArrayList<String>();
        try {
            zipcodes.add(weather.getJSONObject(2).getString("zip"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!savedZips.isEmpty()) {
            Iterator zipIterator = savedZips.iterator();
            while (zipIterator.hasNext()) {
                String temp = zipIterator.next().toString();
                if (!temp.equals(zipcodes.get(0))) {
                    zipcodes.add(temp);
                }
            }
        }
        if (zipcodes.size() > 0) {
            if (!zipcodes.get(0).equals("") && savedZips.contains(zipcodes.get(0))) {
                zipcodes.add("(DEL)");
            } else if (!zipcodes.get(0).equals("")) {
                zipcodes.add("(SAVE)");
            }
        }
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item_zipcode, zipcodes.toArray(new String[0]));
        mDropdown.setAdapter(mAdapter);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet("ZIPCODES", savedZips);
        editor.apply();
        editor.commit();
    }
}
