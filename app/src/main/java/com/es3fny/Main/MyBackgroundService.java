package com.es3fny.Main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.es3fny.Maps.AppController;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MyBackgroundService extends Service implements ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = "MyBackgroundService";
    static public String longtitude, latitude;
    static public String mCurrentID;
    static public Location mCurrentlocation;
    public boolean Updated;
    LocationManager locationManager;
    Handler handler = new Handler();
    private FirebaseAuth mAuth;

    public MyBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service is On.....");
        scheduleSendLocation();
        AppController.getInstance().setConnectivityListener(this);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            Gson gson = new Gson();
            String json = settings.getString("mCurrentlocation", null);
            Type type = new TypeToken<Location>() {
            }.getType();
            mCurrentlocation = gson.fromJson(json, type);
            mCurrentID = settings.getString("mCurrentID", mAuth.getCurrentUser().getUid());
            longtitude = settings.getString("longtitude", "null");
            latitude = settings.getString("latitude", "null");
            Updated = settings.getBoolean("Updated", false);
            Log.e("Test",longtitude+" "+latitude+" "+mAuth.getCurrentUser().getUid());
            if (mCurrentlocation != null) {
                Log.e("Current Location ", mCurrentlocation.getLatitude() + " " + mCurrentlocation.getLongitude());
            }
        }
        startLocationUpdates();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("Test", "............. onTaskRemoved .......");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mCurrentlocation);
        editor.putString("mCurrentlocation", json);
        editor.putString("mCurrentID", mCurrentID);
        editor.putString("longtitude", longtitude);
        editor.putString("latitude", latitude);
        editor.putBoolean("Updated", Updated);
        editor.apply();
    }

    public void scheduleSendLocation() {
        handler.postDelayed(new Runnable() {
            public void run() {
                startLocationUpdates();
                if (mAuth.getCurrentUser() != null) {
                    mCurrentID = mAuth.getCurrentUser().getUid();
                    Log.e(TAG, "User ID is ==> " + mCurrentID);
                }
                changeLocation();
                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);
    }

    public void changeLocation() {
        if (isNetworkAvailable()  ) {
            Log.e(TAG, longtitude + " " + latitude);
            UpdateCurrentLocation();
        } else {
            Log.e(TAG, "Location Had To Be Updated");
            Updated = false;
        }
    }

    private void UpdateCurrentLocation() {
        if (mAuth.getCurrentUser() != null) {
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mCurrentID = mAuth.getCurrentUser().getUid();
            mCurrentID = mAuth.getCurrentUser().getUid();
            Map<String, Object> UpdatedLocation = new HashMap<>();
            UpdatedLocation.put("latitude", latitude);
            UpdatedLocation.put("longtitude", longtitude);
            mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).update(UpdatedLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Updated = true;
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.e(TAG, "Connection Changed.....");
        if (isConnected && !Updated) {
            Log.e(TAG, "Location Updated In DataBase.....");
            UpdateCurrentLocation();
        }

    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        long UPDATE_INTERVAL = 15 * 60 * 1000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 5 * 1000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged...");
        mCurrentlocation = location;
        // New location has now been determined
        longtitude = Double.toString(location.getLongitude());
        latitude = Double.toString(location.getLatitude());
        Log.e(TAG, "New Location ... " + longtitude + " " + latitude);
        // You can now create a LatLng Object for use with maps
    }

}
