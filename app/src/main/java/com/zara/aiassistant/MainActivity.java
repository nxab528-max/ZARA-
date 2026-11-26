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
    Button startButton, stopButton;

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

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_CODE
                );

                return;
            }

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_CODE
                );

                return;
            }

        } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_CODE
            );

            return;
        }

        startZara();
    }

    private void startZara() {

        Intent intent = new Intent(this, ZaraVoiceService.class);
        intent.setAction("START");

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

            status.setText("ZARA IS ACTIVE\n\nSay Hey ZARA!");

        } catch (Exception e) {
            status.setText("Unable to start ZARA");
        }
    }

    private void stopZara() {

        Intent intent = new Intent(this, ZaraVoiceService.class);

        stopService(intent);

        status.setText("ZARA STOPPED");
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == PERMISSION_CODE) {

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {

                checkPermissions();

            } else {
                status.setText("Microphone Permission Required");
            }
        }
    }
}
