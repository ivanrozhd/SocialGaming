package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.littlelarri.MyFirebaseMessagingService;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterAcitivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // get firebase instance
        firebaseAuth = FirebaseAuth.getInstance();

        // get the user information from the EditText fields via id
        final EditText emailText = findViewById(R.id.email);
        final EditText passwordText = findViewById(R.id.password);
        final EditText nicknameText = findViewById(R.id.nickname);

        //switch to the login activity
        loginButton.setOnClickListener(v -> startActivity(new Intent(RegisterAcitivity.this, MainActivity.class)));

        registerButton.setOnClickListener(v -> {
            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();
            String nickname = nicknameText.getText().toString();
            if (!Util.checkForEmptyInputs(RegisterAcitivity.this, email, password, nickname))
                registerUser(email, password, nickname);
        });
    }

    private void registerUser(String email, String password, String nickname){
        // register the user firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful())
            {
                Log.d(TAG, "registerUser: User successfully registered!");
                registerUserInSpringBackend(firebaseAuth.getCurrentUser().getUid(), nickname );
                Toast.makeText(RegisterAcitivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            } else{
                Log.d(TAG, "registerUser: failed to register :(", task.getException());
                if (task.getException() != null && task.getException().getClass() == FirebaseAuthUserCollisionException.class) {
                    String message = String.format(getResources().getString(R.string.alert_message_emailAlreadyExists), email);
                    new AlertDialog.Builder(RegisterAcitivity.this)
                            .setTitle(R.string.alert_title_emailAlreadyExists)
                            .setMessage(message)
                            .setPositiveButton(R.string.alert_understood, null)
                            .show();
                }
                else
                    Toast.makeText(RegisterAcitivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUserInSpringBackend(String firebaseUID, String nickname) {
        try {
            // Convert user information to JSON
            JSONObject playerJSON = new JSONObject();
            playerJSON.put("firebaseUID", firebaseUID);
            playerJSON.put("nickname", nickname);
            playerJSON.put("friendsFirebaseUIDs", new JSONArray());// TODO: friends do not have to be passed to the database
            //TODO disable buttons and stuff until response is received or/and show loading symbol
            PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
            playerVolleyHelper.postRegisterNewUser(playerJSON,
                    response -> {
                        Log.d(TAG, "registerUserInSpringBackend: User successfully registered in Spring backend!");
                        switchToHomeScreenActivity();
                        MyFirebaseMessagingService.updateFCMToken(RegisterAcitivity.this);
                    },
                    error -> {
                        Log.d(TAG, "registerUserInSpringBackend: Failed to register user in Spring backend!", error);

                        // Delete the Firebase account
                        firebaseAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Log.i(TAG, "Firebase user account deleted, after registerUserInSpringBackend failed!");
                            } else {
                                Log.e(TAG, "Firebase user not account deleted, after registerUserInSpringBackend failed!", task.getException());
                                Log.e(TAG, "Additional steps required!");
                                // Additional steps required
                            }
                        });
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(RegisterAcitivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void switchToHomeScreenActivity() {
        Intent intent = new Intent(RegisterAcitivity.this, LarryPageActivity.class);
        startActivity(intent);
    }
}
