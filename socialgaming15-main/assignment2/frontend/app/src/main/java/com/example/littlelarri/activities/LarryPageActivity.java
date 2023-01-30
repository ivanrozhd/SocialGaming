package com.example.littlelarri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.content.ClipData;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.littlelarri.Player;
import com.example.littlelarri.helpers.MenuBarHelper;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class LarryPageActivity extends AppCompatActivity {
    private static final String TAG = "LarryPageActivity";

    FirebaseAuth firebaseAuth;
    FloatingActionButton pop_up, resource1, resource2, resource3;
    Boolean pop_up_open = false;
    ImageView myImageView;
    ImageView water;
    ImageView pills;
    ImageView food;
    ImageView book, toy, coins;
    TextView textView_water;
    TextView textView_pills;
    TextView textView_pizza, textView_book, textView_toy, textView_coins, level_text;
    ProgressBar progressBar;
    ProgressBar progressBarMood;
    ProgressBar progressBarHunger;
    int points;
    int reachable_points;
    long time;
    int level;
    MediaPlayer music, dragDrop, crying, relief, sneezing, angry;
    int length;

    ImageButton mailButton;
    Button tradingButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_larry_page);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            processExtras(extras);

        music = MediaPlayer.create(LarryPageActivity.this, R.raw.music);
        music.setLooping(true);
        music.setVolume(0.3f, 0.3f);
        music.start();
        water = (ImageView) findViewById(R.id.water);
        pills = (ImageView) findViewById(R.id.pills);
        food = (ImageView) findViewById(R.id.food);
        book = (ImageView) findViewById(R.id.book);
        toy = (ImageView) findViewById(R.id.toy);
        coins = (ImageView) findViewById(R.id.coins);
        water.setImageResource(R.drawable.water);
        water.setScaleY(1.3f);
        water.setScaleX(1.3f);
        pills.setImageResource(R.drawable.pills2);
        food.setImageResource(R.drawable.hamburger);
        book.setImageResource(R.drawable.book2);
        book.setScaleY(1f);
        book.setScaleX(1f);
        toy.setImageResource(R.drawable.toy2);
        toy.setScaleY(1.1f);
        toy.setScaleX(1.1f);
        myImageView = (ImageView) findViewById(R.id.my_image_view);
