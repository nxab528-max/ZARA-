package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    TextView status, result;
    TextToSpeech tts;
    boolean ttsReady = false;

    PackageManager pm;

    ArrayList<String> appNames = new ArrayList<>();
    ArrayList<String> packageNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pm = getPackageManager();

        loadApps();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(0xFF101820);

        TextView title = new TextView(this);
        title.setText("ZARA AI");
        title.setTextSize(34);
        title.setTextColor(0xFF00E5FF);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Hello! আমি ZARA");
        status.setTextSize(20);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 40, 10, 30);

        result = new TextView(this);
        result.setText("আমি তোমার কথা শুনছি।");
        result.setTextSize(17);
        result.setTextColor(0xFF00E5FF);
        result.setGravity(Gravity.CENTER);
        result.setPadding(10, 20, 10, 30);

        Button speakButton = new Button(this);
        speakButton.setText("🎤 TALK TO ZARA");

        layout.addView(title);
        layout.addView(status);
        layout.addView(result);
        layout.addView(speakButton);

        setContentView(layout);

        tts = new TextToSpeech(this, code -> {

            if (code == TextToSpeech.SUCCESS) {

                int language = tts.setLanguage(
                        new Locale("bn", "BD")
                );

                if (language == TextToSpeech.LANG_MISSING_DATA ||
                        language == TextToSpeech.LANG_NOT_SUPPORTED) {

                    tts.setLanguage(Locale.ENGLISH);
                }

                tts.setSpeechRate(0.95f);
                ttsReady = true;

                speak("Hello! আমি ZARA. How can I help you?");
            }
        });

        speakButton.setOnClickListener(v -> startListening());
    }

    // Load installed launchable apps

    private void loadApps() {

        appNames.clear();
        packageNames.clear();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        java.util.List<ResolveInfo> apps =
                pm.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {

            String name = app.loadLabel(pm).toString();
            String packageName = app.activityInfo.packageName;

            appNames.add(name);
            packageNames.add(packageName);
        }
    }

    // Voice recognition

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
                "বলুন, আমি শুনছি"
        );

        try {

            startActivityForResult(intent, 100);
            status.setText("Listening...");

        } catch (Exception e) {

            speak("Speech recognition is not available.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> matches =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (matches != null && !matches.isEmpty()) {

                String command = matches.get(0).toLowerCase(
                        Locale.ROOT
                );

                result.setText("You: " + command);

                processCommand(command);
            }
        }
    }

    // Process voice commands

    private void processCommand(String command) {

        if (command.contains("সময়") ||
                command.contains("টাইম") ||
                command.contains("কয়টা বাজে") ||
                command.contains("time")) {

            tellTime();
            return;
        }

        if (command.contains("তোমার নাম") ||
                command.contains("your name")) {

            speak("আমার নাম ZARA. I am your AI assistant.");
            return;
        }

        if (command.contains("কেমন আছো") ||
                command.contains("how are you")) {

            speak("আমি ভালো আছি। I am doing great!");
            return;
        }

        if (command.contains("settings") ||
                command.contains("সেটিংস")) {

            Intent settings = new Intent(
                    android.provider.Settings.ACTION_SETTINGS
            );

            startActivity(settings);
            speak("সেটিংস খুলছি। Opening Settings.");
            return;
        }

        if (command.contains("বন্ধ করো") ||
                command.contains("stop")) {

            speak("ঠিক আছে।");
            return;
        }

        // Search installed apps by name

        String cleaned = command
                .replace("open", "")
                .replace("খুলে দাও", "")
                .replace("খুলো", "")
                .replace("খুলুন", "")
                .replace("খোল", "")
                .replace("চালু করো", "")
                .replace("চালু কর", "")
                .replace("ওপেন করো", "")
                .replace("ওপেন", "")
                .replace("দাও", "")
                .replace("please", "")
                .trim();

        boolean found = false;

        for (int i = 0; i < appNames.size(); i++) {

            String appName = appNames.get(i).toLowerCase(
                    Locale.ROOT
            );

            if (cleaned.equals(appName) ||
                    cleaned.contains(appName) ||
                    appName.contains(cleaned)) {

                openApp(packageNames.get(i), appNames.get(i));
                found = true;
                break;
            }
        }

        if (!found) {

            speak("দুঃখিত, অ্যাপটি খুঁজে পাইনি। Please try again.");
        }
    }

    // Open any matching installed app

    private void openApp(String packageName, String appName) {

        Intent launchIntent =
                pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {

            speak(appName + " খুলছি। Opening " + appName);

            startActivity(launchIntent);

        } else {

            speak("এই অ্যাপটি চালু করা যাচ্ছে না।");
        }
    }

    // Current time

    private void tellTime() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.ENGLISH
                );

        String time = format.format(new Date());

        speak("এখন সময় " + time +
                "। The current time is " + time);
    }

    // Text to speech

    private void speak(String message) {

        status.setText("ZARA Speaking...");
        result.setText("ZARA: " + message);

        if (ttsReady) {

            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "ZARA_SPEECH"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
