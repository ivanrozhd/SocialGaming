package com.example.littlelarri.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.littlelarri.helpers.MenuBarHelper;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.helpers.Util;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {
    private static final String TAG = "InventoryActivity";

    static final String info_water = "Give your Lärri regularly water, so he doesn't die of thirst! It improves your hunger status " +
            "and earns you valuable points to increase your level.";

    static final String info_medicine = "If your Lärri is sick, you must take care of him and give him his medicine. It improves your " +
            "health status and earns you valuable points to increase your level. Make sure you never run out of it!";

    static final String info_food = "Your Lärri loves to eat, so don't let him go hungry and feed him regularly! It improves your " +
            "food status and earns you valuable points to increase your level.";

    static final String info_books = "Lärri needs school and education. He gets these by reading books, so make sure that he doesn't go dumb! It improves your " +
            "mood status and earns you valuable points to increase your level.";

    static final String info_toys = "Everyone has to have a little fun, including your Lärri. Make him happy and give him his favorite toy! It improves your " +
            "mood status and earns you valuable points to increase your level.";

    static final String info_coins = "Money rules the world. So don't waste it all while trading and rather give it to your Lärri! It improves your " +
            "mood status and earns you valuable points to increase your level.";

    FirebaseAuth firebaseAuth;
    ImageView water, pills, food,book, toy,coins;
    static TextView infotext;
    static ImageView info;
    static ImageButton cancelButton;
    TextView textView_water, textView_pills, textView_food, textView_book, textView_toy, textView_coins;
    ArrayList<ImageView> frames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
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
        coins.setImageResource(R.drawable.coin);
        info = findViewById(R.id.InformationBackground);
        info.setVisibility(View.INVISIBLE);
        infotext = findViewById(R.id.infotext);
        infotext.setVisibility(View.INVISIBLE);
        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.INVISIBLE);
        textView_water = findViewById(R.id.water_status);
        textView_water.setVisibility(View.VISIBLE);
        textView_pills = findViewById(R.id.pills_status);
        textView_pills.setVisibility(View.VISIBLE);
        textView_food = findViewById(R.id.pizza_status);
        textView_food.setVisibility(View.VISIBLE);
        textView_book = findViewById(R.id.book_status);
        textView_book.setVisibility(View.VISIBLE);
        textView_toy = findViewById(R.id.toy_status);
        textView_toy.setVisibility(View.VISIBLE);
        textView_coins = findViewById(R.id.coins_status);
        textView_coins.setVisibility(View.VISIBLE);
        for (ImageView frame : frames) {
            frame = (ImageView) findViewById(R.id.frame);
            frame.setImageResource(R.drawable.mini_frame1);
        }
        MenuBarHelper.setup(this, InventoryActivity.this, R.id.menuToInventory);
        firebaseAuth = FirebaseAuth.getInstance();
        getValues(textView_water,textView_pills,textView_food,textView_book,textView_toy,textView_coins);


        ChoiceTouchListener choiceTouchListener1 = new ChoiceTouchListener(1);
        ChoiceTouchListener choiceTouchListener2 = new ChoiceTouchListener(2);
        ChoiceTouchListener choiceTouchListener3 = new ChoiceTouchListener(3);
        ChoiceTouchListener choiceTouchListener4 = new ChoiceTouchListener(4);
        ChoiceTouchListener choiceTouchListener5 = new ChoiceTouchListener(5);
        ChoiceTouchListener choiceTouchListener6 = new ChoiceTouchListener(6);
        ChoiceTouchListener choiceTouchListener7 = new ChoiceTouchListener(7);
        water.setOnTouchListener(choiceTouchListener1);
        pills.setOnTouchListener(choiceTouchListener2);
        food.setOnTouchListener(choiceTouchListener3);
        book.setOnTouchListener(choiceTouchListener4);
        toy.setOnTouchListener(choiceTouchListener5);
        coins.setOnTouchListener(choiceTouchListener6);
        cancelButton.setOnTouchListener(choiceTouchListener7);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info.setVisibility(View.INVISIBLE);
                infotext.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
            }
        });

    }



    private static final class ChoiceTouchListener implements  View.OnTouchListener {
        private int check;
        ChoiceTouchListener(int check) {
            this.check = check;

        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if((event.getAction() == MotionEvent.ACTION_DOWN) && ((ImageView)v).getDrawable() != null) {
                ClipData data = ClipData.newPlainText("","");
                switch (check) {
                    case 1: info.setVisibility(View.VISIBLE);
                            infotext.setVisibility(View.VISIBLE);
                            cancelButton.setVisibility(View.VISIBLE);
                            infotext.setText(info_water);
                        System.out.println(check);
                            break;
                    case 2: info.setVisibility(View.VISIBLE);
                        infotext.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        infotext.setText(info_medicine);
                        System.out.println(check);
                        break;
                    case 3: info.setVisibility(View.VISIBLE);
                        infotext.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        infotext.setText(info_food);
                        System.out.println(check);
                        break;
                    case 4: info.setVisibility(View.VISIBLE);
                        infotext.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        infotext.setText(info_books);
                        System.out.println(check);
                        break;
                    case 5: info.setVisibility(View.VISIBLE);
                        infotext.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        infotext.setText(info_toys);
                        System.out.println(check);
                        break;

                    case 6: info.setVisibility(View.VISIBLE);
                        infotext.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        infotext.setText(info_coins);
                        System.out.println(check);
                        break;
                    default: break;
                }
                return true;

            }
            else{
                return false;
            }
        }
    }


    private void getValues(TextView water_status, TextView pills_status, TextView pizza_status,TextView textView_book, TextView textView_toy, TextView textView_coins) {
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


    @Override
    public void onBackPressed() {
        Util.logoutUser(firebaseAuth, InventoryActivity.this);
    }
}