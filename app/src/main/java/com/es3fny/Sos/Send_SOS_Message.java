package com.es3fny.Sos;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.Main.SettingsActivity;
import com.es3fny.Main.ShowNotifications;
import com.es3fny.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;



public class Send_SOS_Message extends AppCompatActivity {
    Button buttonSend;
    public ArrayList<Integer> index_arr = new ArrayList<>();
    public ArrayList<String> Names = new ArrayList<>();
    public ArrayList<String> Numbers = new ArrayList<>();
    public boolean sos_switch;
    public TextView nameView;
    public TextView phoneView;
    private boolean Language_Changed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__sos__message);

        Language_Changed = getIntent().getBooleanExtra("Language_Changed",false);

        buttonSend =  findViewById(R.id.buttonSend);
        RelativeLayout parent_Relative_layout = findViewById(R.id.parent_Relative_layout2);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Notifications");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getTitle());

       loadData();
//------------------------------------------------------------------------------------------------------------------------------------------------
        for (int i=0; i<Names.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") RelativeLayout rowView = (RelativeLayout) inflater.inflate(R.layout.field2, null);
            Random r = new Random();
            int ii = r.nextInt(1000 - 1) + 1;

            rowView.setId(ii);
            index_arr.add(ii);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (parent_Relative_layout.getChildCount() > 0) {
                int y = parent_Relative_layout.getChildCount() - 1;
                params.addRule(RelativeLayout.BELOW, index_arr.get(y));
                rowView.setLayoutParams(params);
            }
            // Add the new row before the add field button.
            parent_Relative_layout.addView(rowView, params);
            nameView = rowView.findViewById(R.id.textName);
            phoneView = rowView.findViewById(R.id.textPhone);

            nameView.setText(Names.get(i));
            phoneView.setText(Numbers.get(i));
        }
//------------------------------------------------------------------------------------------------------------------------------------------------

        buttonSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String smss = "Please Help Me!";
                Intent intent=new Intent(getApplicationContext(),Send_SOS_Message.class);

                Intent iiintent = new Intent(Send_SOS_Message.this, SosActivity.class);

                iiintent.putExtra("names",Names);
                iiintent.putExtra("numbers",Numbers);
                iiintent.putExtra("sos_switch",sos_switch);
                PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

                try {
                    SmsManager sms = SmsManager.getDefault();
                    for (int i=0; i<Numbers.size();i++) {
                        String temp = Numbers.get(i);
                        sms.sendTextMessage(temp, null, smss, pi, null);
                        Toast.makeText(getApplicationContext(), R.string.sms_sent, Toast.LENGTH_LONG).show();
                    }
                    startActivity(iiintent);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            R.string.sms_failed,
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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
        String load="";
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
    public void onBackPressed() {
        super.onBackPressed();
        if(Language_Changed){
            Intent intent = new Intent(this,SosActivity.class);
            intent.putExtra("Language_Changed",Language_Changed);
            startActivity(intent);
        }
        finish();
    }
    public void loadData()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("names",null);
        String json2 = sharedPreferences.getString("numbers",null);
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        Names = gson.fromJson(json,type);
        Numbers = gson.fromJson(json2,type);
        if (Names == null)
        {
            Names = new ArrayList<>();
            Numbers = new ArrayList<>();
        }

    }

}
