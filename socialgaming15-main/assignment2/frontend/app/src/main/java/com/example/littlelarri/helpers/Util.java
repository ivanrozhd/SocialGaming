package com.example.littlelarri.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.littlelarri.Player;

import com.example.littlelarri.R;
import com.example.littlelarri.activities.MainActivity;
import com.example.littlelarri.activities.MapActivity;
import com.example.littlelarri.activities.QRcodeActivity;
import com.example.littlelarri.trading.Trading;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Room for methods that we could use in any class
 * **/
public class Util {

    public static void logoutUser(FirebaseAuth firebaseAuth, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("logout");
        alertDialog.setMessage("Do you  REAAAAALLLY want to log out?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "logout", (dialog, which) -> {
            firebaseAuth.signOut();
            dialog.dismiss();
            context.startActivity(new Intent(context, MainActivity.class));
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public static void showPopupProfile(Player player, Context context, String currentUserUID) {
        Dialog profileDialog = new Dialog(context);
        profileDialog.setContentView(R.layout.profile_popup_window);

        Button windowCloseButton = profileDialog.findViewById(R.id.windowCloseButton);
        TextView levelText = profileDialog.findViewById(R.id.levelText);
        TextView nicknameText = profileDialog.findViewById(R.id.nicknameText);
        TextView uidText = profileDialog.findViewById(R.id.uidText);
        TextView resText1 = profileDialog.findViewById(R.id.resText1);
        TextView resText2 = profileDialog.findViewById(R.id.resText2);
        TextView resText3 = profileDialog.findViewById(R.id.resText3);
        TextView resText4 = profileDialog.findViewById(R.id.resText4);
        TextView resText5 = profileDialog.findViewById(R.id.resText5);
        TextView resText6 = profileDialog.findViewById(R.id.resText6);

        TextView friendCountText = profileDialog.findViewById(R.id.friendCountText);

        Button addFriendButton = profileDialog.findViewById(R.id.addFriendButton);
        Button tradeButton = profileDialog.findViewById(R.id.tradeButton);

        windowCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDialog.dismiss();
            }
        });

        levelText.setText((int) player.getLevel() + "");
        nicknameText.setText(player.getNickname());
        uidText.setText("#" + player.getUID());

        resText1.setText("" + player.getWater());
        resText2.setText("" + player.getPills());
        resText3.setText("" + player.getFood());
        resText4.setText("" + player.getBooks());
        resText5.setText("" + player.getToys());
        resText6.setText("" + player.getCoins());

        friendCountText.setText("" + player.getFriends().size() + " friends");
        if (player.isCurrentUser()) {
            addFriendButton.setVisibility(View.INVISIBLE);
            tradeButton.setVisibility(View.INVISIBLE);
        }
        if (player.getFriends().contains(currentUserUID)) {
            tradeButton.setOnClickListener(v -> {
                // Send trade request to other user and start new activity
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(context.getResources().getString(R.string.firebase_realtime_database_url));
                String tradingID = Trading.createTradeRequest(currentUserUID, player.getUID(), firebaseDatabase);
                QRcodeActivity.start(context, true, player.getNickname(), tradingID);
            });
            addFriendButton.setText("Unfriend");
            addFriendButton.setOnClickListener(v -> {
                player.removeFriend(currentUserUID);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (String friend : player.getFriends()) {
                    jsonArray.put(friend);
                }
                writeJSONArrayInDatabase(jsonArray, "friendsFirebaseUIDs", player.getUID(), context);

                PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(context);
                playerVolleyHelper.getPlayerByFirebaseUID(currentUserUID,
                        response -> {
                            JSONArray jsonArray2;
                            try {
                                jsonArray2 = response.getJSONArray("friendsFirebaseUIDs");
                                int index = -1;
                                for (int i = 0; i < jsonArray2.length(); i++) {
                                    if (jsonArray2.getString(i).compareTo(player.getUID()) == 0) {
                                        index = i;
                                        break;
                                    }
                                }
                                jsonArray2.remove(index);
                                writeJSONArrayInDatabase(jsonArray2, "friendsFirebaseUIDs", currentUserUID, context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.e("showPopupProfile", "Error in showPopupProfile (Util class)"));
                profileDialog.dismiss();
            });
        } else {
            addFriendButton.setText("Add Friend");
            addFriendButton.setOnClickListener(v -> {
                if (player.getFriendRequests().contains(currentUserUID)) {
                    return;
                }
                player.addFriendRequest(currentUserUID);
                JSONArray jsonArray = new JSONArray();
                for (String request : player.getFriendRequests()) {
                    jsonArray.put(request);
                }
                writeJSONArrayInDatabase(jsonArray, "friendRequestsUIDs", player.getUID(), context);
                addFriendButton.setVisibility(View.INVISIBLE);
            });
        }

        profileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profileDialog.show();
    }

    public static void writeJSONArrayInDatabase(JSONArray jsonArray, String jsonName, String firebaseUID, Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(jsonName, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(context);
        playerVolleyHelper.updatePlayer(firebaseUID, jsonObject,
                response -> {},
                error -> Log.e("writeJSONArrayInDatabase", "Updating Array (friends or friendRequests) didn't work", error));
    }

    public static Player createPlayerFromJSON(JSONObject jsonObject, String currentUserID) {
        String UID = null;
        String nickname = null;
        ArrayList<String> friends = new ArrayList<>();
        ArrayList<String> friendRequests = new ArrayList<>();
        float level = 0;
        int[] characteristics = new int[0];
        int[] resources = new int[0];
        boolean isCurrentUser = false;
        try {
            UID = jsonObject.getString("firebaseUID");
            nickname = jsonObject.getString("nickname");

            JSONArray jsonArray = jsonObject.getJSONArray("friendsFirebaseUIDs");
            for (int i = 0; i < jsonArray.length(); i++) {
                friends.add(jsonArray.getString(i));
            }

            JSONArray jsonArray2 = jsonObject.getJSONArray("friendRequestsUIDs");
            for (int i = 0; i < jsonArray2.length(); i++) {
                friendRequests.add(jsonArray2.getString(i));
            }

            level = jsonObject.getInt("level");

            int health = jsonObject.getInt("health");
            int hunger = jsonObject.getInt("hunger");
            int mood = jsonObject.getInt("mood");
            characteristics = new int[]{health, hunger, mood};

            int food = jsonObject.getInt("food");
            int pills = jsonObject.getInt("pills");
            int water = jsonObject.getInt("pizza");
            int books = jsonObject.getInt("book");
            int toys = jsonObject.getInt("toy");
            int coins = jsonObject.getInt("coins");
            resources = new int[]{water, food, pills, books, toys, coins};

            if (currentUserID.compareTo(UID) == 0)
                isCurrentUser = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Player(UID, nickname, friends, friendRequests, level, characteristics, resources, isCurrentUser);
    }

    public static boolean checkForEmptyInputs(Context context, String... strings) {
        for (String s : strings)
            if (s == null || s.isEmpty()) {
                showEmptyInputAlert(context);
                return true;
            }
        return false;
    }

    // shows an alert that is shown, when the input of text fields is empty (the alert is not empty)
    public static void showEmptyInputAlert(Context context) {
        new android.app.AlertDialog.Builder(context)
                .setTitle(R.string.alert_title_emptyInput).setMessage(R.string.alert_message_emptyInput)
                .setPositiveButton(R.string.alert_understood, null)
                .show();
    }
}
