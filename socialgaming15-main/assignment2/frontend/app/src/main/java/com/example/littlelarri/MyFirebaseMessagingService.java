package com.example.littlelarri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.littlelarri.activities.TradingDialogActivity;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.helpers.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

// https://firebase.google.com/docs/cloud-messaging/android/client
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final static String TAG = "MyFirebaseMessagingService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        setFCMTokenInDatabase(token, this);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        // TODO understand why messages are received twice
        Log.d(TAG, "Message received with name: " + message.getData().get("requesterName") + ", tradingID: "
                + message.getData().get("tradingID"));
        String tradingID = message.getData().get("tradingID");
        if (tradingID != null && message.getData().get("requesterName") != null)
            TradingDialogActivity.showDialog(MyFirebaseMessagingService.this, tradingID, message.getData().get("requesterName"));
    }

        private static void setFCMTokenInDatabase(String token, Context context) {
        JSONObject playerJSON = new JSONObject();
        try {
            playerJSON.put("fcmtoken", token);
        } catch (JSONException e) {
            Log.e(TAG, "setFCMTokenInDatabase: Could not create JSON!");
            return;
        }
        PlayerVolleyHelper volleyHelper = new PlayerVolleyHelper(context);
        String uid = FirebaseAuth.getInstance().getUid();
        if(uid == null)
            Log.e(TAG, "setFCMTokenInDatabase: Couldn't retrieve firebaseUID");
        else
            volleyHelper.updatePlayer(uid, playerJSON, response -> {
                Log.i(TAG, "Player FCM token in database updated");
            }, error -> {
                Log.e(TAG, "setFCMTokenInDatabase: Could not update the fcm token in the database!");
            });
    }

    public static void updateFCMToken(Context context) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                setFCMTokenInDatabase(task.getResult(), context);
            else
                Log.e(TAG, "updateFCMToken: Could not get FCM token!");
        });
    }

    // TODO: check for google play services? (GoogleApiAvailability.makeGooglePlayServicesAvailable())
//    FirebaseMessaging.getInstance().getToken()
//    .addOnCompleteListener(new OnCompleteListener<String>() {
//        @Override
//        public void onComplete(@NonNull Task<String> task) {
//            if (!task.isSuccessful()) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                return;
//            }
//
//            // Get new FCM registration token
//            String token = task.getResult();
//
//            // Log and toast
//            String msg = getString(R.string.msg_token_fmt, token);
//            Log.d(TAG, msg);
//            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//        }
//    });
}
