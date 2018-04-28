package com.es3fny.Main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.es3fny.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static GoogleApiClient mGoogleApiClient;
    DatePicker datePicker;
    int day, month, year;
    String Myname, myemail, myPassword, Myphone, MyCity, MyStreet, MyNID, longtitude, latitude;
    LocationManager locationManager;
    private EditText fullname;
    private EditText email;
    private EditText password;
    private EditText phone;
    private EditText city;
    private EditText nid;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        datePicker = findViewById(R.id.DOB);
        fullname = findViewById(R.id.full_name);
        email =  findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone =  findViewById(R.id.Phone);
        city =  findViewById(R.id.city);
        EditText street = findViewById(R.id.street);
        nid = findViewById(R.id.NID);

        mGoogleApiClient = new GoogleApiClient.Builder(RegisterActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        if (isLocationServiceEnabled()) {
            if (isNetworkAvailable()) {
                startLocationUpdates();
            } else {
                Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RegisterActivity.this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
            showSettingDialog();
        }

        Button mRegBtn =  findViewById(R.id.btnRegister);
        Button mLoginPageBtn =findViewById(R.id.btnLinkToLoginScreen);

        mLoginPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLocationServiceEnabled()) {
                    if (isNetworkAvailable()) {
                        Register();
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
                    showSettingDialog();
                }
            }
        });

    }

    private void Register() {
        if (longtitude == null || latitude == null) {
            Toast.makeText(RegisterActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            return;
        }
        Myname = fullname.getText().toString();
        myemail = email.getText().toString();
        myPassword = password.getText().toString();
        Myphone = phone.getText().toString();
        MyCity = city.getText().toString();
        MyStreet = city.getText().toString();
        MyNID = nid.getText().toString();
        day = datePicker.getDayOfMonth();
        month = (datePicker.getMonth() + 1);
        year = (datePicker.getYear());

        if (ValidateData()) {
            mAuth.createUserWithEmailAndPassword(myemail, myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        ProgressDialog.show(RegisterActivity.this, getString(R.string.registering), getString(R.string.wait_till_register_complete));
                        String User_id = mAuth.getCurrentUser().getUid();
                        MyBackgroundService.mCurrentID = User_id;
                        String Token_id = FirebaseInstanceId.getInstance().getToken();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", Myname);
                        userMap.put("email", myemail);
                        userMap.put("phone", Myphone);
                        userMap.put("city", MyCity);
                        userMap.put("street", MyStreet);
                        userMap.put("nid", MyNID);
                        userMap.put("day", day);
                        userMap.put("month", month);
                        userMap.put("year", year);
                        userMap.put("longtitude", longtitude);
                        userMap.put("latitude", latitude);
                        userMap.put("token_id", Token_id);

                        mFirestore.collection("Users").document(User_id).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(Myname)
                                        .build();
                                assert user != null;
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                }
                                            }
                                        });
                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("example_text",Myname);
                                editor.apply();
                                SendToMain();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean ValidateData() {
        if (Myname.equals("")) {
            Toast.makeText(RegisterActivity.this, R.string.must_enter_name, Toast.LENGTH_SHORT).show();
            return false;
        } else if (myemail.equals("")) {
            Toast.makeText(RegisterActivity.this, R.string.must_enter_email, Toast.LENGTH_SHORT).show();
            return false;
        } else if (myPassword.equals("")) {
            Toast.makeText(RegisterActivity.this, R.string.must_enter_password, Toast.LENGTH_SHORT).show();
            return false;
        } else if (MyNID.equals("")) {
            Toast.makeText(RegisterActivity.this, R.string.must_enter_NID, Toast.LENGTH_SHORT).show();
            return false;
        } else if ((MyNID.length() != 14)) {
            Toast.makeText(RegisterActivity.this, R.string.must_enter_valid_NID, Toast.LENGTH_SHORT).show();
            return false;
        }
        int NIDYear = Integer.parseInt(MyNID.substring(0, 1));
        int NIDBYear = Integer.parseInt(MyNID.substring(1, 3));
        int NIDMonth = Integer.parseInt(MyNID.substring(3, 5));
        int NIDDay = Integer.parseInt(MyNID.substring(5, 7));
        int City = Integer.parseInt(MyNID.substring(7, 9));
        if (NIDBYear >= 50) {
            NIDBYear += 1900;
        } else {
            NIDBYear += 2000;
        }
        if ((year >= 2000 && NIDYear != 3) || (year < 2000 && NIDYear != 2) || (year != NIDBYear) || (month != NIDMonth) || (day != NIDDay) || (City > 35 && City != 88)) {
            Toast.makeText(RegisterActivity.this, R.string.invalid_NID, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (year > 2001) {
            Toast.makeText(RegisterActivity.this, R.string.young_for_register, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        long UPDATE_INTERVAL = 5 * 1000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 1000;
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
        // New location has now been determined
        longtitude = Double.toString(location.getLongitude());
        latitude = Double.toString(location.getLatitude());
        // You can now create a LatLng Object for use with maps
    }

    private void SendToMain() {
        Intent intent = new Intent(RegisterActivity.this, Home.class);
        startActivity(intent);
        finish();
    }

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(RegisterActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");
                        if (isNetworkAvailable()) {
                            if (ActivityCompat.checkSelfPermission(RegisterActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(RegisterActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            startLocationUpdates();
                        } else {
                            Toast.makeText(RegisterActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(RegisterActivity.this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    public boolean isLocationServiceEnabled() {
        boolean gps_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            assert locationManager != null;
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gps_enabled;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
