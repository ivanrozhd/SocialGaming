package com.example.littlelarri;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.View;

public class KawaiiView extends View {
    Handler handler;
    Runnable runnable;
    final int UPDATE_MILLIS = 30;
    Bitmap kawaii;


    public KawaiiView(Context context) {
        super(context);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate(); // call onDraw()
            }
        };
        kawaii = BitmapFactory.decodeResource(getResources(), R.drawable.mapmarkerblue);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw kawaii on canvas
        canvas.drawBitmap(kawaii, 0, 0, null);
        handler.postDelayed(runnable, UPDATE_MILLIS);

    }
}
