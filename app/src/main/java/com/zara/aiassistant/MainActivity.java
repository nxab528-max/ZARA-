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
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    TextView status, result;
    Button speakButton;
    TextToSpeech tts;

    boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        layout.setBackgroundColor(0xFF101820);

        TextView title = new TextView(this);
        title.setText("ZARA AI");
        title.setTextSize(32);
        title.setTextColor(0xFF00E5FF);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("Hello! আমি ZARA AI");
        status.setTextSize(19);
        status.setTextColor(0xFFFFFFFF);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 40, 10, 30);

        result = new TextView(this);
        result.setText("আমি তোমার কথা শুনছি।");
        result.setTextSize(17);
        result.setTextColor(0xFF00E5FF);
        result.setGravity(Gravity.CENTER);
        result.setPadding(10, 20, 10, 30);

        speakButton = new Button(this);
        speakButton.setText("🎤 TALK TO ZARA");

        layout.addView(title);
        layout.addView(status);
        layout.addView(result);
        layout.addView(speakButton);

        setContentView(layout);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int code) {

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
                "বলুন, আমি শুনছি..."
        );

        try {
            startActivityForResult(intent, 100);
            status.setText("Listening...");
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Speech recognition not available",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
            );

            if (matches != null && !matches.isEmpty()) {

                String command = matches.get(0).toLowerCase();

                result.setText("You: " + command);

                processCommand(command);
            }
        }
    }

    private void processCommand(String command) {

        if (command.contains("সময়") ||
            command.contains("টাইম") ||
            command.contains("কয়টা বাজে") ||
            command.contains("time") ||
            command.contains("clock")) {

            tellTime();
        }

        else if (command.contains("hello") ||
                 command.contains("হ্যালো") ||
                 command.contains("হাই")) {

            speak("Hello! কেমন আছো? How can I help you?");
        }

        else if (command.contains("তোমার নাম") ||
                 command.contains("your name")) {

            speak("আমার নাম ZARA. I am your AI assistant.");
        }

        else if (command.contains("কেমন আছো") ||
                 command.contains("how are you")) {

            speak("আমি ভালো আছি। I am doing great!");
        }

        else if (command.contains("ধন্যবাদ") ||
                 command.contains("thank you")) {

            speak("তোমাকে স্বাগতম! You are welcome.");
        }

        else {

            speak("আমি তোমার কথা বুঝতে পেরেছি। কিন্তু এই command এখনো শিখিনি।");
        }
    }

    private void tellTime() {

        SimpleDateFormat format =
                new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        String currentTime = format.format(new Date());

        String response =
                "এখন সময় " + currentTime +
                "। The current time is " + currentTime;

        speak(response);
    }

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
