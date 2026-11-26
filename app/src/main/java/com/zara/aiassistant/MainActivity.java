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
import android.Manifest;
import android.content.pm.PackageManager;
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

    Runnable restartTask;

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
                            100
                    );
                }
            }
        });
    }

    private void initializeZara() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech Recognition Not Available");
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
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
                scheduleListening(1000);
            }

            @Override
            public void onResults(Bundle results) {

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

        scheduleListening(500);
    }

    private void startListening() {

        if (recognizer == null || speaking) return;

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
            scheduleListening(1500);
        }
    }

    private void processVoice(String voice) {

        String text = voice.toLowerCase(Locale.ROOT);

        if (!commandMode) {

            if (text.contains("hey zara") ||
                text.contains("hey jara") ||
                text.contains("হেই জারা") ||
                text.contains("হে জারা")) {

                commandMode = true;

                speak("Yes Boss!", Locale.US, () -> {
                    startListening();
                });

            } else {
                scheduleListening(500);
            }

            return;
        }

        commandMode = false;

        if (text.contains("facebook")) {

            openApp(
                    "com.facebook.lite",
                    "Opening Facebook Lite."
            );

        } else if (text.contains("tiktok") ||
                   text.contains("টিকটক")) {

            openApp(
                    "com.zhiliaoapp.musically",
                    "Opening TikTok."
            );

        } else if (text.contains("youtube") ||
                   text.contains("ইউটিউব")) {

            openApp(
                    "com.google.android.youtube",
                    "Opening YouTube."
            );

        } else if (text.contains("time") ||
                   text.contains("সময়") ||
                   text.contains("কয়টা বাজে")) {

            String time = new SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
            ).format(new Date());

            speak("The time is " + time, Locale.US,
                    () -> scheduleListening(500));

        } else if (text.contains("date") ||
                   text.contains("তারিখ")) {

            String date = new SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.ENGLISH
            ).format(new Date());

            speak("Today's date is " + date, Locale.US,
                    () -> scheduleListening(500));

        } else {

            speak(
                    "Sorry Boss, I did not understand.",
                    Locale.US,
                    () -> scheduleListening(500)
            );
        }
    }

    private void openApp(String packageName, String message) {

        Intent launchIntent =
                getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {

            speak(message, Locale.US, () -> {
                try {
                    startActivity(launchIntent);
                    scheduleListening(1500);
                } catch (Exception e) {
                    scheduleListening(500);
                }
            });

        } else {

            speak(
                    "Sorry Boss, this app is not installed.",
                    Locale.US,
                    () -> scheduleListening(500)
            );
        }
    }

    private void speak(String message, Locale language, Runnable callback) {

        if (!ttsReady) return;

        speaking = true;

        status.setText("ZARA: " + message);

        tts.setLanguage(language);

        tts.setSpeechRate(1.0f);

        String id = "ZARA_" + System.currentTimeMillis();

        tts.setOnUtteranceProgressListener(
                new android.speech.tts.UtteranceProgressListener() {

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
                    scheduleListening(500);
                });
            }
        });

        tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                id
        );
    }

    private void scheduleListening(long delay) {

        if (isFinishing()) return;

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(() -> {

            if (!speaking) {
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
                requestCode, permissions, grantResults
        );

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            initializeZara();

        } else {
            status.setText("Microphone Permission Required");
        }
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (recognizer != null) {
            recognizer.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
