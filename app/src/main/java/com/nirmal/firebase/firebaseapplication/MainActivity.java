package com.nirmal.firebase.firebaseapplication;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String TAG = "FirebaseSample";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView email;
    private TextView password;
    private TextView text1;
    private Button login;
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = (TextView) findViewById(R.id.text1);
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);



        database = FirebaseDatabase.getInstance();
        setValueListener();
        addValue("test1");

        mAuth = FirebaseAuth.getInstance();
        setAuthListener();
    }

    public void addValue(String value) {
        myRef.setValue(value);
    }

    public void setValueListener() {
        myRef = database.getReference("message");
        myRef.addValueEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            String value = dataSnapshot.getValue(String.class);
            Log.d(TAG, "Value is: " + value);
            text1.setText(dataSnapshot.toString());
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
            text1.setText(error.getMessage());
        }
    };

    public void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    getUserInfo(user);
                } else {
                    // User is signed out
                    login.setText("Login");
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    public void SignInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            //  Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                            //        Toast.LENGTH_SHORT).show();
                            if (valueEventListener != null)
                                myRef.removeEventListener(valueEventListener);
                            login.setText("Sign In");
                        }else {
                            setValueListener();
                            login.setText("Sign Out");
                        }
                        // ...
                    }
                });
    }

    public void login(View view) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            SignInUser(email.getText().toString(), password.getText().toString());
        else
            mAuth.signOut();
    }

    public void writevalue(View view) {
        addValue("dynamicvalue" + new Date().toString());
    }

    public void getUserInfo(FirebaseUser user) {
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
            text1.setText(uid + "-->" + name + "---->" + email);
        }
    }
}
