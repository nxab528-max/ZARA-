package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status;
    TextView result;
    TextToSpeech tts;

    final int SPEECH_REQUEST = 100;

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
        speakButton.setText("🎤 কথা বলুন");

        layout.addView(title);
        layout.addView(status);
        layout.addView(result);
        layout.addView(speakButton);

        setContentView(layout);

        tts = new TextToSpeech(this, value -> {
            if (value != TextToSpeech.ERROR) {
                tts.setLanguage(new Locale("bn", "BD"));
            }
        });

        speakButton.setOnClickListener(v -> startListening());
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "ZARA-কে কিছু বলুন");

        try {
            startActivityForResult(intent, SPEECH_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Speech Recognition পাওয়া যাচ্ছে না", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches =
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches != null && !matches.isEmpty()) {
                String command = matches.get(0);
                result.setText("তুমি বলেছ: " + command);
                processCommand(command.toLowerCase());
            }
        }
    }

    private void processCommand(String command) {

        String reply = "দুঃখিত, আমি এখনো এই কমান্ড শিখিনি।";

        if (command.contains("হ্যালো") || command.contains("hello")) {
            reply = "হ্যালো! আমি জারা।";
        }
        else if (command.contains("সময়") || command.contains("time")) {
            reply = "এখন সময় " +
                new java.text.SimpleDateFormat("hh:mm a",
                Locale.getDefault()).format(new java.util.Date());
        }
        else if (command.contains("chrome")) {
            reply = "Chrome খুলছি।";
            openApp("com.android.chrome");
        }
        else if (command.contains("youtube")) {
            reply = "YouTube খুলছি।";
            openApp("com.google.android.youtube");
        }
        else if (command.contains("বন্ধ") || command.contains("stop")) {
            reply = "ঠিক আছে।";
        }

        status.setText(reply);
        speak(reply);
    }

    private void openApp(String packageName) {
        Intent launchIntent = getPackageManager()
            .getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "অ্যাপটি পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "ZARA_REPLY");
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
