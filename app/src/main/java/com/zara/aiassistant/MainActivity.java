package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status, result;
    TextToSpeech tts;

    final int SPEECH_REQUEST = 100;
    boolean ttsReady = false;

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

        result = new TextView(this);
        result.setTextSize(18);
        result.setGravity(Gravity.CENTER);

        Button speakButton = new Button(this);
        speakButton.setText("🎤 কথা বলুন / Speak");

        layout.addView(title);
        layout.addView(status);
        layout.addView(result);
        layout.addView(speakButton);

        setContentView(layout);

        tts = new TextToSpeech(this, value -> {
            if (value == TextToSpeech.SUCCESS) {

                ttsReady = true;

                tts.setSpeechRate(0.85f);
                tts.setPitch(1.3f);

                speak("হ্যালো! আমি জারা। Hello! I am Zara.");
            }
        });

        speakButton.setOnClickListener(v -> startListening());
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
            "ZARA-কে কিছু বলুন / Speak to Zara"
        );

        try {
            startActivityForResult(intent, SPEECH_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this,
                "Speech Recognition পাওয়া যাচ্ছে না",
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST &&
            resultCode == RESULT_OK && data != null) {

            ArrayList<String> matches =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                );

            if (matches != null && !matches.isEmpty()) {

                String command = matches.get(0);

                result.setText("তুমি বলেছ: " + command);

                processCommand(command.toLowerCase());
            }
        }
    }

    private void processCommand(String command) {

        String reply;

        if (command.contains("হ্যালো") ||
            command.contains("hello")) {

            reply = "হ্যালো! আমি জারা। Hello! I am Zara.";
        }

        else if (command.contains("সময়") ||
                 command.contains("time")) {

            String time =
                new java.text.SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()
                ).format(new java.util.Date());

            reply = "এখন সময় " + time;
        }

        else if (command.contains("chrome")) {

            reply = "Chrome খুলছি। Opening Chrome.";
            openApp("com.android.chrome");
        }

        else if (command.contains("youtube")) {

            reply = "YouTube খুলছি। Opening YouTube.";
            openApp("com.google.android.youtube");
        }

        else if (command.contains("বন্ধ") ||
                 command.contains("stop")) {

            reply = "ঠিক আছে। Okay.";
        }

        else {
            reply = "দুঃখিত, আমি এখনো এই কমান্ড শিখিনি। Sorry, I don't know this command yet.";
        }

        status.setText(reply);
        speak(reply);
    }

    private void openApp(String packageName) {

        Intent launchIntent =
            getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this,
                "অ্যাপটি পাওয়া যায়নি",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String message) {

        if (tts != null && ttsReady) {

            tts.setSpeechRate(0.85f);
            tts.setPitch(1.3f);

            boolean hasBangla =
                message.matches(".*[\\u0980-\\u09FF].*");

            Locale language = hasBangla
                ? new Locale("bn", "BD")
                : Locale.US;

            if (tts.isLanguageAvailable(language)
                >= TextToSpeech.LANG_AVAILABLE) {

                tts.setLanguage(language);
            }

            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "ZARA_REPLY"
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
