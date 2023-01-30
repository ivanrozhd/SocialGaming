package com.example.littlelarri.trading;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.littlelarri.activities.QRcodeActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class TradingInitializerChildEventListener implements ChildEventListener {
    private final static String TAG = "TradingInitializerChildEventListener";

    private QRcodeActivity qrCodeActivity;

    public TradingInitializerChildEventListener(Context context) {
        qrCodeActivity = (QRcodeActivity) context;
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        if (Trading.ACTION_KEY.equals(snapshot.getKey())) {
            String action = snapshot.getValue(String.class);
            switch (action) {// TODO why is language level 8? change and replace by enhanced switch?
                case Trading.ACTION_ACCEPTED:
                    qrCodeActivity.onOtherUserAccept();
                    break;
                case Trading.ACTION_REJECTED:
                    qrCodeActivity.onOtherUserReject();
                    break;
                case Trading.ACTION_CANCELED:
                    qrCodeActivity.onOtherUserCanceled();
                    break;
                case Trading.ACTION_SCANNED:
                    qrCodeActivity.onOtherUserScanned();
                    break;
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "An error occurred in the firebase realtime database trading data!");
        Log.e(TAG, error.toString());
        qrCodeActivity.onError();
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
}
