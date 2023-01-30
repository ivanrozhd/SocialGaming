package com.example.littlelarri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littlelarri.Player;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.trading.Trading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TradingActivity extends AppCompatActivity {
    private static final String TAG = "TradingActivity";
    private PlayerVolleyHelper playerVolleyHelper;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference tradingDBReference;
    private DatabaseReference actionDBReference;
    private DatabaseReference currentUserDBReference;
    private DatabaseReference otherUserDBReference;
    private ActionChangedEventListener actionChangedEventListener;
    private ItemChangedEventListener itemChangedEventListener;
    private ReadyStateChangedEventListener readyStateChangedEventListener;

    private String otherUserName;
    private boolean isRequester;
    private boolean areFriends;

    private boolean currentPlayerReady;
    private boolean otherPlayerReady;

    private View partnerOfferBox;
    private View myOfferBox;

    private Button cancelButton;
    private ImageButton partnerOfferButton;
    private ImageButton acceptOfferButton;

    // Order of the arrays: Water, Medicine, Food, Book, Toy, Coins

    private ImageView[] partnerOfferImages;
    private TextView[] partnerOfferAmounts;

    private ImageView[] myOfferImages;
    private TextView[] myOfferAmounts;

    private ImageView[] resourceImages;
    private TextView[] resourceAmounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading);

        playerVolleyHelper = new PlayerVolleyHelper(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Setup database updates
        Bundle extras = getIntent().getExtras();
        isRequester = extras.getBoolean("isRequester");
        tradingDBReference = FirebaseDatabase.getInstance(getResources().getString(R.string.firebase_realtime_database_url))
                .getReference().child("trading").child(extras.getString("tradingID"));
        actionDBReference = tradingDBReference.child(Trading.ACTION_KEY);
        currentUserDBReference = tradingDBReference.child(isRequester ? "requester" : "requested");
        otherUserDBReference = tradingDBReference.child(isRequester ? "requested" : "requester");
        registerEventListeners();

        // Get nickname of other user from extras and display it
        otherUserName = extras.getString("otherUserName");
        ((TextView)findViewById(R.id.nicknameTrader)).setText(otherUserName);

        partnerOfferBox = findViewById(R.id.otherOffer);
        myOfferBox = findViewById(R.id.myOffer);

        cancelButton = findViewById(R.id.cancelNegotiationButton);
        partnerOfferButton = findViewById(R.id.partnerOfferButton);
        acceptOfferButton = findViewById(R.id.acceptOfferButton);

        currentPlayerReady = false;
        otherPlayerReady = false;
        updateStateOfCurrentUserBox();
        updateStateOfPartnerBox();

        partnerOfferImages = new ImageView[6];
        partnerOfferImages[0] = findViewById(R.id.waterPartnerOfferImage);
        partnerOfferImages[1] = findViewById(R.id.medicinePartnerOfferImage);
        partnerOfferImages[2] = findViewById(R.id.foodPartnerOfferImage);
        partnerOfferImages[3] = findViewById(R.id.bookPartnerOfferImage);
        partnerOfferImages[4] = findViewById(R.id.toyPartnerOfferImage);
        partnerOfferImages[5] = findViewById(R.id.coinPartnerOfferImage);

        partnerOfferAmounts = new TextView[6];
        partnerOfferAmounts[0] = findViewById(R.id.waterPartnerOfferAmount);
        partnerOfferAmounts[1] = findViewById(R.id.medicinePartnerOfferAmount);
        partnerOfferAmounts[2] = findViewById(R.id.foodPartnerOfferAmount);
        partnerOfferAmounts[3] = findViewById(R.id.bookPartnerOfferAmount);
        partnerOfferAmounts[4] = findViewById(R.id.toyPartnerOfferAmount);
        partnerOfferAmounts[5] = findViewById(R.id.coinPartnerOfferAmount);

        myOfferImages = new ImageView[6];
        myOfferImages[0] = findViewById(R.id.waterOfferImage);
        myOfferImages[1] = findViewById(R.id.medicineOfferImage);
        myOfferImages[2] = findViewById(R.id.foodOfferImage);
        myOfferImages[3] = findViewById(R.id.bookOfferImage);
        myOfferImages[4] = findViewById(R.id.toyOfferImage);
        myOfferImages[5] = findViewById(R.id.coinOfferImage);

        myOfferAmounts = new TextView[6];
        myOfferAmounts[0] = findViewById(R.id.waterOfferAmount);
        myOfferAmounts[1] = findViewById(R.id.medicineOfferAmount);
        myOfferAmounts[2] = findViewById(R.id.foodOfferAmount);
        myOfferAmounts[3] = findViewById(R.id.bookOfferAmount);
        myOfferAmounts[4] = findViewById(R.id.toyOfferAmount);
        myOfferAmounts[5] = findViewById(R.id.coinOfferAmount);

        resourceImages = new ImageView[6];
        resourceImages[0] = findViewById(R.id.waterRessourceImage);
        resourceImages[1] = findViewById(R.id.medicineRessourceImage);
        resourceImages[2] = findViewById(R.id.foodRessourceImage);
        resourceImages[3] = findViewById(R.id.bookRessourceImage);
        resourceImages[4] = findViewById(R.id.toyRessourceImage);
        resourceImages[5] = findViewById(R.id.coinRessourceImage);

        resourceAmounts = new TextView[6];
        resourceAmounts[0] = findViewById(R.id.waterRessourceAmount);
        resourceAmounts[1] = findViewById(R.id.medicineRessourceAmount);
        resourceAmounts[2] = findViewById(R.id.foodRessourceAmount);
        resourceAmounts[3] = findViewById(R.id.bookRessourceAmount);
        resourceAmounts[4] = findViewById(R.id.toyRessourceAmount);
        resourceAmounts[5] = findViewById(R.id.coinRessourceAmount);

        cancelButton.setOnClickListener(v -> clickCancel());
        partnerOfferButton.setOnClickListener(v -> clickUnclickable());
        acceptOfferButton.setOnClickListener(v -> setCurrentPlayerReady(!currentPlayerReady));

        for (int i = 0; i < 6; i++) {
            applyColorFilter(partnerOfferImages[i]);
            applyColorFilter(myOfferImages[i]);

            partnerOfferImages[i].setOnClickListener(v -> clickUnclickable());

            int index = i; // needed bc variable i can't be used in lambda (don't know why)  I can explain it to you (Jonas)

            // TODO: Using onTouchListener instead of onClickListener made somehow no difference -> maybe only in emulator
            resourceImages[i].setOnClickListener(v -> {
                offerResource(index);
                setCurrentPlayerReady(false);
                updateOfferInDB(index);
            });

            myOfferImages[i].setOnClickListener(v -> {
                takebackResource(index);
                setCurrentPlayerReady(false);
                updateOfferInDB(index);
            });
        }

        fillResourceAmounts();

        areFriends(friends -> {
            Log.d(TAG, "The users are friends " + (friends.booleanValue() ? "" : "not") + " friends.");
            this.areFriends = friends;
        });
    }

    private void registerEventListeners() {
        actionChangedEventListener = new ActionChangedEventListener();
        itemChangedEventListener = new ItemChangedEventListener();
        readyStateChangedEventListener = new ReadyStateChangedEventListener();
        actionDBReference.addValueEventListener(actionChangedEventListener);
        otherUserDBReference.child("items").addValueEventListener(itemChangedEventListener);
        otherUserDBReference.child(Trading.KEY_ACCEPT_OFFER).addValueEventListener(readyStateChangedEventListener);
    }

    private void unregisterEventListeners() {
        actionDBReference.removeEventListener(actionChangedEventListener);
        otherUserDBReference.child("items").removeEventListener(itemChangedEventListener);
        otherUserDBReference.child(Trading.KEY_ACCEPT_OFFER).removeEventListener(readyStateChangedEventListener);
    }

    public void onError() {
        Log.e(TAG, "An error occurred int the while trading");
        Trading.showTradeErrorAlert(TradingActivity.this, null);
        closeActivity();
    }

    public void onOtherUserCanceled() {
        unregisterEventListeners();
        Trading.showTradeCanceledAlert(TradingActivity.this, (dialog, which) -> startLarryPage(), otherUserName);
    }

    private void clickCancel() {
        closeActivity();
        actionDBReference.setValue(Trading.ACTION_CANCELED);
    }

    private void closeActivity() {
        unregisterEventListeners();
        startLarryPage();
    }

    private void startLarryPage() {
        startActivity(new Intent(this, LarryPageActivity.class));
    }

    @Override
    public void onBackPressed() {
        clickCancel();
    }

    // player can't click any button or image in upper box
    private void clickUnclickable() {
        Toast.makeText(TradingActivity.this, "not clickable for you", Toast.LENGTH_SHORT).show();
    }

    // transfers one resource at an index from the resource section to the offer section
    private void offerResource(int index) {
        int amount = getAmountOfTextview(resourceAmounts[index]);
        if (amount == 0)
            return;
        else if (amount == 1)
            applyColorFilter(resourceImages[index]);
        amount--;
        resourceAmounts[index].setText("" + amount);

        amount = getAmountOfTextview(myOfferAmounts[index]);
        amount++;
        myOfferAmounts[index].setText("" + amount);
        removeColorFilter(myOfferImages[index]);
    }

    // transfers one resource at an index from the offer section to the resource section
    private void takebackResource(int index) {
        int amount = getAmountOfTextview(myOfferAmounts[index]);
        if (amount == 0)
            return;
        else if(amount == 1)
            applyColorFilter(myOfferImages[index]);
        amount--;
        myOfferAmounts[index].setText("" + amount);

        amount = getAmountOfTextview(resourceAmounts[index]);
        amount++;
        resourceAmounts[index].setText("" + amount);
        removeColorFilter(resourceImages[index]);
    }

    // returns the number that is written in a textview
    private int getAmountOfTextview(TextView textView) {
        int result = -1;
        try {
            result = Integer.parseInt(textView.getText().toString());
        } catch(NumberFormatException e) {
            Log.e(TAG, "Couldn't parse Textview because text was " + textView.getText().toString());
        }
        return result;
    }

    private void applyColorFilter(ImageView image) {
        image.setColorFilter(Color.argb(150, 200, 200, 200));
    }

    private void removeColorFilter(ImageView image) {
        image.setColorFilter(null);
    }

    public void setCurrentPlayerReady(boolean currentPlayerReady) {
        boolean oldState = this.currentPlayerReady;
        this.currentPlayerReady = currentPlayerReady;
        if (oldState != this.currentPlayerReady) {
            updateStateOfCurrentUserBox();
            currentUserDBReference.child(Trading.KEY_ACCEPT_OFFER).setValue(currentPlayerReady);
        }
        checkAgreement();
    }

    public void setOtherPlayerReady(boolean otherPlayerReady) {
        boolean oldState = this.otherPlayerReady;
        this.otherPlayerReady = otherPlayerReady;
        if (oldState != this.otherPlayerReady)
            updateStateOfPartnerBox();

        checkAgreement();
    }

    // Check if both players have agreed to the trade
    private void checkAgreement() {
        if (!currentPlayerReady || !otherPlayerReady)
            return;
        // Items are exchanged on the server
        unregisterEventListeners();
        tradingDBReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_COMPLETED);
        showPopupSummary();
        /*
        new AlertDialog.Builder(TradingActivity.this)
                .setTitle(R.string.alert_title_trade_completed)
                .setMessage(R.string.alert_message_trade_completed)
                .setPositiveButton(R.string.alert_understood, (dialog, which) -> closeActivity())
                .create().show();

         */
    }

    private void updateStateOfCurrentUserBox() {
        if (currentPlayerReady)
            setBoxVisualsGreen(myOfferBox, acceptOfferButton);
        else
            setBoxVisualsRed(myOfferBox, acceptOfferButton);
    }

    private void updateStateOfPartnerBox() {
        if (otherPlayerReady)
            setBoxVisualsGreen(partnerOfferBox, partnerOfferButton);
        else
            setBoxVisualsRed(partnerOfferBox, partnerOfferButton);
    }

    private void setBoxVisualsGreen(View box, ImageButton button) {
        box.setBackgroundColor(getColor(R.color.green_background_happy_with_trade));
        button.setImageResource(R.drawable.face_happy);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_bg3_green));
    }

    private void setBoxVisualsRed(View box, ImageButton button) {
        box.setBackgroundColor(getColor(R.color.red_background_unhappy_with_trade));
        button.setImageResource(R.drawable.face_sad);
        button.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_bg3_red));
    }

    // gets all resource amounts from database and writes them into the resource textviews
    private void fillResourceAmounts() {
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    int food = -1;
                    int pills = -1;
                    int pizza = -1;
                    int books = -1;
                    int toys = -1;
                    int coins = -1;
                    try {
                        food = response.getInt("food");
                        pills = response.getInt("pills");
                        pizza = response.getInt("pizza");
                        books = response.getInt("book");
                        toys = response.getInt("toy");
                        coins = response.getInt("coins");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setResourceAmounts(new int[]{food, pills, pizza, books, toys, coins});
                },
                error -> Log.e(TAG, "Didn't get resources from backend in fillResourceAmounts()", error));
    }

    public void setPartnerOfferAmounts(int[] amounts) {
        setAmounts(amounts, partnerOfferAmounts);
    }

    private void setMyOfferAmounts(int[] amounts) {
        setAmounts(amounts, myOfferAmounts);
    }

    private void setResourceAmounts(int[] amounts) {
        setAmounts(amounts, resourceAmounts);
    }

    private void setAmounts(int[] amounts, TextView[] textViews) {
        for (int i = 0; i < 6; i++) {
            textViews[i].setText("" + amounts[i]);
        }
    }

    private void showPopupSummary() {
        Dialog summaryDialog = new Dialog(this);
        summaryDialog.setContentView(R.layout.trading_successful_popup_window);

        TextView nickname = summaryDialog.findViewById(R.id.nicknameTextView);

        // Textview array with first half offered resources (left side) and second half received resources (right side)
        TextView[] amounts = new TextView[12];
        amounts[0] = summaryDialog.findViewById(R.id.leftWaterAmount);
        amounts[1] = summaryDialog.findViewById(R.id.leftMedicineAmount);
        amounts[2] = summaryDialog.findViewById(R.id.leftFoodAmount);
        amounts[3] = summaryDialog.findViewById(R.id.leftBookAmount);
        amounts[4] = summaryDialog.findViewById(R.id.leftToyAmount);
        amounts[5] = summaryDialog.findViewById(R.id.leftCoinsAmount);
        amounts[6] = summaryDialog.findViewById(R.id.rightWaterAmount);
        amounts[7] = summaryDialog.findViewById(R.id.rightMedicineAmount);
        amounts[8] = summaryDialog.findViewById(R.id.rightFoodAmount);
        amounts[9] = summaryDialog.findViewById(R.id.rightBookAmount);
        amounts[10] = summaryDialog.findViewById(R.id.rightToyAmount);
        amounts[11] = summaryDialog.findViewById(R.id.rightCoinsAmount);

        // Imageview array with first half offered resources (left side) and second half received resources (right side)
        ImageView[] images = new ImageView[12];
        images[0] = summaryDialog.findViewById(R.id.leftWaterImage);
        images[1] = summaryDialog.findViewById(R.id.leftMedicineImage);
        images[2] = summaryDialog.findViewById(R.id.leftFoodImage);
        images[3] = summaryDialog.findViewById(R.id.leftBookImage);
        images[4] = summaryDialog.findViewById(R.id.leftToyImage);
        images[5] = summaryDialog.findViewById(R.id.leftCoinsImage);
        images[6] = summaryDialog.findViewById(R.id.rightWaterImage);
        images[7] = summaryDialog.findViewById(R.id.rightMedicineImage);
        images[8] = summaryDialog.findViewById(R.id.rightFoodImage);
        images[9] = summaryDialog.findViewById(R.id.rightBookImage);
        images[10] = summaryDialog.findViewById(R.id.rightToyImage);
        images[11] = summaryDialog.findViewById(R.id.rightCoinsImage);

        Button okButton = summaryDialog.findViewById(R.id.okButton);

        TextView bonusText1 = summaryDialog.findViewById(R.id.moodBonusText1);
        TextView bonusText2 = summaryDialog.findViewById(R.id.moodBonusText2);

        nickname.setText(otherUserName);

        // Display how much the mood increased
        int moodIncrease = Trading.TRADING_MOOD_INCREASE;
        int friendBonus = Trading.TRADING_FRIEND_BONUS;
        //boolean areFriends can be used to check if users are friends

        if (areFriends) {
            bonusText1.setText("+ " + moodIncrease + " + " + friendBonus);
            bonusText2.setText("(friendship bonus)\nextra mood!");
        } else {
            bonusText1.setText("+ " + moodIncrease);
            bonusText2.setText("extra mood!");
        }

        for (int i = 0; i < amounts.length; i++) {
            if (i < 6)
                amounts[i].setText(myOfferAmounts[i].getText().toString());
            else
                amounts[i].setText(partnerOfferAmounts[i - 6].getText().toString());
            // if the value is zero we want to grey picture out
            if (Integer.parseInt(amounts[i].getText().toString()) == 0) {
                applyColorFilter(images[i]);
            }
        }

        okButton.setOnClickListener(v -> {
            summaryDialog.dismiss();
            closeActivity();
        });

        summaryDialog.setCancelable(false);
        summaryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        summaryDialog.show();
    }

    // Tests if the two traders are friends
    // Returned by lambda because asynchronous
    // Returns false if an error occurred
    private void areFriends(Consumer<Boolean> consumer) {
        tradingDBReference.get().addOnCompleteListener(task -> {
            final String errorMessage = "showPopupSummary: could not retrieve weather the two users are friends!";
            if (!task.isSuccessful()) {
                Log.e(TAG, errorMessage);
                consumer.accept(false);
                return;
            }
            Map<String, Object> map = (Map<String, Object>) task.getResult().getValue();
            String requesterUID = (String)((Map<String, Object>)map.get("requester")).get("firebaseUID");
            String requestedUID = (String)((Map<String, Object>)map.get("requested")).get("firebaseUID");
            playerVolleyHelper.getPlayerByFirebaseUID(requesterUID, response -> {
                try {
                    JSONArray friendUIDs = (JSONArray) response.get("friendsFirebaseUIDs");
                    for (int i = 0; i < friendUIDs.length(); i++)
                        if (friendUIDs.getString(i).equals(requestedUID)) {
                            consumer.accept(true);
                            return;
                        }
                    consumer.accept(false);
                } catch (JSONException e) {
                    consumer.accept(false);
                    Log.e(TAG, errorMessage + ": " + e);
                }
            }, error ->  {
                consumer.accept(false);
                Log.e(TAG, errorMessage);
            });
        });
    }

    private void updateOfferInDB(int index) {
        int newAmount = getAmountOfTextview(myOfferAmounts[index]);
        currentUserDBReference.child("items").child(index + "").setValue(newAmount);
    }

    private class ActionChangedEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String action = (String)snapshot.getValue();
            if (action == null) {
                Log.w(TAG, "Trading action was null");
                return;
            }
            if (action.equals(Trading.ACTION_CANCELED))
                onOtherUserCanceled();
            else if (action.equals(Trading.ACTION_ERROR))
                onError();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            onError();
        }
    }

    private class ItemChangedEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.getValue() == null) {
                Log.e(TAG, "ItemData is null");
                return;
            }
            int[] amounts = new int[Trading.TRADING_ITEMS_AMOUNT];
            if (snapshot.getValue() instanceof List) {
                List<Long> items = (List<Long>) snapshot.getValue();
                int i = 0;
                for (Long amount : items) {
                    amounts[i] = amount == null ? 0 : amount.intValue();
                    i++;
                }
            }
            else if (snapshot.getValue() instanceof Map) {
                Map<String, Long> items = (Map<String, Long>) snapshot.getValue();
                for (int i = 0; i < Trading.TRADING_ITEMS_AMOUNT; i++) {
                    Long item = items.get(i + "");
                    if (item != null)
                        amounts[i] = item.intValue();
                }
            }
            else {
                Log.e(TAG, "onDataChange: Unexpected data format: " + snapshot.getValue().getClass());
                tradingDBReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_ERROR);
                onError();
            }
            setCurrentPlayerReady(false);
            setPartnerOfferAmounts(amounts);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            onError();
        }
    }

    private class ReadyStateChangedEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Boolean ready = (Boolean)snapshot.getValue();
            if (ready == null) {
                tradingDBReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_ERROR);
                onError();
            }
            else
                setOtherPlayerReady(ready);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            onError();
        }
    }
}