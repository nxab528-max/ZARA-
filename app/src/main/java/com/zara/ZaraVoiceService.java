package com.zara.aiassistant;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.speech.SpeechRecognizer;
import android.Manifest;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Locale;

public class ZaraVoiceService extends Service {

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private Handler handler;

    private boolean active = false;
    private boolean ttsReady = false;

    private static final String CHANNEL_ID = "ZARA_VOICE_CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());

        createNotificationChannel();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("en", "US"));
                ttsReady = true;

                if (active) {
                    startRecognizer();
                }
            }
        });

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {

                @Override
                public void onReadyForSpeech(android.os.Bundle params) {}

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
                    scheduleListening();
                }

                @Override
                public void onResults(android.os.Bundle results) {
                    ArrayList<String> matches =
                            results.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION
                            );

                    if (matches != null && !matches.isEmpty()) {
                        handleCommand(matches.get(0));
                    }

                    scheduleListening();
                }

                @Override
                public void onPartialResults(android.os.Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, android.os.Bundle params) {}
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1, buildNotification());

        active = true;

        if (ttsReady) {
            startRecognizer();
        }

        return START_STICKY;
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ZARA Assistant")
                .setContentText("ZARA voice service is running")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ZARA Voice Assistant",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private void startRecognizer() {
        if (!active || recognizer == null) return;

        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "bn-BD"
            );

            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

            recognizer.startListening(intent);

        } catch (Exception e) {
            scheduleListening();
        }
    }

    private void scheduleListening() {
        if (!active) return;

        handler.postDelayed(this::startRecognizer, 1200);
    }

    private void handleCommand(String command) {

        String text = command.toLowerCase(Locale.ROOT);

        if (text.contains("hey zara") ||
                text.contains("হেই জারা") ||
                text.contains("হে জারা")) {

            speak("Yes Boss! I'm listening.");
            return;
        }

        if (text.contains("facebook lite") ||
                text.contains("ফেসবুক লাইট")) {

            openApp("com.facebook.lite");
        }

        else if (text.contains("tiktok") ||
                text.contains("টিকটক")) {

            openApp("com.zhiliaoapp.musically");
        }

        else if (text.contains("youtube") ||
                text.contains("ইউটিউব")) {

            openApp("com.google.android.youtube");
        }

        else if (text.contains("time") ||
                text.contains("সময়") ||
                text.contains("কটা বাজে")) {

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                    );

            speak("Boss, time is " + sdf.format(new java.util.Date()));
        }

        else {
            speak("Sorry Boss, I did not understand.");
        }
    }

    private void openApp(String packageName) {

        PackageManager pm = getPackageManager();

        Intent launchIntent =
                pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
            speak("Opening your app, Boss.");
        } else {
            speak("Sorry Boss, app not found.");
        }
    }

    private void speak(String message) {
        if (tts != null && ttsReady) {
            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "ZARA_SPEECH"
            );
        }
    }

    @Override
    public void onDestroy() {
        active = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
  }
