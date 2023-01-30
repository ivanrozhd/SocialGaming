package com.example.littlelarri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.littlelarri.trading.Trading;
import com.example.littlelarri.trading.TradingInitializerChildEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.example.littlelarri.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Map;

public class QRcodeActivity extends AppCompatActivity {
    private static final String TAG = "QRcodeActivity";
    private final int CAMERA_REQUEST_CODE = 101;

    private TextView statusText;
    private boolean isRequester;
    private String otherUserName;
    private String otherUserUID;
    private String tradingID;
    private DatabaseReference databaseReference;
    private TradingInitializerChildEventListener childEventListener;

    private CodeScannerView codeScannerView;
    private CodeScanner codeScanner;
    private ImageView qrOutput;
    private Button scanButton;

    private boolean wasAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        qrOutput = findViewById(R.id.qr_output);
        codeScannerView = findViewById(R.id.scanner_view);
        scanButton = findViewById(R.id.scanButton);

        generateCode();

        // Camera permission
        setUpPermission();
        // if camera permission was denied, the scan() method will not be executed
        scan();

        // Get extra information
        Bundle extras = getIntent().getExtras();
        otherUserName = extras.getString("otherNickname");
        isRequester = extras.getBoolean("isRequester");
        tradingID = extras.getString("tradingID");

        wasAccepted = !isRequester;

        // Initialize text
        TextView explanationText = findViewById(R.id.explanationText);
        explanationText.setText(String.format(explanationText.getText().toString(), otherUserName));

        statusText = findViewById(R.id.statusText);
        int statusString = isRequester ? R.string.qr_code_status_waiting : R.string.qr_code_status_accepted_you;
        statusText.setText(String.format(getResources().getString(statusString), otherUserName));

        // Setup button listeners
        scanButton.setOnClickListener(view -> toggleScannerAndCodeView());
        findViewById(R.id.cancelButton).setOnClickListener(view -> cancel());

        databaseReference = FirebaseDatabase.getInstance(getResources().getString(R.string.firebase_realtime_database_url)).getReference().child("trading").child(tradingID);
        // Get UID of otherUser
        databaseReference.child(isRequester ? "requested" : "requester").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, String> map = (Map<String, String>) task.getResult().getValue();
                otherUserUID = map.get("firebaseUID");
            }
            else
                onError();
        });
        childEventListener = new TradingInitializerChildEventListener(QRcodeActivity.this);
        databaseReference.addChildEventListener(childEventListener);
    }

    private void cancel() {
        databaseReference.removeEventListener(childEventListener);
        databaseReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_CANCELED);
        onBackPressed();
    }

    public void onOtherUserAccept() {
        wasAccepted = true;
        statusText.setText(String.format(getResources().getString(R.string.qr_code_status_accepted_other), otherUserName));
    }

    // TODO dialogs of onOtherUserReject, onOtherUserCanceled and onError could be made more beautiful
    public void onOtherUserReject() {
        new AlertDialog.Builder(QRcodeActivity.this)
                .setTitle(R.string.alert_title_trade_rejected)
                .setMessage(String.format(getResources().getString(R.string.alert_message_trade_rejected), otherUserName))
                .setPositiveButton(R.string.alert_understood, (dialog, which) -> onBackPressed())
                .create()
                .show();
        databaseReference.removeEventListener(childEventListener);
    }

    public void onOtherUserCanceled() {
        Trading.showTradeCanceledAlert(QRcodeActivity.this, (dialog, which) -> onBackPressed(), otherUserName);
        databaseReference.removeEventListener(childEventListener);
    }

    public void onOtherUserScanned() {
        databaseReference.removeEventListener(childEventListener);
        startTradingActivity();
    }

    private void startTradingActivity() {
        Intent intent = new Intent(QRcodeActivity.this, TradingActivity.class);
        intent.putExtra("tradingID", tradingID);
        intent.putExtra("isRequester", isRequester);
        intent.putExtra("otherUserName", otherUserName);
        startActivity(intent);
    }

    public void onError() {
        Trading.showTradeErrorAlert(QRcodeActivity.this, (dialog, which) -> onBackPressed());
        databaseReference.removeEventListener(childEventListener);
    }

    private void generateCode(){
        String sText = FirebaseAuth.getInstance().getUid();
        //String sText = qrInput.getText().toString().trim(); // get input value
                /*
                MiltiFormatWriter: factory class which finds the appropriate Writer subclass for the BarcodeFormat
                requested and encodes the barcode with the supplied contents
                 */
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            //Intialize bit matrix
            BitMatrix matrix = writer.encode(sText, BarcodeFormat.QR_CODE, 350, 350);
            //Initialize barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();
            //Initialize bitmap
            Bitmap bitmap = encoder.createBitmap(matrix);
            // set bitmap on imageview
            qrOutput.setImageBitmap(bitmap);
            // initialize input manager
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            // hide soft keyboard
            //manager.hideSoftInputFromWindow(qrInput.getApplicationWindowToken(), 0);
        } catch (WriterException e) {
            e.printStackTrace(); // print error
        }
    }

    public static void start(Context context, boolean isRequester, String tradePartnerNickname, String tradingID) {
        Bundle extras = new Bundle();
        extras.putBoolean("isRequester", isRequester);
        extras.putString("otherNickname", tradePartnerNickname);
        extras.putString("tradingID", tradingID);
        Intent intent = new Intent(context, QRcodeActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    private void toggleScannerAndCodeView() {
        if (qrOutput.getVisibility() == View.GONE) {
            qrOutput.setVisibility(View.VISIBLE);
            codeScannerView.setVisibility(View.GONE);
            scanButton.setText(R.string.scan_qrcode);
        }
        else {
            qrOutput.setVisibility(View.GONE);
            codeScannerView.setVisibility(View.VISIBLE);
            scanButton.setText(R.string.show_qrcode);
        }
    }

    //Needed for QR-Code scanner
    @Override
    public void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    public void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    private void scan(){
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this, scannerView);
        // decoding process
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                // return to the Main Thread to update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!wasAccepted) {
                            Toast.makeText(QRcodeActivity.this, "Request must be accepted first", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!result.getText().equals(otherUserUID))
                        {
                            Log.d(TAG, "Scanned: " + result.getText() + ", but expected was: " + otherUserUID);
                            Toast.makeText(QRcodeActivity.this, "Wrong QR-Code", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(QRcodeActivity.this, "Scanned successfully", Toast.LENGTH_SHORT).show();

                        databaseReference.removeEventListener(childEventListener);
                        databaseReference.child(Trading.ACTION_KEY).setValue(Trading.ACTION_SCANNED);
                        startTradingActivity();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScanner.startPreview();
            }
        });
    }

    private void setUpPermission(){
        // if camera permission was denied or it is the first time when user uses the app
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(permission != PackageManager.PERMISSION_GRANTED){
            makeRequest();
        }
    }

    private void makeRequest(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need a camera permission to be able to use a qr code scanner!", Toast.LENGTH_SHORT).show();
                }
                else{
                    // successful
                }

        }
    }
}

