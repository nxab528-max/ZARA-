package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.provider.MediaStore;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status;
    EditText commandInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("ZARA AI");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Hello! আমি ZARA। Ready to help you.");
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        commandInput = new EditText(this);
        commandInput.setHint("কমান্ড লিখুন / Enter command");

        Button runButton = new Button(this);
        runButton.setText("RUN COMMAND");

        layout.addView(title);
        layout.addView(status);
        layout.addView(commandInput);
        layout.addView(runButton);

        setContentView(layout);

        runButton.setOnClickListener(v -> {
            String command = commandInput.getText()
                .toString().toLowerCase(Locale.ROOT).trim();

            if (!command.isEmpty()) {
                processCommand(command);
            }
        });
    }

    private void processCommand(String command) {

        if (command.contains("youtube")) {

            openApp("com.google.android.youtube", "YouTube");

        } else if (command.contains("chrome")) {

            openApp("com.android.chrome", "Chrome");

        } else if (command.contains("facebook")) {

            openApp("com.facebook.katana", "Facebook");

        } else if (command.contains("whatsapp")) {

            openApp("com.whatsapp", "WhatsApp");

        } else if (command.contains("camera") ||
                   command.contains("ক্যামেরা")) {

            openCamera();

        } else if (command.contains("gallery") ||
                   command.contains("গ্যালারি")) {

            openGallery();

        } else if (command.contains("settings") ||
                   command.contains("সেটিংস")) {

            startActivity(new Intent(Settings.ACTION_SETTINGS));
            status.setText("Settings খুলছি।");

        } else if (command.contains("wifi") ||
                   command.contains("ওয়াইফাই")) {

            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            status.setText("WiFi Settings খুলছি।");

        } else if (command.contains("time") ||
                   command.contains("সময়")) {

            String time = new SimpleDateFormat(
                "hh:mm a", Locale.getDefault()
            ).format(new Date());

            status.setText("এখন সময় " + time);

        } else if (command.contains("date") ||
                   command.contains("তারিখ")) {

            String date = new SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault()
            ).format(new Date());

            status.setText("আজকের তারিখ " + date);

        } else if (command.contains("hello") ||
                   command.contains("হ্যালো")) {

            status.setText("হ্যালো! আমি ZARA AI।");

        } else {

            status.setText("দুঃখিত! এই কমান্ডটি এখনো শিখিনি।");
        }

        commandInput.setText("");
    }

    private void openApp(String packageName, String appName) {

        Intent intent = getPackageManager()
            .getLaunchIntentForPackage(packageName);

        if (intent != null) {

            startActivity(intent);
            status.setText(appName + " খুলছি।");

        } else {

            status.setText(appName + " পাওয়া যায়নি।");
            Toast.makeText(this,
                appName + " ইনস্টল করা নেই।",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            status.setText("Camera খুলছি।");
        } else {
            status.setText("Camera পাওয়া যায়নি।");
        }
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            status.setText("Gallery খুলছি।");
        } else {
            status.setText("Gallery পাওয়া যায়নি।");
        }
    }
}
