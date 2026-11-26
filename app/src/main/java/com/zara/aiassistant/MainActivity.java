package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    TextView status, result;

    TextToSpeech tts;
    boolean ttsReady = false;

    PackageManager pm;

    ArrayList<String> appNames = new ArrayList<>();
    ArrayList<ResolveInfo> appList = new ArrayList<>();

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
        status.setText("ZARA Ready");
        status.setTextSize(20);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 40, 10, 20);

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

        status.setText("ZARA Ready | Apps: " + appNames.size());

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

    // Load installed apps

    private void loadApps() {

        appNames.clear();
        appList.clear();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps =
                pm.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {

            if (app.activityInfo == null) continue;

            if (!app.activityInfo.enabled) continue;

            String name = app.loadLabel(pm).toString();

            appNames.add(name);
            appList.add(app);
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
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "bn-BD"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "বলুন, আমি শুনছি..."
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

                String command = matches.get(0);

                result.setText("You: " + command);

                processCommand(command);
            }
        }
    }

    // Normalize text

    private String normalize(String text) {

        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    // Bengali app name conversion

    private String translateCommand(String command) {

        command = command.toLowerCase(Locale.ROOT);

        command = command.replace("ফেসবুক লাইট", "facebook lite");
        command = command.replace("ফেইসবুক লাইট", "facebook lite");
        command = command.replace("ফেসবুক", "facebook");
        command = command.replace("ফেইসবুক", "facebook");

        command = command.replace("টিকটক লাইট", "tiktok lite");
        command = command.replace("টিক টক লাইট", "tiktok lite");
        command = command.replace("টিকটক", "tiktok");
        command = command.replace("টিক টক", "tiktok");

        command = command.replace("ইউটিউব", "youtube");
        command = command.replace("হোয়াটসঅ্যাপ", "whatsapp");
        command = command.replace("হোয়াটসঅ্যাপ", "whatsapp");
        command = command.replace("ইমো", "imo");
        command = command.replace("ইনস্টাগ্রাম", "instagram");
        command = command.replace("মেসেঞ্জার", "messenger");
        command = command.replace("ক্রোম", "chrome");
        command = command.replace("ক্যামেরা", "camera");
        command = command.replace("প্লে স্টোর", "play store");

        return command;
    }

    // Remove command words

    private String cleanCommand(String command) {

        command = translateCommand(command);

        String[] removeWords = {

                "open",
                "launch",
                "start",
                "please",
                "app",
                "application",

                "খুলে দাও",
                "খুলে দে",
                "খুলে দিন",
                "খুলে দিও",
                "খুলে",
                "খুলো",
                "খুলুন",
                "খুল",
                "চালু করো",
                "চালু কর",
                "ওপেন করো",
                "ওপেন",
                "দাও",
                "দিয়ে দাও"
        };

        for (String word : removeWords) {

            command = command.replace(word, " ");
        }

        return normalize(command);
    }

    // Process commands

    private void processCommand(String command) {

        String original = command.toLowerCase(Locale.ROOT);

        if (original.contains("সময়") ||
            original.contains("টাইম") ||
            original.contains("কয়টা বাজে") ||
            original.contains("time")) {

            tellTime();
            return;
        }

        if (original.contains("তোমার নাম") ||
            original.contains("your name")) {

            speak("আমার নাম ZARA. I am your AI assistant.");
            return;
        }

        if (original.contains("কেমন আছো") ||
            original.contains("how are you")) {

            speak("আমি ভালো আছি। I am doing great!");
            return;
        }

        if (original.contains("সেটিংস") ||
            original.contains("settings")) {

            Intent settings = new Intent(
                    android.provider.Settings.ACTION_SETTINGS
            );

            startActivity(settings);

            speak("Settings খুলছি।");
            return;
        }

        String cleaned = cleanCommand(original);

        if (cleaned.isEmpty()) {

            speak("কোন অ্যাপ খুলতে হবে বলো।");
            return;
        }

        // EXACT APP NAME MATCHING

        for (int i = 0; i < appList.size(); i++) {

            String installedName =
                    normalize(appNames.get(i));

            if (cleaned.equals(installedName)) {

                openApp(appList.get(i));
                return;
            }
        }

        // No wrong app opening

        speak("দুঃখিত, এই নামে কোনো অ্যাপ খুঁজে পাইনি।");
    }

    // Open exact matching app

    private void openApp(ResolveInfo app) {

        try {

            String name = app.loadLabel(pm).toString();

            Intent launch = new Intent(Intent.ACTION_MAIN);

            launch.addCategory(Intent.CATEGORY_LAUNCHER);

            launch.setComponent(
                    new ComponentName(
                            app.activityInfo.packageName,
                            app.activityInfo.name
                    )
            );

            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            speak(name + " খুলছি। Opening " + name);

            startActivity(launch);

        } catch (Exception e) {

            speak("অ্যাপটি খুলতে সমস্যা হচ্ছে।");
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
