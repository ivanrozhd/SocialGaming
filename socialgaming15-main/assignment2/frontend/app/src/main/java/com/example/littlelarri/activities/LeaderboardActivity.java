package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.littlelarri.helpers.RecycleViewAdapter;
import com.example.littlelarri.Player;
import com.example.littlelarri.helpers.MenuBarHelper;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderbordActivity";
    private FirebaseAuth firebaseAuth;
    private PlayerVolleyHelper playerVolleyHelper;

    private Button friendsButton;
    private Button rankButton;
    private FloatingActionButton searchButton;

    private RecyclerView shownList;

    private boolean isShownRanklist = false;

    private TextView noResultTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        firebaseAuth = FirebaseAuth.getInstance();
        playerVolleyHelper = new PlayerVolleyHelper(this);

        friendsButton = findViewById(R.id.friendsButton);
        rankButton = findViewById(R.id.rankButton);
        searchButton = findViewById(R.id.searchButton);

        shownList = findViewById(R.id.recyclerview_list);
        shownList.setAdapter(RecycleViewAdapter.emptyAdapter(this));
        shownList.setLayoutManager(new LinearLayoutManager(this));
        fillListWithFriends();

        noResultTextview = findViewById(R.id.noResult_textview);

        // show list of all Friends
        friendsButton.setOnClickListener(v -> clickFriendsButton());

        // show world rank of best players
        rankButton.setOnClickListener(v -> clickRankButton());

        searchButton.setOnClickListener(v -> clickSearchButton());

        MenuBarHelper.setup(this, LeaderboardActivity.this, R.id.menuToLeaderboard);
    }

    private void clickSearchButton() {
        startActivity(new Intent(LeaderboardActivity.this, SearchActivity.class));
    }

    private void clickFriendsButton() {
        friendsButton.setBackgroundColor(getColor(R.color.teal_200));
        rankButton.setBackgroundColor(getColor(R.color.purple_500));
        isShownRanklist = false;

        fillListWithFriends();
    }

    private void clickRankButton() {
        rankButton.setBackgroundColor(getColor(R.color.teal_200));
        friendsButton.setBackgroundColor(getColor(R.color.purple_500));
        isShownRanklist = true;

        fillListWithRank();
    }

    private void fillListWithFriends() {
        playerVolleyHelper.getFriendsOfFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                this::setShownList,
                error -> Log.w(TAG, "Error in fillListWithFriends (LeaderbordActivity)!")
        );
    }

    private void fillListWithRank() {
        playerVolleyHelper.findAllByOrderByLevelDesc(
                this::setShownList,
                error -> Log.w(TAG, "Error in fillListWithRank (LeaderbordActivity)!")
        );
    }

    private void setShownList(JSONArray jsonArray) {
        if (jsonArray.length() == 0)
            noResultTextview.setVisibility(View.VISIBLE);
        else
            noResultTextview.setVisibility(View.INVISIBLE);

        ArrayList<Player> playerArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                playerArrayList.add(Util.createPlayerFromJSON(jsonArray.getJSONObject(i), firebaseAuth.getCurrentUser().getUid()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RecycleViewAdapter adapter = new RecycleViewAdapter(this, playerArrayList, isShownRanklist, false,
                pos -> Util.showPopupProfile(playerArrayList.get(pos), this, firebaseAuth.getCurrentUser().getUid()));
        shownList.setAdapter(adapter);
        shownList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        Util.logoutUser(firebaseAuth, LeaderboardActivity.this);
    }
}