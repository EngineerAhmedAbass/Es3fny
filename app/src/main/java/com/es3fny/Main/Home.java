package com.es3fny.Main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.First_Aid.MainActivity;
import com.es3fny.Maps.MainMap;
import com.es3fny.R;
import com.es3fny.Request.HelpRequest;
import com.es3fny.Request.bloodDonationRequest;
import com.es3fny.Sos.SosActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



public class Home extends AppCompatActivity {
    public Button Help_Request_BTN;
    public Button Blood_Donor_BTN;
    public Button SOS_BTN;
    public Button Places_BTN;
    public Button First_Aid_BTN;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mfirestore;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Home.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.back_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetLocal();
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        Help_Request_BTN = findViewById(R.id.help_request_Btn);
        Blood_Donor_BTN = findViewById(R.id.Blood_BTN);
        SOS_BTN = findViewById(R.id.SOS_BTN);
        Places_BTN = findViewById(R.id.Places_BTN);
        First_Aid_BTN = findViewById(R.id.First_Aid_BTN);
        Toolbar toolbar = findViewById(R.id.app_bar);
        TextView mTitle =  toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Help_Request_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent HelpIntent = new Intent(Home.this, HelpRequest.class);
                startActivity(HelpIntent);
            }
        });
        Blood_Donor_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bloodIntent = new Intent(Home.this, bloodDonationRequest.class );
                startActivity(bloodIntent);
            }
        });
        SOS_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent HelpIntent = new Intent(Home.this, SosActivity.class);
                startActivity(HelpIntent);
            }
        });
        Places_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MapsIntnet = new Intent(Home.this, MainMap.class);
                startActivity(MapsIntnet);
            }
        });
        First_Aid_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent FirstAidIntnet = new Intent(Home.this, MainActivity.class);
                startActivity(FirstAidIntnet);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String load = "";
        switch (item.getItemId())
        {
            case R.id.notification:
                Intent GoToNotifications = new Intent(Home.this, ShowNotifications.class);
                startActivity(GoToNotifications);
                 break;
            case R.id.settings:
                Intent settings = new Intent(Home.this, SettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.log_out:
                Log_Out();
                break;
            case R.id.Language:
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
                startActivity(getIntent());
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void SetLocal(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String load = settings.getString("Language", "en");
        Locale locale = new Locale(load);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,getResources().getDisplayMetrics());
    }

    private void Log_Out() {
        if(isNetworkAvailable()) {
            Intent myService = new Intent(Home.this, MyBackgroundService.class);
            stopService(myService);
            Map<String, Object> tokenMapRemove = new HashMap<>();
            tokenMapRemove.put("token_id", FieldValue.delete());
            String mCurrentID = mAuth.getCurrentUser().getUid();
            mfirestore.collection("Users").document(mCurrentID).update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mAuth.signOut();
                    Intent LoginIntent = new Intent(Home.this, LoginActivity.class);
                    startActivity(LoginIntent);
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //LoadLanguage();
        FirebaseUser CurrentUser = mAuth.getCurrentUser();
        if (CurrentUser == null) {
            sendToLogin();
        }else{
            mfirestore = FirebaseFirestore.getInstance();
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(Home.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
