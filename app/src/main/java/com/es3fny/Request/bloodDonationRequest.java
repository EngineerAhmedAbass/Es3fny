package com.es3fny.Request;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.Main.Home;
import com.es3fny.Main.LoginActivity;
import com.es3fny.Main.MyBackgroundService;
import com.es3fny.Main.SettingsActivity;
import com.es3fny.Main.ShowNotifications;
import com.es3fny.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;



import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class bloodDonationRequest extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static GoogleApiClient mGoogleApiClient;
    MyBackgroundService myBackgroundService;
    ProgressDialog progressDialog;
    LocationManager locationManager;
    private Spinner spinner;
    private EditText requestText;
    private Button SendRequestBtn;
    private String Message;
    private String mCurrentID;
    private String mCurrentName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mfirestore;
    private Vector<String> SentUsers = new Vector<>();
    private boolean Language_Changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_donation_request);

        Language_Changed = getIntent().getBooleanExtra("Language_Changed", false);

        requestPermission();
        /*  Start Spinner Code */
        spinner = findViewById(R.id.Blood_type_Spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        /*  End Spinner Code */

        requestText = findViewById(R.id.text_help);
        SendRequestBtn = findViewById(R.id.send_request);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.request_blood);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getTitle());
        mAuth = FirebaseAuth.getInstance();

        myBackgroundService = new MyBackgroundService();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.requesting);
        progressDialog.setMessage(getString(R.string.please_wait_till_blood_request));

        mCurrentID = MyBackgroundService.mCurrentID;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(bloodDonationRequest.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        if (isLocationServiceEnabled()) {
            if (!isNetworkAvailable()) {
                Toast.makeText(bloodDonationRequest.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(bloodDonationRequest.this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
            showSettingDialog();
        }
        SendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendRequestBtn.setClickable(false);
                if (isLocationServiceEnabled()) {
                    if (isNetworkAvailable()) {
                        progressDialog.show();
                        SendNotifications();
                    } else {
                        SendRequestBtn.setClickable(true);
                        Toast.makeText(bloodDonationRequest.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    SendRequestBtn.setClickable(true);
                    Toast.makeText(bloodDonationRequest.this, R.string.open_location_first, Toast.LENGTH_SHORT).show();
                    showSettingDialog();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String load = "";
        switch (item.getItemId()) {
            case R.id.notification:
                Intent GoToNotifications = new Intent(this, ShowNotifications.class);
                startActivity(GoToNotifications);
                break;
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.Language:
                Language_Changed = true;
                if (item.getTitle().equals("English")) {
                    load = "en";
                } else if (item.getTitle().equals("عربي")) {
                    load = "ar";
                }
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putString("Language", load);
                editor.apply();
                Locale locale = new Locale(load);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                finish();
                Intent intent = getIntent();
                intent.putExtra("Language_Changed", Language_Changed);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(getIntent());
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Language_Changed) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser CurrentUser = mAuth.getCurrentUser();
        if (CurrentUser == null) {
            sendToLogin();
        } else {
            mfirestore = FirebaseFirestore.getInstance();
        }
    }

    void SendNotifications() {
        mfirestore = FirebaseFirestore.getInstance();
        mCurrentName = mAuth.getCurrentUser().getDisplayName();
        mCurrentID = mAuth.getCurrentUser().getUid();
        Message = requestText.getText().toString();
        String btype = spinner.getSelectedItem().toString();
        Message += " \n ";
        Message += btype;
        Map<String, Object> RequestMessage = new HashMap<>();
        Date currentTime = Calendar.getInstance().getTime();
        RequestMessage.put("message", Message);
        RequestMessage.put("from", mCurrentID);
        RequestMessage.put("status", "waiting");
        RequestMessage.put("longtitude", MyBackgroundService.longtitude);
        RequestMessage.put("latitude", MyBackgroundService.latitude);
        RequestMessage.put("date", currentTime);
        mfirestore.collection("Requests").add(RequestMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                final String RequestID = documentReference.getId();
                mfirestore.collection("Users").addSnapshotListener(bloodDonationRequest.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String user_id = doc.getDocument().getId();
                                User temp_user = doc.getDocument().toObject(User.class);

                                if (temp_user.getToken_id() == null || user_id.equals(mCurrentID)) {
                                    continue;
                                }
                                if (temp_user.getLatitude() == null || temp_user.getLongtitude() == null) {
                                    continue;
                                }
                                String temUserLong = temp_user.longtitude;
                                String temUserLat= temp_user.latitude;
                                if (temUserLat.equals("null") || temUserLong.equals("null")){
                                    continue;
                                }
                                if (MyBackgroundService.latitude.equals("null") || MyBackgroundService.longtitude.equals("null")){
                                    Toast.makeText(bloodDonationRequest.this, R.string.no_location, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                double Dist = distance(Double.parseDouble(MyBackgroundService.latitude), Double.parseDouble(MyBackgroundService.longtitude), Double.parseDouble(temUserLat), Double.parseDouble(temUserLong));
                                if (Dist < 12) {
                                    Log.e("In Distance ", "To " + temp_user.getName() + " " + Dist);
                                    Map<String, Object> notificationMessage = new HashMap<>();
                                    Date currentTime = Calendar.getInstance().getTime();
                                    notificationMessage.put("message", Message);
                                    notificationMessage.put("from", mCurrentID);
                                    notificationMessage.put("user_name", mCurrentName);
                                    notificationMessage.put("domain", "تبرع بالدم");
                                    notificationMessage.put("longtitude", MyBackgroundService.longtitude);
                                    notificationMessage.put("latitude", MyBackgroundService.latitude);
                                    notificationMessage.put("requestID", RequestID);
                                    notificationMessage.put("type", "Request");
                                    notificationMessage.put("date", currentTime);
                                    mfirestore.collection("Users/" + user_id + "/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                        }
                                    });
                                } else {
                                    Log.e("Out Distance ", "To " + temp_user.getName() + " " + Dist);
                                }
                            }

                        }
                        progressDialog.hide();
                        SendRequestBtn.setClickable(true);
                        Toast.makeText(bloodDonationRequest.this, R.string.blood_request_success, Toast.LENGTH_SHORT).show();
                        GoToHome();
                    }
                });
            }
        });

        /*mfirestore.collection("Users").addSnapshotListener(bloodDonationRequest.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String user_id = doc.getDocument().getId();
                        User temp_user = doc.getDocument().toObject(User.class);

                        if (temp_user.getToken_id() == null || user_id.equals(mCurrentID)) {
                            continue;
                        }
                        if (temp_user.getLatitude() == null || temp_user.getLongtitude() == null) {
                            continue;
                        }
                        double Dist = distance(Double.parseDouble(MyBackgroundService.latitude), Double.parseDouble(MyBackgroundService.longtitude), Double.parseDouble(temp_user.getLatitude()), Double.parseDouble(temp_user.getLongtitude()));
                        if (Dist < 10) {
                            Log.e("In Distance ", "To " + temp_user.getName() + " " + Dist);
                            SentUsers.add(user_id);
                        } else {
                            Log.e("Out Distance ", "To " + temp_user.getName() + " " + Dist);
                        }
                    }
                }
                if (SentUsers.isEmpty()) {
                    progressDialog.hide();
                    SendRequestBtn.setClickable(true);
                    Toast.makeText(bloodDonationRequest.this, R.string.no_users, Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> RequestMessage = new HashMap<>();
                    Date currentTime = Calendar.getInstance().getTime();
                    RequestMessage.put("message", Message);
                    RequestMessage.put("from", mCurrentID);
                    RequestMessage.put("status", "waiting");
                    RequestMessage.put("longtitude", MyBackgroundService.longtitude);
                    RequestMessage.put("latitude", MyBackgroundService.latitude);
                    RequestMessage.put("date", currentTime);
                    mfirestore.collection("Requests").add(RequestMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String RequestID = documentReference.getId();
                            for (int i = 0; i < SentUsers.size(); i++) {
                                Map<String, Object> notificationMessage = new HashMap<>();
                                Date currentTime = Calendar.getInstance().getTime();
                                notificationMessage.put("message", Message);
                                notificationMessage.put("from", mCurrentID);
                                notificationMessage.put("user_name", mCurrentName);
                                notificationMessage.put("domain", "تبرع بالدم");
                                notificationMessage.put("longtitude", MyBackgroundService.longtitude);
                                notificationMessage.put("latitude", MyBackgroundService.latitude);
                                notificationMessage.put("requestID", RequestID);
                                notificationMessage.put("type", "Request");
                                notificationMessage.put("date", currentTime);
                                mfirestore.collection("Users/" + SentUsers.elementAt(i) + "/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.hide();
                                        SendRequestBtn.setClickable(true);
                                        Toast.makeText(bloodDonationRequest.this, "Error :  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            progressDialog.hide();
                            SendRequestBtn.setClickable(true);
                            Toast.makeText(bloodDonationRequest.this, R.string.blood_request_success, Toast.LENGTH_SHORT).show();
                            GoToHome();
                        }
                    });
                }
            }
        });*/

    }

    private void GoToHome() {
        finish();
    }

    private void sendToLogin() {
        Intent intent = new Intent(bloodDonationRequest.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(bloodDonationRequest.this, new String[]{ACCESS_FINE_LOCATION}, 1);
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
                final LocationSettingsStates state = result.getLocationSettingsStates();
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
                            status.startResolutionForResult(bloodDonationRequest.this, REQUEST_CHECK_SETTINGS);
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
                            if (ActivityCompat.checkSelfPermission(bloodDonationRequest.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(bloodDonationRequest.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(bloodDonationRequest.this, R.string.location_enabled, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(bloodDonationRequest.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(bloodDonationRequest.this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
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

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        dist = dist / 1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
