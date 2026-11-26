package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Gravity;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    TextView status;

    Button startButton;
    Button stopButton;

    private static final int PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.BLACK);

        status = new TextView(this);

        status.setText("ZARA AI\n\nVoice Assistant");
        status.setTextSize(26);
        status.setTextColor(Color.WHITE);
        status.setGravity(Gravity.CENTER);

        startButton = new Button(this);
        startButton.setText("START ZARA");

        stopButton = new Button(this);
        stopButton.setText("STOP ZARA");

        layout.addView(status);
        layout.addView(startButton);
        layout.addView(stopButton);

        setContentView(layout);

        startButton.setOnClickListener(v -> checkPermissions());

        stopButton.setOnClickListener(v -> stopZara());
    }

    private void checkPermissions() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    PERMISSION_CODE
            );

            return;
        }

        startZara();
    }

    private void startZara() {

        Intent intent = new Intent(
                MainActivity.this,
                ZaraVoiceService.class
        );

        intent.setAction("START");

        try {

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

            status.setText(
                    "ZARA IS ACTIVE\n\nSay Hey ZARA!"
            );

        } catch (Exception e) {

            status.setText("Unable to start ZARA");
        }
    }

    private void stopZara() {

        Intent intent = new Intent(
                MainActivity.this,
                ZaraVoiceService.class
        );

        intent.setAction("STOP");

        startService(intent);

        status.setText("ZARA STOPPED");
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startZara();

        } else {

            status.setText("Microphone Permission Required");
        }
    }
}
