package com.shofiul.sk.anondoride.anondoride.models;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shofiul.sk.anondoride.anondoride.R;

public class FirebaseMethodes {
    private static final String TAG = "FirebaseMethodes";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Context mContext;
    String mUserId;

    public FirebaseMethodes(Context context) {
        mContext = context;
        mAuth= FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        if(mAuth.getCurrentUser() != null){
            mUserId = mAuth.getCurrentUser().getUid();

        }
    }



    public void addNewDriver( String username, String email, String userid, long phonenumber){

      Driver driver = new Driver(
              username ,
              email,
              "",
              "",
              "",
              userid,
              phonenumber
      );

        Log.d(TAG, "addNewDriver: Inserting data: "+ driver);

        mDatabaseReference.child(mContext.getString(R.string.Users))
              .child(mContext.getString(R.string.Drivers))
              .child(userid).setValue(driver);


    }
}
