package com.es3fny.Sos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.Main.Home;
import com.es3fny.Main.SettingsActivity;
import com.es3fny.Main.ShowNotifications;
import com.es3fny.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;



public class SosActivity extends AppCompatActivity{
    public Switch sos_switch;
    public boolean sos_flag;
    public ArrayList<String> Names;
    private boolean Language_Changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Language_Changed = getIntent().getBooleanExtra("Language_Changed",false);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("SOS");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getTitle());
        loadData();
//--------------------------------------------------------------------------------------------------------------------------
        sos_switch = findViewById(R.id.sos_switch);
        Button edit_contacts = findViewById(R.id.edit_contacts);
        Button send_sos_msg = findViewById(R.id.send_sos_msg);
        sos_switch.setChecked(sos_flag);
        if (sos_switch.isChecked()) {
            sos_switch.setText(R.string.sos_deactivate);
        } else {
            sos_switch.setText(R.string.sos_activate);
        }
        sos_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sos_flag = isChecked;
                if (sos_switch.isChecked()) {
                    sos_switch.setText(R.string.sos_deactivate);
                } else {
                    sos_switch.setText(R.string.sos_activate);
                }
                saveData();
            }
        });
        send_sos_msg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SosActivity.this,
                        Send_SOS_Message.class);
                if (sos_switch.isChecked()) {
                    if (Names.size() < 1) {
                        Toast.makeText(SosActivity.this, R.string.add_contact_first, Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(SosActivity.this, R.string.activate_sos, Toast.LENGTH_SHORT).show();
                }
            }
        });
        edit_contacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(SosActivity.this, contacts.class);

                if (sos_switch.isChecked()) {
                    startActivity(intent);
                } else {
                    Toast.makeText(SosActivity.this, R.string.activate_sos, Toast.LENGTH_SHORT).show();
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
                saveData();
                finish();
                Intent intent = getIntent();
                intent.putExtra("Language_Changed",Language_Changed);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(getIntent());
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onBackPressed() {
        saveData();
        if(Language_Changed){
            Intent intent = new Intent(this,Home.class);
            startActivity(intent);
        }
        finish();
    }

    public void saveData()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(Names);
        editor.putString("names",json);
        editor.putBoolean("sos_flag",sos_flag);
        editor.apply();
        Log.e("SOS","------------ OnSaveData SOSActivity ---------------------- " + json+" "+sos_flag);
    }
    public void loadData()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("names",null);
        sos_flag = sharedPreferences.getBoolean("sos_flag",false);
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        Names = gson.fromJson(json,type);
        if (Names == null)
        {
            Names = new ArrayList<>();
        }
        Log.e("SOS","------------ . Sos ---------------------- "+Names +" "+sos_flag);
    }
}
