package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.provider.Settings;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status;
    EditText commandInput;

    final int SPEECH_REQUEST = 100;
    final int AUDIO_PERMISSION = 200;

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
        status.setText("আমি ZARA। তোমার কথা শুনতে প্রস্তুত!");
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        commandInput = new EditText(this);
        commandInput.setHint("কমান্ড লিখুন / Enter command");

        Button runButton = new Button(this);
        runButton.setText("RUN COMMAND");

        Button voiceButton = new Button(this);
        voiceButton.setText("🎤 SPEAK TO ZARA");

        layout.addView(title);
        layout.addView(status);
        layout.addView(commandInput);
        layout.addView(runButton);
        layout.addView(voiceButton);

        setContentView(layout);

        runButton.setOnClickListener(v -> {
            String command = commandInput.getText()
                .toString()
                .toLowerCase(Locale.ROOT)
                .trim();

            if (!command.isEmpty()) {
                processCommand(command);
            }
        });

        voiceButton.setOnClickListener(v -> checkPermission());
    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_PERMISSION
            );

        } else {
            startListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        );

        if (requestCode == AUDIO_PERMISSION &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startListening();

        } else if (requestCode == AUDIO_PERMISSION) {

            status.setText("মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন।");
        }
    }

    private void startListening() {

        Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "bn-BD"
        );

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "ZARA-কে নির্দেশ দিন"
        );

        try {

            status.setText("আমি শুনছি...");

            startActivityForResult(intent, SPEECH_REQUEST);

        } catch (Exception e) {

            status.setText("Voice Recognition পাওয়া যাচ্ছে না।");

            Toast.makeText(this,
                "Speech service পাওয়া যায়নি",
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST &&
            resultCode == RESULT_OK &&
            data != null) {

            ArrayList<String> matches =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                );

            if (matches != null && !matches.isEmpty()) {

                String command = matches.get(0);

                commandInput.setText(command);

                status.setText("তুমি বলেছ: " + command);

                processCommand(
                    command.toLowerCase(Locale.ROOT)
                );
            }
        }
    }

    private void processCommand(String command) {

        if (command.contains("facebook lite") ||
            command.contains("ফেসবুক লাইট")) {

            openApp("com.facebook.lite", "Facebook Lite");

        } else if (command.contains("facebook") ||
                   command.contains("ফেসবুক")) {

            openApp("com.facebook.katana", "Facebook");

        } else if (command.contains("whatsapp") ||
                   command.contains("হোয়াটসঅ্যাপ") ||
                   command.contains("হোয়াটসঅ্যাপ")) {

            openApp("com.whatsapp", "WhatsApp");

        } else if (command.contains("youtube") ||
                   command.contains("ইউটিউব")) {

            openApp("com.google.android.youtube", "YouTube");

        } else if (command.contains("chrome")) {

            openApp("com.android.chrome", "Chrome");

        } else if (command.contains("camera") ||
                   command.contains("ক্যামেরা")) {

            openCamera();

        } else if (command.contains("gallery") ||
                   command.contains("গ্যালারি")) {

            openGallery();

        } else if (command.contains("settings") ||
                   command.contains("সেটিংস")) {

            startActivity(
                new Intent(Settings.ACTION_SETTINGS)
            );

            status.setText("Settings খুলছি।");

        } else if (command.contains("wifi") ||
                   command.contains("ওয়াইফাই")) {

            startActivity(
                new Intent(Settings.ACTION_WIFI_SETTINGS)
            );

            status.setText("Wi-Fi Settings খুলছি।");

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

            status.setText(
                "দুঃখিত! এই কমান্ডটি এখনো শিখিনি।"
            );
        }

        commandInput.setText("");
    }

    private void openApp(
        String packageName,
        String appName
    ) {

        Intent intent = getPackageManager()
            .getLaunchIntentForPackage(packageName);

        if (intent != null) {

            startActivity(intent);
            status.setText(appName + " খুলছি।");

        } else {

            status.setText(appName + " পাওয়া যায়নি।");

            Toast.makeText(this,
                appName + " শনাক্ত করা যায়নি।",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {

        Intent intent = new Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        );

        if (intent.resolveActivity(getPackageManager()) != null) {

            startActivity(intent);
            status.setText("Camera খুলছি।");

        } else {

            status.setText("Camera পাওয়া যায়নি।");
        }
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        try {

            startActivityForResult(intent, 300);
            status.setText("Gallery খুলছি।");

        } catch (Exception e) {

            status.setText("Gallery পাওয়া যায়নি।");
        }
    }
            }
