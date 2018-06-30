package com.es3fny.Sos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.es3fny.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;



public class contacts extends Activity {

    private RelativeLayout parent_Relative_layout;
    public TextView nameView;
    public TextView phoneView;
    private static final int RESULT_PICK_CONTACT = 85500;
    public ArrayList<String> Names;
    public ArrayList<String> Numbers;
    public ArrayList<Integer> index_arr;
    public int count;
    public boolean sos_flag;
    String[] test_numbers;
    List<String> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        test_numbers = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        list = Arrays.asList(test_numbers);
        index_arr = new ArrayList<>();


        Toolbar toolbar = findViewById(R.id.app_bar);
        TextView mTitle =  toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Add/Remove Contact");
//------------------------------------------------------------------------------------------------------------
        loadData();

        parent_Relative_layout = findViewById(R.id.parent_Relative_layout);

        if (Names != null) {
            for (int i = 0; i < Names.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                @SuppressLint("InflateParams") RelativeLayout rowView = (RelativeLayout) inflater.inflate(R.layout.field, null);
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
        }
    }
    public void onAddField(View v) {
        if(count>=3)
        {
            Toast.makeText(getApplicationContext(), R.string.add_3_contacts,Toast.LENGTH_SHORT).show();
        }
        else {
            pickContact();
        }
    }

    public void onDelete(View v) {
        parent_Relative_layout.removeView((View) v.getParent());
        int t = ((View)v.getParent()).getId();
        int in = index_arr.indexOf(t);
        index_arr.remove(in);
        Names.remove(in);
        Numbers.remove(in);
        for (int j=1; j<parent_Relative_layout.getChildCount(); j++)
        {
            RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.BELOW,index_arr.get(j-1) );
            RelativeLayout r = parent_Relative_layout.findViewById(index_arr.get(j));
            r.setLayoutParams(params);
        }
        count--;
        saveData();
        Toast.makeText(getApplicationContext(), R.string.contact_deleted_success,Toast.LENGTH_SHORT).show();
    }
    public void pickContact()
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("SosActivity", "Failed to pick contact");
        }
    }
    /**
     * Query the Uri and read contact details. Handle the picked contact data.
     * @param data
     */
    @SuppressLint("Recycle")
    private void contactPicked(Intent data) {
        Log.e("SOS","------------ On contactPicked ----------------------");
        Cursor cursor;
        try {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") RelativeLayout rowView = (RelativeLayout) inflater.inflate(R.layout.field, null);
                Random r = new Random();
                int i = r.nextInt(1000 - 1) + 1;

                rowView.setId(i);
                index_arr.add(i);
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

                //---------------------------------------------------------------------------------------------------------------------
                String test_phoneNo;
            StringBuilder temp_phoneNo = new StringBuilder();
            StringBuilder phoneNo = new StringBuilder();
                String name;
                // getData() method will have the Content Uri of the selected contact
                Uri uri = data.getData();
                //Query the content uri
            assert uri != null;
            cursor = getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
                // column index of the phone number
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                // column index of the contact name
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                test_phoneNo = cursor.getString(phoneIndex);
                name = cursor.getString(nameIndex);

                for (int k=0; k<test_phoneNo.length(); k++)
                {
                    String temp = "";
                    temp +=test_phoneNo.charAt(k);
                        if (list.contains(temp)) {
                            temp_phoneNo.append(test_phoneNo.charAt(k));
                        }
                }
                for (int k=0; k<temp_phoneNo.length();k++)
                {
                    if (k==0 && (temp_phoneNo.charAt(0) == '2'))
                    {
                        if ((temp_phoneNo.charAt(1) != '0')) {
                            phoneNo.append('0');
                        }
                    }
                    else
                    {
                        phoneNo.append(temp_phoneNo.charAt(k));
                    }
                }
                // Set the value to the textviews

                if (name != null) {

                    nameView.setText(name);
                    Names.add(name);
                    phoneView.setText(phoneNo.toString());
                    Numbers.add(phoneNo.toString());
                    Log.e("SOS", Names+" "+Numbers+" "+count);
                    count++;
                    Toast.makeText(this, R.string.contact_added_success, Toast.LENGTH_SHORT).show();
                    saveData();
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveData()
    {
        Log.e("SOS","------------ OnSaveData Contacts ----------------------");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(Names);
        String json2 = gson.toJson(Numbers);
        editor.putString("names",json);
        editor.putString("numbers",json2);
        editor.putInt("count",Names.size());
        editor.putBoolean("sos_flag",sos_flag);
        editor.commit();
        Log.e("SOS","------------ OnSaveData Contacts ---------------------- " + json+" "+json2+" "+sos_flag);
    }
    public void loadData()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("names",null);
        String json2 = sharedPreferences.getString("numbers",null);
        count = sharedPreferences.getInt("count",0);
        sos_flag = sharedPreferences.getBoolean("sos_flag",true);
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        Names = gson.fromJson(json,type);
        Numbers = gson.fromJson(json2,type);
        if (Names == null | Numbers == null)
        {
            Names = new ArrayList<>();
            Numbers = new ArrayList<>();
            count =0;
        }
        Log.e("SOS","------------ OnLoadData Contacts ---------------------- "+Names +" "+Numbers+" "+count);
    }
}
