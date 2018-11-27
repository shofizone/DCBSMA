package com.shofiul.sk.anondoride.anondoride.driverlogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shofiul.sk.anondoride.anondoride.DriversMapsActivity;
import com.shofiul.sk.anondoride.anondoride.MainActivity;
import com.shofiul.sk.anondoride.anondoride.R;

public class DriverLoginActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;



Context mContext = DriverLoginActivity.this;
    private static final String TAG = "DriverLoginActivity";

    //Widgets
    private EditText mEmail,mPassword;
    private Button mLogin, mReg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);


        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(DriverLoginActivity.this,DriversMapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email_driver_login);
        mPassword = (EditText) findViewById(R.id.password_driver_login);
        mLogin = (Button) findViewById(R.id.email_sign_in_button_driver_login);
        mReg = (Button) findViewById(R.id.email_reg_button_driver_login);



        mReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mEmail.getText().toString();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DriverLoginActivity.this,"Sign Up Error",Toast.LENGTH_LONG).show();
                        }else {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserDatabaseReference = FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(getString(R.string.Users))
                                    .child(getString(R.string.Drivers))
                                    .child(user_id);
                            currentUserDatabaseReference.setValue(true);
                            Toast.makeText(DriverLoginActivity.this,"Sign Up Successful",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mEmail.getText().toString();

                mAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(mContext,"Sign In Error",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(mContext,"Sign Successful",Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
