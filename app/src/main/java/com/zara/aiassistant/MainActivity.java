package com.zara.aiassistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView status;
    EditText commandInput;

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
        status.setText("Hello! আমি ZARA। তোমার নির্দেশের অপেক্ষায় আছি।");
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        commandInput = new EditText(this);
        commandInput.setHint("কমান্ড লিখুন / Enter command");

        Button runButton = new Button(this);
        runButton.setText("RUN COMMAND");

        layout.addView(title);
        layout.addView(status);
        layout.addView(commandInput);
        layout.addView(runButton);

        setContentView(layout);

        runButton.setOnClickListener(v -> {

            String command = commandInput.getText()
                .toString().toLowerCase(Locale.ROOT).trim();

            if (!command.isEmpty()) {
                processCommand(command);
            }
        });
    }

    private void processCommand(String command) {

        if (command.contains("chrome")) {

            status.setText("Chrome খুলছি...");
            openApp("com.android.chrome");

        }

        else if (command.contains("youtube")) {

            status.setText("YouTube খুলছি...");
            openApp("com.google.android.youtube");

        }

        else if (command.contains("settings") ||
                 command.contains("সেটিংস")) {

            status.setText("Settings খুলছি...");
            startActivity(new Intent(Settings.ACTION_SETTINGS));

        }

        else if (command.contains("wifi") ||
                 command.contains("ওয়াইফাই")) {

            status.setText("WiFi Settings খুলছি...");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

        }

        else if (command.contains("time") ||
                 command.contains("সময়")) {

            String time = new SimpleDateFormat(
                "hh:mm a", Locale.getDefault()
            ).format(new Date());

            status.setText("এখন সময় " + time);

        }

        else if (command.contains("hello") ||
                 command.contains("হ্যালো")) {

            status.setText("হ্যালো! আমি ZARA AI।");

        }

        else if (command.contains("stop") ||
                 command.contains("বন্ধ")) {

            status.setText("ঠিক আছে।");

        }

        else {

            status.setText(
                "দুঃখিত! এই কমান্ডটি এখনো শিখিনি।"
            );
        }

        commandInput.setText("");
    }

    private void openApp(String packageName) {

        Intent intent = getPackageManager()
            .getLaunchIntentForPackage(packageName);

        if (intent != null) {

            startActivity(intent);

        } else {

            Toast.makeText(this,
                "অ্যাপটি পাওয়া যায়নি",
                Toast.LENGTH_SHORT).show();
        }
    }
}
