package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.Manifest;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

TextView status;

SpeechRecognizer recognizer;
TextToSpeech tts;

Handler handler = new Handler();

boolean commandMode = false;
boolean speaking = false;
boolean ttsReady = false;
boolean listening = false;

private static final int MIC_PERMISSION = 100;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    status = new TextView(this);
    status.setText("ZARA AI\n\nStarting...");
    status.setTextSize(25);
    status.setTextColor(Color.WHITE);
    status.setGravity(Gravity.CENTER);
    status.setBackgroundColor(Color.BLACK);

    setContentView(status);

    tts = new TextToSpeech(this, result -> {

        if (result == TextToSpeech.SUCCESS) {

            ttsReady = true;

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {

                initializeZara();

            } else {

                requestPermissions(
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MIC_PERMISSION
                );
            }

        } else {

            status.setText("ZARA: TTS Initialization Failed");
        }
    });
}

private void initializeZara() {

    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        status.setText("Speech Recognition Not Available");
        return;
    }

    if (recognizer != null) {
        recognizer.destroy();
    }

    recognizer = SpeechRecognizer.createSpeechRecognizer(this);

    recognizer.setRecognitionListener(new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {

            listening = true;

            status.setText(commandMode
                    ? "ZARA: Listening to your command..."
                    : "ZARA: Say Hey ZARA!");
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {

            listening = false;

            scheduleListening(1200);
        }

        @Override
        public void onResults(Bundle results) {

            listening = false;

            ArrayList<String> matches =
                    results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );

            if (matches != null && !matches.isEmpty()) {

                processVoice(matches.get(0));

            } else {

                scheduleListening(500);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    });

    scheduleListening(700);
}

private void startListening() {

    if (recognizer == null || speaking || listening) {
        return;
    }

    Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
    );

    intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    );

    // Uses the phone's selected language.
    // Bengali and English recognition depends on the
    // installed speech recognition provider.

    intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault().toLanguageTag()
    );

    intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            false
    );

    intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            3
    );

    try {

        recognizer.startListening(intent);

    } catch (Exception e) {

        listening = false;
        scheduleListening(1500);
    }
}

private void processVoice(String voice) {

    String text = voice.toLowerCase(Locale.ROOT).trim();

    if (!commandMode) {

        if (text.contains("hey zara") ||
            text.contains("hey jara") ||
            text.contains("হেই জারা") ||
            text.contains("হে জারা") ||
            text.contains("হে জারা")) {

            commandMode = true;

            speak(
                    "Yes Boss! বলো, What can I do for you?",
                    () -> scheduleListening(400)
            );

        } else {

            scheduleListening(500);
        }

        return;
    }

    commandMode = false;

    if (text.contains("facebook") ||
        text.contains("ফেসবুক")) {

        openApp(
                "com.facebook.lite",
                "Okay Boss! Opening Facebook Lite."
        );

    } else if (text.contains("tiktok") ||
               text.contains("টিকটক")) {

        openApp(
                "com.zhiliaoapp.musically",
                "Okay Boss! Opening TikTok."
        );

    } else if (text.contains("youtube") ||
               text.contains("ইউটিউব")) {

        openApp(
                "com.google.android.youtube",
                "Okay Boss! Opening YouTube."
        );

    } else if (text.contains("time") ||
               text.contains("সময়") ||
               text.contains("সময়") ||
               text.contains("কয়টা") ||
               text.contains("কয়টা") ||
               text.contains("বাজে")) {

        String time = new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        speak(
                "Boss, এখন সময় " + time,
                () -> scheduleListening(500)
        );

    } else if (text.contains("date") ||
               text.contains("তারিখ")) {

        String date = new SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.ENGLISH
        ).format(new Date());

        speak(
                "Boss, today's date is " + date,
                () -> scheduleListening(500)
        );

    } else {

        speak(
                "Sorry Boss! বুঝতে পারিনি. Please try again.",
                () -> scheduleListening(500)
        );
    }
}

private void openApp(String packageName, String message) {

    Intent launchIntent =
            getPackageManager().getLaunchIntentForPackage(packageName);

    if (launchIntent != null) {

        speak(message, () -> {

            try {

                startActivity(launchIntent);

            } catch (Exception e) {

                status.setText("Unable to open application");
                scheduleListening(1000);
            }
        });

    } else {

        speak(
                "Sorry Boss! This app is not installed.",
                () -> scheduleListening(500)
        );
    }
}

private void speak(String message, Runnable callback) {

    if (!ttsReady || tts == null) {
        return;
    }

    speaking = true;

    status.setText("ZARA: " + message);

    // Bengali voice if available, otherwise English.
    boolean hasBengali = message.matches(".*[\\u0980-\\u09FF].*");

    int languageResult;

    if (hasBengali &&
            tts.isLanguageAvailable(
                    Locale.forLanguageTag("bn-BD")
            ) >= TextToSpeech.LANG_AVAILABLE) {

        languageResult = tts.setLanguage(
                Locale.forLanguageTag("bn-BD")
        );

    } else {

        languageResult = tts.setLanguage(Locale.US);
    }

    tts.setSpeechRate(1.0f);

    String id = "ZARA_" + System.currentTimeMillis();

    tts.setOnUtteranceProgressListener(
            new UtteranceProgressListener() {

        @Override
        public void onStart(String utteranceId) {}

        @Override
        public void onDone(String utteranceId) {

            runOnUiThread(() -> {

                speaking = false;

                if (callback != null) {
                    callback.run();
                }
            });
        }

        @Override
        public void onError(String utteranceId) {

            runOnUiThread(() -> {

                speaking = false;
                scheduleListening(700);
            });
        }
    });

    int result = tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            id
    );

    if (result == TextToSpeech.ERROR) {

        speaking = false;
        scheduleListening(700);
    }
}

private void scheduleListening(long delay) {

    handler.removeCallbacksAndMessages(null);

    handler.postDelayed(() -> {

        if (!isFinishing() && !speaking && !listening) {
            startListening();
        }

    }, delay);
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

    if (requestCode == MIC_PERMISSION &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        initializeZara();

    } else {

        status.setText("ZARA: Microphone Permission Required");
    }
}

@Override
protected void onDestroy() {

    handler.removeCallbacksAndMessages(null);

    if (recognizer != null) {

        recognizer.cancel();
        recognizer.destroy();
        recognizer = null;
    }

    if (tts != null) {

        tts.stop();
        tts.shutdown();
        tts = null;
    }

    super.onDestroy();
}

}
