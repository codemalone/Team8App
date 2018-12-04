package tcss450.uw.edu.team8app.home;


import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.WaitFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, WaitFragment.OnFragmentInteractionListener {

    private MapView mMapView;
    private GoogleMap mMap;
    private MarkerOptions mMarkerOptions;
    private Geocoder mGeocoder;
    private TextView mCurrentZip;
    private double mLat;
    private double mLng;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        onWaitFragmentInteractionShow();

        if (getArguments() != null) {
            mLat = Double.parseDouble(getArguments().getString("lat"));
            mLng = Double.parseDouble(getArguments().getString("lng"));
        }

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
        mCurrentZip = rootView.findViewById(R.id.editText_map_zipcode);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);
        mMapView.onResume();

        ImageButton findByZipButton = rootView.findViewById(R.id.imageButton_map_search);
        findByZipButton.setOnClickListener((View v) -> {
            String zip = mCurrentZip.getText().toString();
            if (!zip.equals("")) {
                LatLng newLatLng = getLatLngByZipcode(zip);
                if (newLatLng != null) {
                    mMap.clear();
                    mMarkerOptions = new MarkerOptions().position(newLatLng).title("" + newLatLng);
                    mMap.addMarker(mMarkerOptions);
                }
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(mMarkerOptions.getPosition()).zoom(4).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });

        ImageButton updateWeatherButton = rootView.findViewById(R.id.imageButton_map_continue);
        updateWeatherButton.setOnClickListener((View v) -> {
            String zip = mCurrentZip.getText().toString();
            if (!zip.equals("")) {
                LatLng newLatLng = getLatLngByZipcode(zip);
                if (newLatLng != null) {
                    mMap.clear();
                    mMarkerOptions = new MarkerOptions().position(newLatLng).title("" + newLatLng);
                    mMap.addMarker(mMarkerOptions);
                }
            }
            Uri uri = new Uri.Builder()
                    .scheme(getString(R.string.ep_scheme))
                    .encodedAuthority(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_weather))
                    .build();
            //build the JSONObject
            JSONObject msg = new JSONObject();
            try {
                msg.put("latitude", mMarkerOptions.getPosition().latitude);
                msg.put("longitude", mMarkerOptions.getPosition().longitude);
                List<Address> addresses = mGeocoder.getFromLocation(mMarkerOptions.getPosition().latitude, mMarkerOptions.getPosition().longitude, 1);
                if (addresses.size() == 0) {
                    msg.put("zipcode", "");
                } else {
                    msg.put("zipcode", addresses.get(0).getPostalCode());
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleHomeOnPostExecute)
                    .build().execute();
        });

        return rootView;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        onWaitFragmentInteractionHide();
        mMap = googleMap;
        mMap.setOnMapClickListener((LatLng latLng) -> {
            mMap.clear();
            try {
                List<Address> addresses = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {
                    mCurrentZip.setText(addresses.get(0).getPostalCode());
                } else {
                    mCurrentZip.setText("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMarkerOptions = new MarkerOptions().position(latLng).title("" + latLng);
            mMap.addMarker(mMarkerOptions);
        });
        Random r = new Random();
        double lat = -90 + 180 * r.nextDouble();
        double lng = -180 + 360 * r.nextDouble();
        LatLng initialLatLng = new LatLng(lat, lng);
        if (mLat != 0.0 && mLng != 0.0) {
            initialLatLng = new LatLng(mLat, mLng);
        }
        try {
            List<Address> addresses = mGeocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                mCurrentZip.setText(addresses.get(0).getPostalCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMarkerOptions = new MarkerOptions().position(initialLatLng).title("" + initialLatLng);
        mMap.addMarker(mMarkerOptions);
        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(initialLatLng).zoom(4).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private LatLng getLatLngByZipcode(String zipcode) {
        List<Address> addresses = null;
        try {
            addresses = mGeocoder.getFromLocationName(zipcode, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty()) {
            return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
        } else {
            return null;
        }
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(getActivity().getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }
}
