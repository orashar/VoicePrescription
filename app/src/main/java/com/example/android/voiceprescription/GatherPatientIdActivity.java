package com.example.android.voiceprescription;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class GatherPatientIdActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;

    private String patientId;
    private EditText patientIdet;
    private Button searchBtn;
    private Button editBtn;
    private TextView mictv;

    private boolean isListening = false;

    private PrescriptionDBAdapter prescriptionDBAdapter;
    List<FoundPatient> foundPatientDetails;
    private String[] patientDetails = {
            "", "", "", "", "",
    };
    private String[] detailsDoctor = {
            "", "", "", ""
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_id_layout);

        patientIdet = findViewById(R.id.patient_id_et);
        searchBtn = findViewById(R.id.search_btn);
        editBtn = findViewById(R.id.patient_edit_manually);
        mictv = findViewById(R.id.text_view_for_partial_result);


        Intent intentForDocDetails = getIntent();
        detailsDoctor = intentForDocDetails.getStringArrayExtra("DETAILS_OF_DOCTOR");

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patientId = patientIdet.getText().toString();
                if(!patientId.equals("")) {

                    prescriptionDBAdapter = PrescriptionDBAdapter.getPrescriptionDBAdapterInstance(GatherPatientIdActivity.this);

                    foundPatientDetails = prescriptionDBAdapter.getPatientDetailsById(patientId);
                    if(foundPatientDetails.size() > 0){
                        onPatientRecordFound(foundPatientDetails.size());
                    } else{
                        onNoPatientRecordFound();
                    }
                } else{
                    Toast toast = Toast.makeText(GatherPatientIdActivity.this, "Patient Id can not be empty!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isListening){
                    speechRecognizer.cancel();
                    isListening = false;
                    mictv.setText("Tap To Talk");
                }
            }
        });

        mictv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isListening) {
                    startJexi();
                }
            }
        });

        initializeSpeechRecognizer();

    }

    private void startJexi(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {

                ActivityCompat.requestPermissions(this,
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
                    Toast.makeText(GatherPatientIdActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
                    mictv.setText("Listening...");
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
        dataMsg = dataMsg.toLowerCase().replaceAll(" ", "");
        patientIdet.setText(dataMsg);
        speechRecognizer.cancel();
    }

    private void displayPartialResult(String partMessage){
        mictv.setText(partMessage.toLowerCase().replaceAll(" ", ""));
    }

    private void onPatientRecordFound(int recordsFound){
        patientDetails[0] = foundPatientDetails.get(0).getPatientId();
        patientDetails[1] = foundPatientDetails.get(0).getPatientName();
        patientDetails[2] = foundPatientDetails.get(0).getPatientAge();
        patientDetails[3] = foundPatientDetails.get(0).getPatientGender();
        patientDetails[4] = ""+recordsFound;
        Intent intentToFoundPatient = new Intent(this, FoundPatientDetailsActivity.class);
        intentToFoundPatient.putExtra("DETAILS_OF_PATIENT", patientDetails);
        intentToFoundPatient.putExtra("DETAILS_OF_DOCTOR", detailsDoctor);
        intentToFoundPatient.putExtra("EMAIL_PATIENT", foundPatientDetails.get(0).getPatientEmail());
        startActivity(intentToFoundPatient);
    }

    private void onNoPatientRecordFound(){
        Toast toast = Toast.makeText(this, "No record found!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
