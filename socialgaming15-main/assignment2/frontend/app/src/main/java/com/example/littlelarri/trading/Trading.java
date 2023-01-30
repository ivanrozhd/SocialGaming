package com.example.littlelarri.trading;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.littlelarri.R;
import com.example.littlelarri.activities.QRcodeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trading {
    // These constants also need to be updated in the backend when changed
    public final static String ACTION_KEY = "action";
    public final static String ACTION_ACCEPTED = "accepted";
    public final static String ACTION_REJECTED = "rejected";
    public final static String ACTION_REQUESTED = "requested";
    public final static String ACTION_CANCELED = "canceled";
    public final static String ACTION_SCANNED = "scanned";
    public final static String ACTION_COMPLETED = "completed";
    public final static String ACTION_ERROR = "error";

    public final static int TRADING_ITEMS_AMOUNT = 6;
    public final static int TRADING_MOOD_INCREASE = 20;
    public final static int TRADING_FRIEND_BONUS = 5;

    public final static String KEY_ACCEPT_OFFER = "acceptOffer";

    public static String createTradeRequest(String requesterID, String requestedID, FirebaseDatabase firebaseDatabase) {
        Map<String, Object> items = new HashMap<>();
        items.put("requester", createUserInfo(requesterID));
        items.put("requested", createUserInfo(requestedID));
        items.put(Trading.ACTION_KEY, Trading.ACTION_REQUESTED);
        DatabaseReference tradingInstance = firebaseDatabase.getReference().child("trading").push();
        tradingInstance.setValue(items);
        return tradingInstance.getKey();
    }

    private static Map<String, Object> createUserInfo(String firebaseUID) {
        Map<String, Object> user = new HashMap<>();
        user.put("firebaseUID", firebaseUID);
        user.put(KEY_ACCEPT_OFFER, false);
        Map<String, Integer> items = new HashMap<>();
        items.put("0", 0);
        user.put("items", items);
        return user;
    }

    public static void showTradeCanceledAlert(Context context, DialogInterface.OnClickListener onClick, String otherUserName) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.alert_title_trade_canceled)
                .setMessage(String.format(context.getResources().getString(R.string.alert_message_trade_canceled), otherUserName))
                .setPositiveButton(R.string.alert_understood, onClick)
                .create().show();
    }

    public static void showTradeErrorAlert(Context context, DialogInterface.OnClickListener onClick) {
        if (!((Activity)context).isFinishing())
            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_title_trade_error)
                    .setMessage(R.string.alert_message_trade_error)
                    .setPositiveButton(R.string.alert_understood, onClick)
                    .create()
                    .show();
    }
}
