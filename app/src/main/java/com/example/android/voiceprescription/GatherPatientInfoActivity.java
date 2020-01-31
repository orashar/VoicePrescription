package com.example.android.voiceprescription;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class GatherPatientInfoActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private AlertDialog.Builder alertBuilder;
    private AlertDialog.Builder alertBuilder2;

    private Button doneBtn;
    private Button editBtn;
    private TextView micBtn;
    private TextView partialResulttv;
    private EditText patientNameet;
    private EditText patientAgeet;
    private EditText patientGenderet;
    private EditText patientEmailet;

    private String[] possibleAges = {
            "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50",
            "51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74","75",
            "76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99","100",
            "101","102","103","104","105","106","107","108","109","110","111","112","113","114","115","116","117","118","119","120","121","122","123","124","125",
            "126","127","128","129","130","131","132","133","134","135","136","137","138","139","140","141","142","143","144","145","146","147","148","149","150"
    };

    private String[] detailsPatient = {
            "", "",
            "", "",""
    };

    private String[] priorPatientDetails;
    private String[] detailsDoctor = {
            "", "All rounder Speacilist",
            "+91 9876543210", ""
    };

    private String currentField = "";

    private SpeechRecognizer speechRecognizer;
    private Intent intentRecog;
    private TextToSpeech tts;

    private boolean isListening = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_info_layout);



        Intent intentForDoc = getIntent();
        detailsDoctor = intentForDoc.getStringArrayExtra("DETAILS_OF_DOCTOR");
        priorPatientDetails = intentForDoc.getStringArrayExtra("DETAILS_OF_PATIENT");


        doneBtn = findViewById(R.id.patient_done_btn);
        editBtn = findViewById(R.id.patient_edit_manually);
        micBtn = findViewById(R.id.text_view_for_partial_result);

        patientNameet = findViewById(R.id.patient_name_info_tv);
        patientAgeet = findViewById(R.id.patient_age_info_tv);
        patientGenderet = findViewById(R.id.patient_gender_info_tv);
        patientEmailet = findViewById(R.id.patient_email_info_tv);
        partialResulttv = findViewById(R.id.text_view_for_partial_result);

        patientNameet.setText(priorPatientDetails[0]);
        patientAgeet.setText(priorPatientDetails[1]);
        patientGenderet.setText(priorPatientDetails[2]);
        patientEmailet.setText(priorPatientDetails[3]);

        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.incomplete_info_dialog_title);
        alertBuilder.setMessage(R.string.incomplete_info_dialog_message);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder2 = new AlertDialog.Builder(this);
        alertBuilder2.setTitle(R.string.invalid_info_dialog_title);
        alertBuilder2.setMessage(R.string.invalid_info_dialog_message);
        alertBuilder2.setIcon(R.mipmap.ic_launcher);
        alertBuilder2.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isComplete = isInfoComplete();
                if(isComplete) {
                    boolean isValid = isInfoValid();
                    if(isValid) {
                        goToVoiceActivity();
                    } else{
                        speechRecognizer.cancel();
                        currentField = "";
                        alertBuilder2.create().show();
                    }
                } else{
                    alertBuilder.create().show();
                }
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isListening = true){
                    speechRecognizer.cancel();
                    currentField = "";
                    micBtn.setText("Tap To Talk");
                }
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(GatherPatientInfoActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(GatherPatientInfoActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {

                    } else {

                        ActivityCompat.requestPermissions(GatherPatientInfoActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                    }
                } else {

                    intentRecog = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intentRecog.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intentRecog.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                    intentRecog.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                    speechRecognizer.startListening(intentRecog);
                }
            }
        });


        initializeTextToSpeech();
        initializeSpeechRecognizer();


    }

    private boolean isInfoValid(){
        gatherPatientDetails();
        if (getAge(detailsPatient[2]).equals(detailsPatient[2])) {
            return true;
        } else{
            return false;
        }
    }

    private boolean isInfoComplete(){
        if(patientNameet.getText().toString().equals("") || patientEmailet.getText().toString().equals("") || patientAgeet.getText().toString().equals("") || patientGenderet.getText().toString().equals("")){
            return false;
        } else{
            return true;
        }
    }

    private void initializeSpeechRecognizer(){
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    micBtn.setText("Listening...");
                    if (currentField.equals("")) {
                        Toast.makeText(GatherPatientInfoActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(GatherPatientInfoActivity.this, "Listening for "+currentField, Toast.LENGTH_SHORT).show();

                    }
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
                    speechRecognizer.startListening(intentRecog);

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

    private void processDataResult(String messageString){

        if(currentField.equals("")) {
            String dataPurpose = whatDataIsFor(messageString);
            if (dataPurpose.equals("name")) {
                currentField = "name";
            } else if (dataPurpose.equals("age")) {
                currentField = "age";
            } else if (dataPurpose.equals("gender")) {
                currentField = "gender";
            } else if (dataPurpose.equals("email")) {
                currentField = "email";
            } else {
                Toast.makeText(this, "Sorry! Unable to understand", Toast.LENGTH_SHORT).show();
            }
        } else{
            if(currentField.equals("name")){
                patientNameet.setText(messageString);
                currentField = "";
            } else if(currentField.equals("age")){
                String writableAge = getAge(messageString);
                if(writableAge.equals(messageString)){
                    patientAgeet.setText(writableAge);
                    currentField = "";
                }else{
                    Toast.makeText(this, "Invalid Age!", Toast.LENGTH_SHORT).show();
                }
            } else if(currentField.equals("gender")){
                String writableGender = getGender(messageString);
                if(writableGender.equals("Male")) {
                    patientGenderet.setText(writableGender);
                    currentField = "";
                } else if(writableGender.equals("Female")) {
                    patientGenderet.setText(writableGender);
                    currentField = "";
                } else if(writableGender.toLowerCase().equals("other")) {
                    patientGenderet.setText(writableGender);
                    currentField = "";
                } else{
                    Toast.makeText(this, "Invalid Gender!", Toast.LENGTH_SHORT).show();
                }
            } else if(currentField.equals("email")){
                String writableEmail = getEmail(messageString);
                patientEmailet.setText(writableEmail);
                currentField = "";
            }
        }

        speechRecognizer.startListening(intentRecog);
    }

    private String getEmail(String emailString){
        return emailString.toLowerCase().replace(" ", "");
    }

    private String getGender(String genderString){
        if((genderString.toLowerCase().contains("mail") || genderString.toLowerCase().contains("male")) && (!genderString.toLowerCase().contains("female"))){
            Log.v("RECOGNIZEDGENDRE:", genderString+"************************");
            return "Male";
        } else if(genderString.toLowerCase().contains("fee mail") || genderString.toLowerCase().contains("female")){
            return "Female";
        } else if(genderString.toLowerCase().contains("other") || genderString.toLowerCase().contains("others")){
            return "Others";
        } else {
            return genderString;
        }
    }

    private String getAge(String ageString){
        if(Arrays.asList(possibleAges).contains(ageString)){
            return ageString;
        }else {
            return "age";
        }
    }

    private void displayPartialResult(String partMessage) {
        if (currentField.equals("email")) {
            partialResulttv.setText(partMessage.replaceAll(" ", ""));
        } else {
            partialResulttv.setText(partMessage.replaceAll("xender", "gender"));
        }
    }

    private String whatDataIsFor(String msgToCheck){
        if(msgToCheck.toLowerCase().contains("name")){
            return "name";
        } else if(msgToCheck.toLowerCase().contains("age")){
            return "age";
        }else if(msgToCheck.toLowerCase().replaceAll("xender", "gender").contains("gender")){
            return "gender";
        }else if(msgToCheck.toLowerCase().contains("email") || msgToCheck.toLowerCase().contains("mail")){
            return "email";
        }
        return "";
    }

    private void initializeTextToSpeech(){
        if(tts == null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (tts.getEngines().size() == 0) {
                        Toast.makeText(GatherPatientInfoActivity.this, getString(R.string.tts_no_engines), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        tts.setLanguage(Locale.US);
                    }

                }
            });
        }
    }

    private void speak(String message){
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    private void gatherPatientDetails(){

        detailsPatient[1] = patientNameet.getText().toString();
        detailsPatient[2] = patientAgeet.getText().toString();
        detailsPatient[3] = patientGenderet.getText().toString();
        detailsPatient[4] = patientEmailet.getText().toString().toLowerCase();

    }

    private void goToVoiceActivity(){
        speechRecognizer.destroy();
        if(tts!=null) {
            tts.shutdown();
        }
        gatherPatientDetails();
        Intent intentToGoVoice = new Intent(GatherPatientInfoActivity.this, VoiceToDisplayActivity.class);
        intentToGoVoice.putExtra("DETAILS_OF_PATIENT", detailsPatient);
        intentToGoVoice.putExtra("DETAILS_OF_DOCTOR", detailsDoctor);
        intentToGoVoice.putExtra("IS_PATIENT_NEW", "true");
        setResult(RESULT_OK, intentToGoVoice);
        startActivity(intentToGoVoice);
    }

    @Override
    protected void onPause() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        speechRecognizer.destroy();
        super.onPause();
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
