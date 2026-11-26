package com.zara.aiassistant;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class ZaraVoiceService extends Service {

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private Handler handler;

    private boolean active = false;
    private boolean ttsReady = false;
    private boolean awaitingCommand = false;

    private static final String CHANNEL_ID = "ZARA_VOICE_CHANNEL";

    private final Runnable listenRunnable = this::startRecognizer;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());

        createNotificationChannel();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("bn", "BD"));
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
                    scheduleListening(1500);
                }

                @Override
                public void onResults(android.os.Bundle results) {

                    ArrayList<String> matches =
                            results.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION
                            );

                    if (matches != null && !matches.isEmpty()) {
                        handleCommand(matches.get(0));
                    } else {
                        scheduleListening(1000);
                    }
                }

                @Override
                public void onPartialResults(android.os.Bundle results) {}

                @Override
                public void onEvent(int eventType, android.os.Bundle params) {}
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && "STOP".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                    1,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            );
        } else {
            startForeground(1, buildNotification());
        }

        active = true;

        if (ttsReady) {
            startRecognizer();
        }

        return START_STICKY;
    }

    private Notification buildNotification() {

        if (Build.VERSION.SDK_INT >= 26) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("ZARA AI Assistant")
                    .setContentText("ZARA is listening")
                    .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                    .setOngoing(true)
                    .build();
        }

        return new Notification.Builder(this)
                .setContentTitle("ZARA AI Assistant")
                .setContentText("ZARA is listening")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

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

        if (!active || !ttsReady || recognizer == null) {
            return;
        }

        try {

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

            recognizer.startListening(intent);

        } catch (Exception e) {
            scheduleListening(2000);
        }
    }

    private void scheduleListening(long delay) {

        if (!active) return;

        handler.removeCallbacks(listenRunnable);
        handler.postDelayed(listenRunnable, delay);
    }

    private void handleCommand(String command) {

        String text = command.toLowerCase(Locale.ROOT);

        if (text.contains("hey zara") ||
                text.contains("hey jara") ||
                text.contains("হেই জারা") ||
                text.contains("হে জারা")) {

            awaitingCommand = true;

            speak("Yes Boss! বলুন, কী করতে হবে?", 800);

            return;
        }

        if (!awaitingCommand) {
            scheduleListening(1000);
            return;
        }

        awaitingCommand = false;

        if (text.contains("facebook") || text.contains("ফেসবুক")) {

            openApp("com.facebook.lite",
                    "Opening Facebook Lite, Boss.");

        } else if (text.contains("tiktok") || text.contains("টিকটক")) {

            openApp("com.zhiliaoapp.musically",
                    "Opening TikTok, Boss.");

        } else if (text.contains("youtube") || text.contains("ইউটিউব")) {

            openApp("com.google.android.youtube",
                    "Opening YouTube, Boss.");

        } else if (text.contains("time") ||
                text.contains("সময়") ||
                text.contains("সময়") ||
                text.contains("কটা বাজে")) {

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                    );

            speak("Boss, এখন সময় " +
                    sdf.format(new java.util.Date()), 1000);

        } else {

            speak("Sorry Boss, বুঝতে পারিনি। আবার বলুন।", 1000);
        }
    }

    private void openApp(String packageName, String message) {

        PackageManager pm = getPackageManager();

        Intent launchIntent =
                pm.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(launchIntent);
                speak(message, 1000);
            } catch (Exception e) {
                speak("Boss, app খুলতে পারছি না।", 1000);
            }

        } else {
            speak("Boss, app খুঁজে পাচ্ছি না।", 1000);
        }
    }

    private void speak(String message, long delay) {

        if (tts != null && ttsReady) {

            handler.removeCallbacks(listenRunnable);

            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "ZARA_SPEECH"
            );

            handler.postDelayed(() -> {
                if (active) {
                    startRecognizer();
                }
            }, delay + 2500);

        } else {
            scheduleListening(delay);
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
