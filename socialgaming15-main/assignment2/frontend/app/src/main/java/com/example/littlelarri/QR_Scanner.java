package com.example.littlelarri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.littlelarri.activities.LarryPageActivity;
import com.example.littlelarri.activities.WelcomeActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;

import java.lang.reflect.Array;
import java.util.concurrent.ExecutionException;

public class QR_Scanner extends AppCompatActivity {
    private CodeScanner codeScanner;
    private final int CAMERA_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        // Camera permission
        setUpPermission();
        // if camera permission was denied, the scan() method will not be executed
        scan();

    }
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
                        Toast.makeText(QR_Scanner.this, result.getText(), Toast.LENGTH_SHORT).show();
                        switchToShowResourcesActibity();
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

    private void switchToShowResourcesActibity() {
        Intent intent = new Intent(QR_Scanner.this, ShowResourcesActivity.class);
        startActivity(intent);
    }
}


