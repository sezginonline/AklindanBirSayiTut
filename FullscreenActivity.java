package com.iyiapp.aklindan.bir.sayi.tut;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class FullscreenActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final Handler mHideHandler = new Handler();
    private TextToSpeech textToSpeech;
    private ImageButton imageButton;
    private TextView fullscreen_content;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    };
    private int myInt;

    private void setMyInt() {
        Random r = new Random();
        int low = 1;
        int high = 100;
        this.myInt = r.nextInt(high - low) + low;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);


        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());

        final InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdClosed() {
            }
        });
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        interstitialAd.loadAd(adRequest);

        this.setMyInt();

        fullscreen_content = (TextView) findViewById(R.id.fullscreen_content);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen(getString(R.string.speech_prompt));
            }
        });
        speak(getString(R.string.speech_prompt), true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (data == null) {
                    speak(getString(R.string.no_voice), false);
                }
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String answer = result.get(0);
                    if (answer.equals("bir")) {
                        answer = "1";
                    }
                    fullscreen_content.setText(answer);

                    ImageView updown = (ImageView) findViewById(R.id.updown);

                    try {
                        Integer theInt = Integer.parseInt(answer);
                        if (theInt > myInt) {
                            speak(answer + getPostfix(theInt) + " " + getString(R.string.go_lower), true);
                            updown.setImageResource(R.drawable.down);
                        }
                        if (theInt < myInt) {
                            speak(answer + getPostfix(theInt) + " " + getString(R.string.go_higher), true);
                            updown.setImageResource(R.drawable.up);
                        }
                        if (theInt == myInt) {
                            speak(answer + " " + getString(R.string.success), true);
                            updown.setImageResource(0);
                            this.setMyInt();
                        }
                    } catch (Exception e) {
                        speak(answer + " " + getString(R.string.no_understand), true);
                    }
                }
                break;
            }
        }
    }

    private void listen(String txt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("tr-TR"));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, txt);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(final String txt, final boolean listen) {
        imageButton.setClickable(false);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("tr-TR"));
                    spk(txt);

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            imageButton.setClickable(true);
                            if (listen) {
                                listen(txt);
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }
                    });

                }
            }

            private void spk(String txt) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(txt);
                } else {
                    ttsUnder20(txt);
                }
            }

            @SuppressWarnings("deprecation")
            private void ttsUnder20(String text) {
                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            private void ttsGreater21(String text) {
                String utteranceId = this.hashCode() + "";
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            }
        });
    }

    private String getPostfix(Integer no) {
        if (no < 1 || no > 100) return " sayısından";
        if (no % 10 == 1) return "'den";
        if (no % 10 == 2) return "'den";
        if (no % 10 == 3) return "'ten";
        if (no % 10 == 4) return "'ten";
        if (no % 10 == 5) return "'ten";
        if (no % 10 == 6) return "'dan";
        if (no % 10 == 7) return "'den";
        if (no % 10 == 8) return "'den";
        if (no % 10 == 9) return "'dan";
        if (no == 0) return "'dan";
        if (no == 10) return "'dan";
        if (no == 20) return "'den";
        if (no == 30) return "'dan";
        if (no == 40) return "'tan";
        if (no == 50) return "'den";
        if (no == 60) return "'tan";
        if (no == 70) return "'ten";
        if (no == 80) return "'den";
        if (no == 90) return "'dan";
        if (no == 100) return "'den";
        return " sayısından";
    }

}
