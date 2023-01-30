package com.example.littlelarri.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

//import com.android.volley.Response;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        firebaseAuth = FirebaseAuth.getInstance();

        addUserNicknameToWelcomeText();

        // get buttons
        Button larryPageButton = findViewById(R.id.larryPageButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        larryPageButton.setOnClickListener(view -> switchToLarryPageActivity());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    // show user message that he is about tu log out
    private void logoutUser(){
        AlertDialog alertDialog = new AlertDialog.Builder(WelcomeActivity.this).create();
        alertDialog.setTitle("logout");
        alertDialog.setMessage("Do you  REAAAAALLLY want to log out?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "logout", (dialog, which) -> {
            firebaseAuth.signOut();
            dialog.dismiss();
            switchToMainActivity();

        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void switchToMainActivity(){
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void switchToLarryPageActivity() {
        Intent intent = new Intent(WelcomeActivity.this, LarryPageActivity.class);
        startActivity(intent);
    }

    private void addUserNicknameToWelcomeText() {
        TextView welcomeText = findViewById(R.id.welcomeText);
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        welcomeText.setText(welcomeText.getText() + " " + response.getString("nickname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "addUserNicknameToWelcomeText: Did not get nickname from backend!", error)
        );

    }

    @Override
    public void onBackPressed() {
        logoutUser();
    }
}