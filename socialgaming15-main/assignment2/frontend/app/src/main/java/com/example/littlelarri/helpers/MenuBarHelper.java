package com.example.littlelarri.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.littlelarri.R;
import com.example.littlelarri.activities.InventoryActivity;
import com.example.littlelarri.activities.LarryPageActivity;
import com.example.littlelarri.activities.LeaderboardActivity;
import com.example.littlelarri.activities.MapActivity;

public class MenuBarHelper {
    public static void setup(AppCompatActivity activity, Context context, int activeId) {
        // Initialise all menu bar onClick events
        activity.findViewById(R.id.menuToInventory).setOnClickListener(v ->
                activity.startActivity(new Intent(context, InventoryActivity.class)));
        activity.findViewById(R.id.menuToLarri).setOnClickListener(v ->
                activity.startActivity(new Intent(context, LarryPageActivity.class)));
        activity.findViewById(R.id.menuToMap).setOnClickListener(v ->
            activity.startActivity(new Intent(context, MapActivity.class)));
        activity.findViewById(R.id.menuToLeaderboard).setOnClickListener(v ->
                activity.startActivity(new Intent(context, LeaderboardActivity.class)));

        // Remove onClickEvent from active and change color
        View active = activity.findViewById(activeId);
        active.setOnClickListener(v -> {});
        active.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200));
    }
}
