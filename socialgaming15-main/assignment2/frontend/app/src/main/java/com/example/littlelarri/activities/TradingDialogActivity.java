package com.example.littlelarri.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.littlelarri.R;
import com.example.littlelarri.trading.Trading;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TradingDialogActivity extends Activity {
    private static final String TAG = "TradingDialogActivity";

    private String tradingID;
    private String requesterName;

    private DatabaseReference tradeDBReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading_dialog_activity);

        // Get data from extras
        Bundle extras = getIntent().getExtras();
        tradingID = extras.getString("tradingID");
        requesterName = extras.getString("requesterName");

        // Add the name of the requester to the text field
        TextView textView = (TextView)findViewById(R.id.message);
        textView.setText(String.format(textView.getText().toString(), requesterName));
        // Setup buttons
        findViewById(R.id.acceptButton).setOnClickListener((v) -> accept());
        findViewById(R.id.rejectButton).setOnClickListener((v) -> reject());

        // Setup database references
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.firebase_realtime_database_url));
        tradeDBReference = firebaseDatabase.getReference().child("trading").child(tradingID);

        // Prevent user from closing the dialog by clicking outside of dialog
        setFinishOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        reject();
        super.onBackPressed();
    }

    private void accept() {
        finish();
        tradeDBReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_ACCEPTED);

        QRcodeActivity.start(TradingDialogActivity.this, false, requesterName, tradeDBReference.getKey());
    }

    private void reject() {
        tradeDBReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_REJECTED);
        finish();
    }

    public static void showDialog(Context context, String tradingID, String requesterName) {
        Intent intent = new Intent(context, TradingDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("tradingID", tradingID);
        bundle.putString("requesterName", requesterName);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
