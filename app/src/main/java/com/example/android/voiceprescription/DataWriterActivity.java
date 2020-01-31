package com.example.android.voiceprescription;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class DataWriterActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextView keywordHeading;
    private EditText dataEditText;
    private Button doneBtn;
    private Button editManuallyBtn;
    private TextView micBtn;

    private boolean isListening = false;

    private Intent intentRecognizer;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;

    private String dataBodyString = "";
    private String headingForKeyword;
    private String priorData;


    private String[] dataGathered = {
            "Keyword", "dataForKeyword"
    };

    private String[] resistantKeywords = {
            "done", "that's it"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_writer_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int windowW = dm.widthPixels;
        int windowH= dm.heightPixels;

        getWindow().setLayout((int) (windowW*0.8), (int) (windowH*0.6));

        doneBtn = findViewById(R.id.back_btn);
        keywordHeading = findViewById(R.id.text_view_keyword_heading);
        dataEditText = findViewById(R.id.edit_text_data_writer);
        editManuallyBtn = findViewById(R.id.data_edit_manually);
        micBtn = findViewById(R.id.mic_btn);

        Intent intentForHeading = getIntent();
        headingForKeyword = intentForHeading.getStringExtra("KEYWORD_NAME_FOR_HEADING");
        priorData = intentForHeading.getStringExtra("PRIOR_DATA_FOR_EDIT_TEXT");

        keywordHeading.setText(headingForKeyword);
        if(priorData != null) {
            String totalPriorData = dataEditText.getText().toString()+priorData;
            dataBodyString = totalPriorData;
            dataEditText.setText(priorData);
        }
        dataGathered[0] = headingForKeyword;


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBacktoVoiceActivity();
            }
        });

        editManuallyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isListening){
                    isListening = false;
                    speechRecognizer.cancel();
                    micBtn.setText("Tap To Talk");
                }
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isListening) {
                    initializeSpeechRecognizer();
                    startJexi();
                }
            }
        });
        initializeTextToSpeech();
    }

    private void startJexi(){

        if (ContextCompat.checkSelfPermission(DataWriterActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(DataWriterActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {

                ActivityCompat.requestPermissions(DataWriterActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

            }
        } else {
            intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            try {
                speechRecognizer.startListening(intentRecognizer);
            } catch (NullPointerException e) {
                Log.v("ErrorWas", e.getMessage());
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeSpeechRecognizer(){
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    Toast.makeText(DataWriterActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
                    micBtn.setText("Listening...");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    Log.e("ErrorDetectingText:", ""+error);
                    speechRecognizer.cancel();
                    speechRecognizer.startListening(intentRecognizer);
                }

                @Override
                public void onResults(Bundle results) {
                    List<String> resultArr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processDataResult(resultArr.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    List<String> partArr = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    displayPartialResult(partArr.get(0));
                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void processDataResult(String dataMsg){
        if(!dataBodyString.equals("")) {
            dataBodyString += "\n- ";
        } else{
            dataBodyString += "- ";
        }
        boolean resistances = checkForResistances(dataMsg);
        if(resistances == false) {
            if (dataMsg != null && !dataMsg.contains("clear") && !dataMsg.contains("reset") && !dataMsg.contains("delete")) {
                dataBodyString += dataMsg + ";";
                dataEditText.setText(dataBodyString);

                try {
                    speechRecognizer.startListening(intentRecognizer);
                } catch (NullPointerException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if((dataMsg.contains("clear") || dataMsg.contains("reset") || dataMsg.contains("delete")) && !dataMsg.contains("exit")){
                dataBodyString = "";
                dataEditText.setText(dataBodyString);
                try {
                    speechRecognizer.startListening(intentRecognizer);
                } catch (NullPointerException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else{
                dataBodyString = "";
                dataEditText.setText(dataBodyString);
                goBacktoVoiceActivity();
            }
        } else{

            goBacktoVoiceActivity();
        }
    }

    private void displayPartialResult(String partMessage){
        micBtn.setText(partMessage);
    }

    private void goBacktoVoiceActivity(){
        try {
            speechRecognizer.destroy();
        } catch(Exception e){
            Log.e("ExceptionDestroyingSR", e.getMessage());
        }

        dataGathered[1] = dataEditText.getText().toString();
        Log.v("GatheredData:", dataGathered[0]+"************"+dataGathered[1]);
        Intent intentToGoBackOnVoice = new Intent();
        intentToGoBackOnVoice.putExtra("DATA_FROM_DATA_WRITER_WINDOW", dataGathered);
        setResult(RESULT_OK, intentToGoBackOnVoice);
        finish();
    }

    private boolean checkForResistances(String stringToCheck){
        for(String resistanceKeyword: resistantKeywords){
            if(stringToCheck.toLowerCase().indexOf(resistanceKeyword) != -1){
                return true;
            }
        }
        return false;
    }

    private void initializeTextToSpeech() {
        if (tts == null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (tts.getEngines().size() == 0) {
                        Toast.makeText(DataWriterActivity.this, getString(R.string.tts_no_engines), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        tts.setLanguage(Locale.US);
                    }
                }
            });
        }
    }

    private void clearData(int idOfKeyword){
        if(idOfKeyword == 4){
        } else if(idOfKeyword == 5){
        } else if(idOfKeyword == 2){
        } else if(idOfKeyword == 3){
        } else{
            Toast.makeText(this, "Unable to process request.", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String message){
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }

    @Override
    protected void onDestroy() {
        if(tts != null){
            tts.shutdown();
        }
        super.onDestroy();
    }
}
