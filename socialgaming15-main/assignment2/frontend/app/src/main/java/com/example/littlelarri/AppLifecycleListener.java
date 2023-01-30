package com.example.littlelarri;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Arrays;

public class AppLifecycleListener implements DefaultLifecycleObserver {
    private static final String TAG = "AppLifecycleListener";

    private Context context;

    public AppLifecycleListener(Context context) {
        this.context = context;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {// App moved to foreground
        DefaultLifecycleObserver.super.onStart(owner);
        Log.i(TAG, "App to foreground");
        // TODO: when open app by click on notification method is called twice, why?
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, notificationManager.getActiveNotifications().length + " unread messages.");
        Log.d(TAG, Arrays.toString(notificationManager.getActiveNotifications()));
        // TODO: process unread messages
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {// App moved to background
        DefaultLifecycleObserver.super.onStop(owner);
        Log.i(TAG, "App to background");
    }
}
