package com.es3fny.Maps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.es3fny.Main.MyBackgroundService;
import com.es3fny.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    LocationManager locationManager;
    CoordinatorLayout mainCoordinatorLayout;
    String data = "";
    ArrayList<String> The_Array;
    String Distance;
    int Dist = 0;
    private GoogleMap mMap;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        setContentView(R.layout.activity_maps);
        Bundle bundle = getIntent().getExtras();
        The_Array = new ArrayList<>();
        The_Array = bundle.getStringArrayList("data");
        //data = bundle.get("data").toString();
        Distance = bundle.get("Distance").toString();
        Dist = Integer.parseInt(Distance) * 1000;
        Toast.makeText(getBaseContext(), "Places searching for are " + data + "s within " + Dist + " Meter",
                Toast.LENGTH_LONG).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mainCoordinatorLayout = findViewById(R.id.mainCoordinatorLayout);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationSettings();
        }
    }

    private void showLocationSettings() {
        Snackbar snackbar = Snackbar
                .make(mainCoordinatorLayout, "Location Error: GPS Disabled!",

                        Snackbar.LENGTH_LONG)
                .setAction("Enable", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        TextView textView = sbView

                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,

                android.Manifest.permission.ACCESS_COARSE_LOCATION)

                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = MyBackgroundService.mCurrentlocation;
            onLocationChanged(location);
        }
        showCurrentLocation();
    }

    private void showCurrentLocation() {
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this,

                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&

                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)

                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, AppConfig.MIN_TIME_BW_UPDATES,

                AppConfig.MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }

    private void loadNearByPlaces(double latitude, double longitude) {
//YOU Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works

        for (int i = 0; i < The_Array.size(); i++) {
            Log.e("Places", "Looking For " + The_Array.get(i));
            String type = The_Array.get(i);
            StringBuilder googlePlacesUrl =
                    new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
            googlePlacesUrl.append("&radius=").append(Dist);
            googlePlacesUrl.append("&types=").append(type);
            googlePlacesUrl.append("&sensor=true");
            googlePlacesUrl.append("&key=" + AppConfig.GOOGLE_BROWSER_API_KEY);

            final int finalI = i;
            JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),

                    new Response.Listener<JSONObject>() {
                        @Override

                        public void onResponse(JSONObject result) {

                            Log.i(AppConfig.TAG, "onResponse: Result= " + result.toString());

                            parseLocationResult(result, finalI);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(AppConfig.TAG, "onErrorResponse: Error= " + error);
                            Log.e(AppConfig.TAG, "onErrorResponse: Error= " + error.getMessage());
                        }
                    });
            AppController.getInstance().addToRequestQueue(request);
        }

    }

    private void parseLocationResult(JSONObject result, int count) {

        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(AppConfig.STATUS).equalsIgnoreCase(AppConfig.OK)) {

                if (count == 0) {
                    mMap.clear();
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    id = place.getString(AppConfig.SUPERMARKET_ID);
                    place_id = place.getString(AppConfig.PLACE_ID);
                    if (!place.isNull(AppConfig.NAME)) {
                        placeName = place.getString(AppConfig.NAME);
                    }
                    if (!place.isNull(AppConfig.VICINITY)) {
                        vicinity = place.getString(AppConfig.VICINITY);
                    }
                    latitude = place.getJSONObject(AppConfig.GEOMETRY).getJSONObject(AppConfig.LOCATION)

                            .getDouble(AppConfig.LATITUDE);
                    longitude = place.getJSONObject(AppConfig.GEOMETRY).getJSONObject(AppConfig.LOCATION)

                            .getDouble(AppConfig.LONGITUDE);
                    reference = place.getString(AppConfig.REFERENCE);
                    icon = place.getString(AppConfig.ICON);

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName + " : " + vicinity);

                    mMap.addMarker(markerOptions);
                }

            } else if (result.getString(AppConfig.STATUS).equalsIgnoreCase(AppConfig.ZERO_RESULTS)) {
                Toast.makeText(getBaseContext(), "No Places found in " + Distance + " radius!!!",

                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(AppConfig.TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        loadNearByPlaces(latitude, longitude);
    }

    @Override

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override

    public void onProviderEnabled(String s) {
    }

    @Override

    public void onProviderDisabled(String s) {
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(AppConfig.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}