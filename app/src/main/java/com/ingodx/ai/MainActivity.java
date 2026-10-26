
package com.ingodx.ai;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView status, answer;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;

    private final int AUDIO_PERMISSION = 100;
    private boolean bengali = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(8, 11, 22));
        getWindow().setNavigationBarColor(Color.rgb(8, 11, 22));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 35, 28, 25);
        root.setBackgroundColor(Color.rgb(8, 11, 22));

        TextView logo = new TextView(this);
        logo.setText("✦ INGODX AI");
        logo.setTextColor(Color.rgb(84, 231, 255));
        logo.setTextSize(30);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("YOUR VOICE. YOUR AI. YOUR CONTROL.");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(11);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 12, 0, 35);
        root.addView(subtitle);

        TextView mic = new TextView(this);
        mic.setText("🎙️");
        mic.setTextSize(65);
        mic.setGravity(Gravity.CENTER);
        root.addView(mic);

        status = new TextView(this);
        status.setText("INGODX AI is ready");
        status.setTextColor(Color.rgb(84, 231, 255));
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 20, 0, 12);
        root.addView(status);

        answer = new TextView(this);
        answer.setText("Tap the microphone and speak.");
        answer.setTextColor(Color.WHITE);
        answer.setTextSize(16);
        answer.setGravity(Gravity.CENTER);
        answer.setPadding(8, 12, 8, 22);
        root.addView(answer);

        Button voiceButton = new Button(this);
        voiceButton.setText("🎙  SPEAK TO AI");
        root.addView(voiceButton);

        Button languageButton = new Button(this);
        languageButton.setText("🌐 বাংলা / English");
        root.addView(languageButton);

        Button webButton = new Button(this);
        webButton.setText("🔎 Web Search");
        root.addView(webButton);

        setContentView(root);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(bengali
                    ? new Locale("bn", "IN")
                    : Locale.US);
            }
        });

        voiceButton.setOnClickListener(v -> startListening());

        languageButton.setOnClickListener(v -> {
            bengali = !bengali;
            languageButton.setText(
                bengali ? "🌐 বাংলা / English" : "🌐 English / বাংলা"
            );

            if (tts != null) {
                tts.setLanguage(
                    bengali ? new Locale("bn", "IN") : Locale.US
                );
            }

            status.setText(bengali
                ? "বাংলা মোড চালু"
                : "English mode enabled");
        });

        webButton.setOnClickListener(v ->
            openWebSearch("AI assistant"));

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech recognition unavailable");
        }
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_PERMISSION
            );
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("Speech recognition is unavailable");
            return;
        }

        if (recognizer != null) {
            recognizer.destroy();
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                status.setText("🎙 Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {
                status.setText("I am listening");
            }

            @Override
            public void onEndOfSpeech() {
                status.setText("Processing...");
            }

            @Override
            public void onError(int error) {
                status.setText("Could not hear you. Try again.");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                    results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    );

                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    answer.setText("You said: " + command);
                    handleCommand(command);
                }

                status.setText("INGODX AI is ready");
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onRmsChanged(float rmsdB) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            bengali ? "bn-IN" : "en-IN"
        );
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizer.startListening(intent);
    }

    private void handleCommand(String command) {
        String text = command.toLowerCase(Locale.ROOT);

        if (text.contains("youtube")) {
            speak("Opening YouTube");
            openApp("com.google.android.youtube");
        } else if (text.contains("chrome")) {
            speak("Opening Chrome");
            openApp("com.android.chrome");
        } else if (text.contains("time") || text.contains("সময়")
                || text.contains("কটা বাজে")) {
            String time = new java.text.SimpleDateFormat(
                "hh:mm a", Locale.getDefault()
            ).format(new java.util.Date());

            speak("The time is " + time);
        } else if (text.contains("search") || text.contains("খোঁজ")
                || text.contains("সার্চ")) {
            openWebSearch(command);
            speak("Opening web search");
        } else if (text.contains("hello") || text.contains("হ্যালো")) {
            speak(bengali
                ? "নমস্কার! আমি INGODX AI। কীভাবে সাহায্য করব?"
                : "Hello! I am INGODX AI. How can I help?");
        } else {
            speak(bengali
                ? "আমি তোমার কথা শুনেছি। এই কমান্ডের সুবিধা পরে যোগ করব।"
                : "I heard you. Support for this command will be added later.");
        }
    }

    private void openApp(String packageName) {
        Intent launch = getPackageManager()
            .getLaunchIntentForPackage(packageName);

        if (launch != null) {
            startActivity(launch);
        } else {
            speak("This app is not installed");
        }
    }

    private void openWebSearch(String query) {
        try {
            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                android.net.Uri.parse(
                    "https://www.google.com/search?q="
                    + android.net.Uri.encode(query)
                )
            );
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                this, "No browser available", Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void speak(String message) {
        status.setText(message);

        if (tts != null) {
            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "INGODX_REPLY"
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        );

        if (requestCode == AUDIO_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else if (requestCode == AUDIO_PERMISSION) {
            status.setText("Microphone permission is required");
        }
    }

    @Override
    protected void onDestroy() {
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
