package com.es3fny.Main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.es3fny.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.HashMap;
import java.util.Map;



public class  LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);

        mEmail =  findViewById(R.id.edemail);
        mPassword = findViewById(R.id.edpassword);
        Button mLoginBtn = findViewById(R.id.btnLogin);
        Button mRegPageBtn = findViewById(R.id.btnLinkToRegisterScreen);
        Button mSkipBtn = findViewById(R.id.btnSkip);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mRegPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SkipHome.class);
                startActivity(intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String Password = mPassword.getText().toString();
                if(email.equals("")){
                    Toast.makeText(LoginActivity.this, R.string.enter_email,Toast.LENGTH_SHORT).show();
                }else if (Password.equals("")){
                    Toast.makeText(LoginActivity.this, R.string.enter_password,Toast.LENGTH_SHORT).show();
                }else if(!isNetworkAvailable()){
                    Toast.makeText(LoginActivity.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
                }else{
                mAuth.signInWithEmailAndPassword(email,Password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    // Sign in success, update UI with the signed-in user's information
                                    String token_Id = FirebaseInstanceId.getInstance().getToken();
                                    String current_Id = mAuth.getCurrentUser().getUid();
                                    MyBackgroundService.mCurrentID =current_Id;
                                    String mCurrentName = mAuth.getCurrentUser().getDisplayName();

                                    Toast.makeText(LoginActivity.this,getString(R.string.welcome)+" "+ mCurrentName,Toast.LENGTH_SHORT).show();
                                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("example_text",mCurrentName);
                                    editor.apply();

                                    Map<String, Object> tokenMap = new HashMap<>();
                                    tokenMap.put("token_id",token_Id);
                                    mFirestore.collection("Users").document(current_Id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            SendToMain();
                                        }
                                    });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this,"Error : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            }
        });

}

    private void SendToMain() {
        Intent intent = new Intent(LoginActivity.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}