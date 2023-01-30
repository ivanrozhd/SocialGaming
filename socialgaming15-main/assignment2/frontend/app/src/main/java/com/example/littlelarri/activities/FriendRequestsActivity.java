package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.littlelarri.Player;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.FriendsRequestRecycleViewAdapter;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.helpers.Util;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FriendRequestsActivity extends AppCompatActivity {
    private static final String TAG = "FriendRequestsActivity";
    private FirebaseAuth firebaseAuth;
    private PlayerVolleyHelper playerVolleyHelper;

    RecyclerView friendRequestList;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        playerVolleyHelper = new PlayerVolleyHelper(this);
        firebaseAuth = FirebaseAuth.getInstance();

        friendRequestList = findViewById(R.id.friend_request_recyclerview);
        friendRequestList.setAdapter(FriendsRequestRecycleViewAdapter.emptyAdapter(this));
        friendRequestList.setLayoutManager(new LinearLayoutManager(this));

        fillFriendRequestList();

        backButton = findViewById(R.id.backButton_friendrequest);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void fillFriendRequestList() {
        playerVolleyHelper.getFriendRequestsOfFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                this::setFriendRequestList,
                error -> Log.e(TAG, "getting all friend requests didn't work", error)
        );
    }

    private void setFriendRequestList(JSONArray jsonArray) {
        ArrayList<Player> playerArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                playerArrayList.add(Util.createPlayerFromJSON(jsonArray.getJSONObject(i), firebaseAuth.getCurrentUser().getUid()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setFriendRequestList(playerArrayList);
    }

    private void setFriendRequestList(ArrayList<Player> playerArrayList) {
        FriendsRequestRecycleViewAdapter adapter = new FriendsRequestRecycleViewAdapter(this, playerArrayList,
                pos -> Util.showPopupProfile(playerArrayList.get(pos), this, firebaseAuth.getCurrentUser().getUid()),
                pos -> removeFriendRequest(pos, playerArrayList),
                pos -> {
                    removeFriendRequest(pos, playerArrayList);
                    addFriendtoPlayer(firebaseAuth.getCurrentUser().getUid(), playerArrayList.get(pos).getUID());
                    addFriendtoPlayer(playerArrayList.get(pos).getUID(), firebaseAuth.getCurrentUser().getUid());
                });
        friendRequestList.setAdapter(adapter);
        friendRequestList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void removeFriendRequest(int position, ArrayList<Player> playerArrayList) {
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = response.getJSONArray("friendRequestsUIDs");
                        int index = -1;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String jsonUID = jsonArray.getString(i);
                            String playerUID = playerArrayList.get(position).getUID();
                            if (jsonUID.compareTo(playerUID) == 0) {
                                index = i;
                            }
                        }
                        jsonArray.remove(index);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Util.writeJSONArrayInDatabase(jsonArray, "friendRequestsUIDs", firebaseAuth.getCurrentUser().getUid(), this);
                    playerArrayList.remove(playerArrayList.get(position));
                    setFriendRequestList(playerArrayList);
                },
                error -> Log.e(TAG, "Didn't get Player from backend in removeFriendRequest()", error)
        );
    }

    private void addFriendtoPlayer(String uid, String friendUID) {
        playerVolleyHelper.getPlayerByFirebaseUID(uid,
                response -> {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = response.getJSONArray("friendsFirebaseUIDs");
                        boolean containsAlready = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (jsonArray.getString(i).compareTo(friendUID) == 0) {
                                containsAlready = true;
                            }
                        }
                        if (!containsAlready)
                            jsonArray.put(friendUID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Util.writeJSONArrayInDatabase(jsonArray, "friendsFirebaseUIDs", uid, this);
                },
                error -> Log.e(TAG, "Didn't get Player from backend in addFriendtoPlayer()", error)
        );
    }
}