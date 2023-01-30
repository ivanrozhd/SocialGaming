package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.littlelarri.AppLifecycleListener;
import com.example.littlelarri.MyFirebaseMessagingService;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // connect code with style, R = Resource, layout = layout folder

        // Register observer, that detects when the app is in fore-/background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener(MainActivity.this));

        firebaseAuth = FirebaseAuth.getInstance();

        // get current user from a database
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            Button registerButton = findViewById(R.id.registerButton);
            Button loginButton = findViewById(R.id.loginButton);

            // get  EditText field values
            final EditText email = findViewById(R.id.email);
            final EditText password = findViewById(R.id.password);
             // send login information to firebase
            loginButton.setOnClickListener(v -> {
                String mailString = email.getText().toString();
                String pwdString = password.getText().toString();
                if (Util.checkForEmptyInputs(MainActivity.this, pwdString, mailString))
                    return;
                loginUser(mailString, pwdString);
            });
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegisterAcitivity.class);
                startActivity(intent);
            });
        }else{
            switchToHomeScreenActivity();
        }
    }

    private void loginUser(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful())
            {
                Log.d(TAG, "loginUser: User successfully logged in!");
                MyFirebaseMessagingService.updateFCMToken(MainActivity.this);
                switchToHomeScreenActivity();
            } else{
                Log.d(TAG, "loginUser: failed to log in :(", task.getException());
                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToHomeScreenActivity() {
        Intent intent = new Intent(MainActivity.this, LarryPageActivity.class);
        // If there are extras pass them to LarryPage
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}