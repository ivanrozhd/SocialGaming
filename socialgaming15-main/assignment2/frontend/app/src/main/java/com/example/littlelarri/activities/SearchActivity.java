package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.littlelarri.Player;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.helpers.RecycleViewAdapter;
import com.example.littlelarri.helpers.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private FirebaseAuth firebaseAuth;

    private EditText textInput;

    private FloatingActionButton searchButton;
    private Button backButton;

    private ImageView switchImage;

    private TextView userID_textview;
    private TextView username_textview;
    private TextView noResult_textview;

    private RecyclerView resultList;

    private boolean isUserIdSelected = false;

    private PlayerVolleyHelper playerVolleyHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseAuth = FirebaseAuth.getInstance();
        playerVolleyHelper = new PlayerVolleyHelper(this);

        textInput = findViewById(R.id.searchInputText);

        searchButton = findViewById(R.id.search_button2);
        backButton = findViewById(R.id.backButton_search);

        switchImage = findViewById(R.id.switchImage);

        userID_textview = findViewById(R.id.userIDChoice);
        username_textview = findViewById(R.id.usernameChoice);
        noResult_textview = findViewById(R.id.noResult_textview2);

        resultList = findViewById(R.id.result_recycler_view);
        resultList.setAdapter(RecycleViewAdapter.emptyAdapter(this));
        resultList.setLayoutManager(new LinearLayoutManager(this));

        switchImage.setOnClickListener(v -> clickSwitch());
        userID_textview.setOnClickListener(v -> {if (!isUserIdSelected) clickSwitch();});
        username_textview.setOnClickListener(v -> {if (isUserIdSelected) clickSwitch();});
        searchButton.setOnClickListener(v -> clickSearch());
        backButton.setOnClickListener(v -> onBackPressed());

    }

    private void clickSwitch() {
        isUserIdSelected = !isUserIdSelected;

        if (isUserIdSelected) {
            switchImage.setImageResource(R.drawable.search_switch_right);
            userID_textview.setBackground(getDrawable(R.drawable.search_textview_bg));
            username_textview.setBackground(null);
        } else {
            switchImage.setImageResource(R.drawable.search_switch_left);
            username_textview.setBackground(getDrawable(R.drawable.search_textview_bg));
            userID_textview.setBackground(null);
        }
    }

    private void clickSearch() {
        // hide keyboard bc searchButton was pressed
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        String search = textInput.getText().toString();

        if (search.length() == 0) {
            noResult_textview.setVisibility(View.VISIBLE);
            resultList.setAdapter(RecycleViewAdapter.emptyAdapter(this));
            resultList.setLayoutManager(new LinearLayoutManager(this));
            return;
        }

        if (isUserIdSelected) {
            playerVolleyHelper.findPlayersByFirebaseUIDBefore(search,
                    this::setSearchList,
                    error -> Log.w(TAG, "Error in clickSearch in if-case (SearchActivity)!")
            );
        } else {
            playerVolleyHelper.findPlayersByNicknameContaining(search,
                    this::setSearchList,
                    error -> Log.w(TAG, "Error in clickSearch in else-case (SearchActivity)!")
            );
        }
    }

    private void setSearchList(JSONArray jsonArray) {
        try {
            if (jsonArray.length() == 0)
                noResult_textview.setVisibility(View.VISIBLE);
            else
                noResult_textview.setVisibility(View.INVISIBLE);

            ArrayList<Player> playerArrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                playerArrayList.add(Util.createPlayerFromJSON(jsonArray.getJSONObject(i), firebaseAuth.getCurrentUser().getUid()));
            }
            RecycleViewAdapter adapter = new RecycleViewAdapter(this, playerArrayList, false, isUserIdSelected,
                    pos -> Util.showPopupProfile(playerArrayList.get(pos), this, firebaseAuth.getCurrentUser().getUid()));
            resultList.setAdapter(adapter);
            resultList.setLayoutManager(new LinearLayoutManager(this));
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}