//        myImageView.setX(10);
//        myImageView.setY(400);
        myImageView.setX(20);
        myImageView.setY(400);
        coins.setImageResource(R.drawable.coin);
        textView_water = findViewById(R.id.water_status);
        //textView_water.setVisibility(View.INVISIBLE);
        textView_pills = findViewById(R.id.pills_status);
        //textView_pills.setVisibility(View.INVISIBLE);
        textView_pizza = findViewById(R.id.pizza_status);
        //textView_pizza.setVisibility(View.INVISIBLE);
        textView_book = findViewById(R.id.book_status);
        //textView_book.setVisibility(View.INVISIBLE);
        textView_toy = findViewById(R.id.toy_status);
        //textView_toy.setVisibility(View.INVISIBLE);
        textView_coins = findViewById(R.id.coins_status);
        //textView_coins.setVisibility(View.INVISIBLE);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBarMood = findViewById(R.id.progressBarMood);
        progressBarHunger = findViewById(R.id.progressBarHunger);
        progressBar.setVisibility(View.VISIBLE);
        progressBarMood.setVisibility(View.VISIBLE);
        progressBarHunger.setVisibility(View.VISIBLE);
        setParametersLarri(progressBar, progressBarMood, progressBarHunger);
        addUserNicknameToWelcomeText();
        crying = MediaPlayer.create(LarryPageActivity.this, R.raw.crying);
        relief = MediaPlayer.create(LarryPageActivity.this, R.raw.relief);
        sneezing = MediaPlayer.create(LarryPageActivity.this, R.raw.sneezing);
        angry = MediaPlayer.create(LarryPageActivity.this, R.raw.angry);
        MenuBarHelper.setup(this, LarryPageActivity.this, R.id.menuToLarri);
        getValues(textView_water, textView_pills, textView_pizza, textView_book, textView_toy, textView_coins);

        try {
            updateTimeAndBars();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                updateImage();
//            }
//        }, 5000);
        //
        //
        //
        // updateImage();

        ChoiceTouchListener choiceTouchListener1 = new ChoiceTouchListener();
        ChoiceTouchListener choiceTouchListener2 = new ChoiceTouchListener();
        ChoiceTouchListener choiceTouchListener3 = new ChoiceTouchListener();
        ChoiceTouchListener choiceTouchListener4 = new ChoiceTouchListener();
        ChoiceTouchListener choiceTouchListener5 = new ChoiceTouchListener();
        ChoiceTouchListener choiceTouchListener6 = new ChoiceTouchListener();
        water.setOnTouchListener(choiceTouchListener1);
        pills.setOnTouchListener(choiceTouchListener2);
        food.setOnTouchListener(choiceTouchListener3);
        book.setOnTouchListener(choiceTouchListener4);
        toy.setOnTouchListener(choiceTouchListener5);
        coins.setOnTouchListener(choiceTouchListener6);

//        Runnable swapIm = updateImages();
//        myImageView.postDelayed(swapIm, 10);
        //pop_up = (FloatingActionButton) findViewById(R.id.floatingPopUp);
        // get Points
        getPoints();
        myImageView.setOnDragListener((v, event) -> {
            int dragEvent = event.getAction();
            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DROP:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    if (progressBarHunger.getProgress() != 100) {
                        if (choiceTouchListener1.check == 1) {
                            choiceTouchListener1.check = 0;
                            if (Integer.parseInt(textView_water.getText().toString()) != 0) {
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                progressBarHunger.setProgress(progressBarHunger.getProgress() + 7);
                                points += 5;
                                try {
                                    updateValues(textView_water);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (progressBar.getProgress() != 100) {
                        if (choiceTouchListener2.check == 1) {
                            choiceTouchListener2.check = 0;
                            if (Integer.parseInt(textView_pills.getText().toString()) != 0) {
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                progressBar.setProgress(progressBar.getProgress() + 7);
                                points += 5;
                                try {
                                    updateValues(textView_pills);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (progressBarHunger.getProgress() != 100) {
                        if (choiceTouchListener3.check == 1) {
                            choiceTouchListener3.check = 0;

                            if (Integer.parseInt(textView_pizza.getText().toString()) != 0) {
                                progressBarHunger.setProgress(progressBarHunger.getProgress() + 7);
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                points += 5;
                                try {
                                    updateValues(textView_pizza);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (progressBarMood.getProgress() != 100) {
                        if (choiceTouchListener4.check == 1) {
                            choiceTouchListener4.check = 0;

                            if (Integer.parseInt(textView_book.getText().toString()) != 0) {
                                progressBarMood.setProgress(progressBarMood.getProgress() + 7);
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                points += 5;
                                try {
                                    updateValues(textView_book);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (progressBarMood.getProgress() != 100) {
                        if (choiceTouchListener5.check == 1) {
                            choiceTouchListener5.check = 0;

                            if (Integer.parseInt(textView_toy.getText().toString()) != 0) {
                                progressBarMood.setProgress(progressBarMood.getProgress() + 7);
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                points += 5;
                                try {
                                    updateValues(textView_toy);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (progressBarMood.getProgress() != 100) {
                        if (choiceTouchListener6.check == 1) {
                            choiceTouchListener6.check = 0;

                            if (Integer.parseInt(textView_coins.getText().toString()) != 0) {
                                progressBarMood.setProgress(progressBarMood.getProgress() + 7);
                                dragDrop = MediaPlayer.create(LarryPageActivity.this, R.raw.zapsplat_multimedia_game_sound_click_selection_menu_001_73511);
                                dragDrop.start();
                                points += 5;
                                try {
                                    updateValues(textView_coins);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    try {
                        updateResources();
                        updateLarri(progressBar, progressBarMood, progressBarHunger);
                        increasePoints();
                        updateImage(level);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                default:
                    return false;
            }
            return true;
        });


        /*
        pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop_up_open = !pop_up_open;
                if(pop_up_open) {
                    water.setVisibility(View.VISIBLE);
                    pills.setVisibility(View.VISIBLE);
                    food.setVisibility(View.VISIBLE);
                    book.setVisibility(View.VISIBLE);
                    toy.setVisibility(View.VISIBLE);
                    coins.setVisibility(View.VISIBLE);
                    textView_water.setVisibility(View.VISIBLE);
                    textView_pills.setVisibility(View.VISIBLE);
                    textView_pizza.setVisibility(View.VISIBLE);
                    textView_book.setVisibility(View.VISIBLE);
                    textView_toy.setVisibility(View.VISIBLE);
                    textView_coins.setVisibility(View.VISIBLE);
                }
                else{
                    water.setVisibility(View.INVISIBLE);
                    pills.setVisibility(View.INVISIBLE);
                    food.setVisibility(View.INVISIBLE);
                    book.setVisibility(View.INVISIBLE);
                    toy.setVisibility(View.INVISIBLE);
                    coins.setVisibility(View.INVISIBLE);
                    textView_water.setVisibility(View.INVISIBLE);
                    textView_pills.setVisibility(View.INVISIBLE);
                    textView_pizza.setVisibility(View.INVISIBLE);
                    textView_book.setVisibility(View.INVISIBLE);
                    textView_toy.setVisibility(View.INVISIBLE);
                    textView_coins.setVisibility(View.INVISIBLE);
                }
            }
        });

         */


        ConstraintLayout constraintLayout = findViewById(R.id.larriPageLayout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        mailButton = findViewById(R.id.friendRequestsButton);
        mailButton.setOnClickListener(v -> startActivity(new Intent(this, FriendRequestsActivity.class)));
    }

    private void updateTimeAndBars() throws JSONException {
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    try {
                        time = response.getLong("time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "updateTimeAndBars: Did not get time from backend!", error)
        );
        if (time == 0) {
            return;
        }
        long current_time = System.currentTimeMillis();
        long difference = current_time - time;
        long diverse = 1000 * 60 * 60;
        difference = difference / diverse;
        System.out.println("current_time " + current_time);
        System.out.println("time " + time);
        System.out.println("difference " + (current_time - time));
        System.out.println(difference + " fsdsdsdsdsdsdsdsdsdsd");
        if (difference >= 1) {
            progressBar.setProgress(progressBar.getProgress() - (int) difference * 5);
            progressBarMood.setProgress(progressBarMood.getProgress() - (int) difference * 4);
            progressBarHunger.setProgress(progressBarHunger.getProgress() - (int) difference * 6);
            updateLarri(progressBar, progressBarHunger, progressBarMood);
            JSONObject playerJSON = new JSONObject();
            playerJSON.put("time", current_time);

            playerVolleyHelper.updatePlayer(firebaseAuth.getCurrentUser().getUid(), playerJSON,
                    response -> Log.d(TAG, "time  updated!"),
                    error -> Log.e(TAG, "time not updated!", error));
        }

    }

    private static final class ChoiceTouchListener implements View.OnTouchListener {
        private int check = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((event.getAction() == MotionEvent.ACTION_DOWN) && ((ImageView) v).getDrawable() != null) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                check = 1;
                v.startDragAndDrop(data, shadowBuilder, v, 0);
                return true;

            } else {
                return false;
            }
        }
    }

    private void processExtras(@NonNull Bundle extras) {// TODO: extract
        String tradingID = extras.getString("tradingID");
        Log.d(TAG, "tradingID: " + tradingID);
        if (tradingID != null)
            TradingDialogActivity.showDialog(LarryPageActivity.this, tradingID, extras.getString("requesterName"));
    }

    private void addUserNicknameToWelcomeText() {
        TextView welcomeText = findViewById(R.id.name);
        welcomeText.setText("");
        TextView levelText = findViewById(R.id.game_score);
        levelText.setText("");
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        welcomeText.setText(welcomeText.getText() + " " + response.getString("nickname"));
                        levelText.setText(levelText.getText() + response.getString("level"));
                        if (levelText.getText() != "") {
                            level = Integer.parseInt(levelText.getText().toString());
                            System.out.println("LEVEL ADD USER: " + level);
                            updateImage(level);
                            //level_text.setText(levelText.getText());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "addUserNicknameToLarryPage: Did not get nickname from backend!", error)
        );
    }

    private void setParametersLarri(ProgressBar health, ProgressBar mood, ProgressBar hunger) {
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        health.setProgress(response.getInt("health"));
                        mood.setProgress(response.getInt("mood"));
                        hunger.setProgress(response.getInt("hunger"));
                        changeColor(health, mood, hunger);
                        level = response.getInt("level");
                        updateImage(level);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "setParameterLarri: Did not get parameters from backend!", error)
        );
        //updateImage();
    }

    private void updateResources() throws JSONException {

        JSONObject playerJSON = new JSONObject();
        if (textView_pills.getText().toString() != "") {
            playerJSON.put("pills", Integer.parseInt(textView_pills.getText().toString()));
        }
        if (textView_water.getText().toString() != "") {
            playerJSON.put("food", Integer.parseInt(textView_water.getText().toString()));
        }

        if (textView_pizza.getText().toString() != "") {
            playerJSON.put("pizza", Integer.parseInt(textView_pizza.getText().toString()));
        }

        if (textView_book.getText().toString() != "") {
            playerJSON.put("book", Integer.parseInt(textView_book.getText().toString()));
        }
        if (textView_toy.getText().toString() != "") {
            playerJSON.put("toy", Integer.parseInt(textView_toy.getText().toString()));
        }
        if (textView_coins.getText().toString() != "") {
            playerJSON.put("coins", Integer.parseInt(textView_coins.getText().toString()));
        }
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.updatePlayer(firebaseAuth.getCurrentUser().getUid(), playerJSON,
                response -> Log.d(TAG, "resources  updated!"),
                error -> Log.e(TAG, "resources not updated!", error));

        changeColor(progressBar, progressBarMood, progressBarHunger);
    }

    private void updateLarri(ProgressBar health, ProgressBar mood, ProgressBar hunger) throws
            JSONException {
        JSONObject playerJSON = new JSONObject();
        playerJSON.put("health", health.getProgress());
        playerJSON.put("mood", mood.getProgress());
        playerJSON.put("hunger", hunger.getProgress());

        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.updatePlayer(firebaseAuth.getCurrentUser().getUid(), playerJSON,
                response -> Log.d(TAG, "score  updated!"),
                error -> Log.e(TAG, "score not updated!", error));

        changeColor(progressBar, progressBarMood, progressBarHunger);

    }

    private void changeColor(ProgressBar health, ProgressBar mood, ProgressBar hunger) {
        if (health.getProgress() < 40) {
            health.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (hunger.getProgress() < 40) {
            hunger.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (mood.getProgress() < 40) {

            mood.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (health.getProgress() > 40) {

            health.getProgressDrawable().setColorFilter(
                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (hunger.getProgress() > 40) {
            hunger.getProgressDrawable().setColorFilter(
                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (mood.getProgress() > 40) {
            mood.getProgressDrawable().setColorFilter(
                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        }


    }

    private void getValues(TextView water_status, TextView pills_status, TextView pizza_status, TextView textView_book, TextView textView_toy, TextView textView_coins) {
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        water_status.setText(response.getString("food"));
                        pills_status.setText(response.getString("pills"));
                        pizza_status.setText(response.getString("pizza"));
                        textView_book.setText(response.getString("book"));
                        textView_toy.setText(response.getString("toy"));
                        textView_coins.setText(response.getString("coins"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "getValues: Did not get resources from backend!", error)
        );
    }

    private void updateImage(int level) {

        System.out.println("LEVEL: " + level);
        int level1 =  level;
        if (level1 < 2) {
            if (progressBar.getProgress() < 30) {
                myImageView.setImageResource(R.drawable.klein_dead3);
                if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    sneezing.start();
                }
            } else if (progressBar.getProgress() >= 30) {
                if ((progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() >= 50)) {
                    myImageView.setImageResource(R.drawable.laerri_klein);
                    crying.stop();
                    relief.start();
                } else if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    myImageView.setImageResource(R.drawable.little_angry);
                    angry.start();
                } else if (progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() < 50) {
                    crying.start();
                    myImageView.setImageResource(R.drawable.little_sad);
                } else if (progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() >= 50) {
                    myImageView.setImageResource(R.drawable.little_angry);
                    angry.start();
                }
            }
        }

        if (level1 >= 2 && level1 < 4) {
            if (progressBar.getProgress() < 30) {
                myImageView.setImageResource(R.drawable.klein_dead);
                if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    sneezing.start();
                }
            } else if (progressBar.getProgress() >= 30) {
                if ((progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() >= 50)) {
                    myImageView.setImageResource(R.drawable.larry_top_happy);
                    crying.stop();
                    relief.start();
                } else if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    myImageView.setImageResource(R.drawable.klein_top_angry);
                    angry.start();
                } else if (progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() < 50) {
                    myImageView.setImageResource(R.drawable.klein_top_sad);
                    crying.start();
                } else if (progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() >= 50) {
                    myImageView.setImageResource(R.drawable.klein_top_angry);
                    angry.start();
                }
            }
        }
        if (level1 >= 4) {
            if (progressBar.getProgress() < 30) {
                myImageView.setImageResource(R.drawable.laerri_dead);
                if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    sneezing.start();
                }
            } else if (progressBar.getProgress() >= 20) {
                if ((progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() >= 50)) {
                    myImageView.setImageResource(R.drawable.laerri_happy);
                    crying.stop();
                    relief.start();
                } else if ((progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() < 50)) {
                    myImageView.setImageResource(R.drawable.laerri_angry);
                    angry.start();
                } else if (progressBarHunger.getProgress() >= 50 && progressBarMood.getProgress() < 50) {
                    myImageView.setImageResource(R.drawable.laerri_sad);
                    crying.start();
                } else if (progressBarHunger.getProgress() < 50 && progressBarMood.getProgress() >= 50) {
                    myImageView.setImageResource(R.drawable.laerri_angry);
                    angry.start();
                }
            }
        }

    }

    private void updateValues(TextView textView) throws JSONException {
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        if (textView.equals(textView_water)) {
                            int value = response.getInt("food");
                            value--;
                            textView_water.setText(String.valueOf(value));
                        }
                        if (textView.equals(textView_pills)) {
                            int value = response.getInt("pills");
                            value--;
                            textView_pills.setText(String.valueOf(value));
                        }
                        if (textView.equals(textView_pizza)) {
                            int value = response.getInt("pizza");
                            value--;
                            textView_pizza.setText(String.valueOf(value));
                        }
                        if (textView.equals(textView_book)) {
                            int value = response.getInt("book");
                            value--;
                            textView_book.setText(String.valueOf(value));
                        }
                        if (textView.equals(textView_toy)) {
                            int value = response.getInt("toy");
                            value--;
                            textView_toy.setText(String.valueOf(value));
                        }
                        if (textView.equals(textView_coins)) {
                            int value = response.getInt("coins");
                            value--;
                            textView_coins.setText(String.valueOf(value));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "updateValues: Did not get resources from backend!", error)
        );
    }


    private void getPoints() {

        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    try {
                        points = response.getInt("points");
                        reachable_points = response.getInt("reachable_points");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "getPoints: Did not get nickname from backend!", error)
        );

    }


    private void increasePoints() throws JSONException {
        JSONObject playerJSON = new JSONObject();
        playerJSON.put("points", points);
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.updatePlayer(firebaseAuth.getCurrentUser().getUid(), playerJSON,
                response -> Log.d(TAG, "points  updated!"),
                error -> Log.e(TAG, "points not updated!", error));

        if (points >= reachable_points) {
            updateLevel();
        }


    }

    private void updateLevel() throws JSONException {
        TextView levelText = findViewById(R.id.game_score);
        level++;
        JSONObject playerJSON = new JSONObject();
        playerJSON.put("points", 0);
        playerJSON.put("reachable_points", reachable_points * 2);
        playerJSON.put("level", level);
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.updatePlayer(firebaseAuth.getCurrentUser().getUid(), playerJSON,
                response -> Log.d(TAG, "level  updated!"),
                error -> Log.e(TAG, "level not updated!", error));

        //getPoints();
        points = 0;
        reachable_points *= 2;
        levelText.setText("" + level);

    }

    @Override
    public void onBackPressed() {
        Util.logoutUser(firebaseAuth, LarryPageActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        music.pause();
        crying.pause();
        relief.pause();
        angry.pause();
        length = music.getCurrentPosition();
    }

}