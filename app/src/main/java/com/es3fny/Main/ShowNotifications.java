package com.es3fny.Main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.R;
import com.es3fny.Request.MyNotification;
import com.es3fny.Request.NotificationsRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;



public class ShowNotifications extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner Sorting_Spiner;
    SwipeRefreshLayout swipeRefreshLayout;
    private CheckBox Request_check, Blood_check, Responces_check;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String mCurrentID;
    private List<MyNotification> notificationsList_Displayed;
    private List<MyNotification> notificationsList_Blood_Request;
    private List<MyNotification> notificationsList_Help_Request;
    private List<MyNotification> notificationsList_Responces;
    private NotificationsRecyclerAdapter notificationsRecyclerAdapter;
    private boolean Language_Changed;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Language_Changed){
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("Test", "............. OnSave .......");
        outState.putString("test", "Welcome back to Activity");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.e("Test", "............. OnRestore .......");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Test", "............. OnCreate .......");
        setContentView(R.layout.activity_show_notifications);

        Language_Changed = getIntent().getBooleanExtra("Language_Changed",false);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkAvailable()){
                    ReLoadNotifications();
                }else{
                    Toast.makeText(ShowNotifications.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*  Start Spinner Code */

        Sorting_Spiner = findViewById(R.id.sorting_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sorting_method, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        Sorting_Spiner.setAdapter(adapter);

        /*  End Spinner Code */

        Sorting_Spiner.setOnItemSelectedListener(this);

        Request_check = findViewById(R.id.request);
        Blood_check = findViewById(R.id.blood);
        Responces_check = findViewById(R.id.response);

        Request_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Chech_Filters("Request");
            }
        });

        Blood_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Chech_Filters("Blood");
            }
        });

        Responces_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Chech_Filters("Response");
            }
        });

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Notifications");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getTitle());

        mFirestore = FirebaseFirestore.getInstance();

        RecyclerView mNotificationsListView = findViewById(R.id.notifications_l);

        notificationsList_Displayed = new ArrayList<>();
        notificationsList_Help_Request = new ArrayList<>();
        notificationsList_Blood_Request = new ArrayList<>();
        notificationsList_Responces = new ArrayList<>();

        notificationsRecyclerAdapter = new NotificationsRecyclerAdapter(this, notificationsList_Displayed);

        mNotificationsListView.getRecycledViewPool().setMaxRecycledViews(0,50);
        mNotificationsListView.setHasFixedSize(true);
        mNotificationsListView.setLayoutManager(new LinearLayoutManager(this));
        mNotificationsListView.setAdapter(notificationsRecyclerAdapter);
        mAuth = FirebaseAuth.getInstance();
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
                break;
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.Language:
                Language_Changed =true;
                if (item.getTitle().equals("English")){
                    load = "en";
                }else if (item.getTitle().equals("عربي")){
                    load = "ar";
                }
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putString("Language",load);
                editor.apply();
                Locale locale = new Locale(load);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config,getResources().getDisplayMetrics());
                finish();
                Intent intent = getIntent();
                intent.putExtra("Language_Changed",Language_Changed);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(getIntent());
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    public void Chech_Filters(String Type) {
        boolean request = Request_check.isChecked();
        boolean blood = Blood_check.isChecked();
        boolean response = Responces_check.isChecked();
        if ((Type.equals("Request") || Type.equals("Main"))) {
            if (request) {
                notificationsList_Displayed.addAll(notificationsList_Help_Request);
            } else {
                notificationsList_Displayed.removeAll(notificationsList_Help_Request);
            }
        }
        if ((Type.equals("Blood") || Type.equals("Main"))) {
            if (blood) {
                notificationsList_Displayed.addAll(notificationsList_Blood_Request);
            } else {
                notificationsList_Displayed.removeAll(notificationsList_Blood_Request);
            }
        }
        if ((Type.equals("Response") || Type.equals("Main"))) {
            if (response) {
                notificationsList_Displayed.addAll(notificationsList_Responces);
            } else {
                notificationsList_Displayed.removeAll(notificationsList_Responces);
            }
        }
        String Selected_sort = Sorting_Spiner.getSelectedItem().toString();
        Sort_The_Data(Selected_sort);
        notificationsRecyclerAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("Test Me ", "----- OnStart --------");
        FirebaseUser CurrentUser = mAuth.getCurrentUser();
        if (CurrentUser == null) {
            sendToLogin();
        } else {
            mFirestore = FirebaseFirestore.getInstance();
            mCurrentID = mAuth.getUid();
        }
        ReLoadNotifications();
    }

    private void ReLoadNotifications() {
        notificationsList_Blood_Request.clear();
        notificationsList_Help_Request.clear();
        notificationsList_Responces.clear();
        notificationsList_Displayed.clear();
        mFirestore.collection("Users").document(mCurrentID).collection("Notifications").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String Notification_Id = doc.getDocument().getId();
                        MyNotification notifications = doc.getDocument().toObject(MyNotification.class).withId(Notification_Id);
                        double Dist = distance(Double.parseDouble(MyBackgroundService.latitude), Double.parseDouble(MyBackgroundService.longtitude), Double.parseDouble(notifications.getLatitude()), Double.parseDouble(notifications.getLongtitude()));
                        notifications.setDistance(Dist);
                        if (notifications.getType().equals("Request")) {
                            if (notifications.getDomain().equals("تبرع بالدم")) {
                                notificationsList_Blood_Request.add(notifications);
                            } else {
                                notificationsList_Help_Request.add(notifications);
                            }
                        } else {
                            notificationsList_Responces.add(notifications);
                        }
                    }
                }
                Chech_Filters("Main");
            }
        });
    }

    private void sendToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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

    public void Sort_The_Data(String Selected_sort) {
        switch (Selected_sort) {
            case "Select Sorting Method":
            case "طريقة الترتيب":
                Collections.sort(notificationsList_Displayed, new Custom_Distace_Comparator());
                break;
            case "Ascending by Distance":
            case "من الاقرب الى الابعد":
                Collections.sort(notificationsList_Displayed, new Custom_Distace_Comparator());
                break;
            case "Descending by Distance":
            case "من الابعد الى الاقرب":
                Collections.sort(notificationsList_Displayed, new Custom_Distace_Comparator());
                Collections.reverse(notificationsList_Displayed);
                break;
            case "Ascending by Time":
            case "من الاقدم الى الاحدث":
                Collections.sort(notificationsList_Displayed, new Custom_Date_Comparator());
                break;
            case "Descending by Time":
            case "من الاحدث الى الاقدم":
                Collections.sort(notificationsList_Displayed, new Custom_Date_Comparator());
                Collections.reverse(notificationsList_Displayed);
                break;
        }
        notificationsRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String Selected_sort = adapterView.getItemAtPosition(i).toString();
        Sort_The_Data(Selected_sort);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class Custom_Distace_Comparator implements Comparator<MyNotification> {
        @Override
        public int compare(MyNotification o1, MyNotification o2) {
            if (o1.getDistance() == o2.getDistance()) {
                return 0;
            } else if (o1.getDistance() > o2.getDistance()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public class Custom_Date_Comparator implements Comparator<MyNotification> {
        @Override
        public int compare(MyNotification o1, MyNotification o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
