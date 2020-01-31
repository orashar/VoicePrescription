package com.example.android.voiceprescription;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecog;

    GoogleSignInClient mGoogleSignInClient;

    private String FOR_PARTICULAR_PATIENT = "false";
    private boolean IS_MIC_ON = false;

    private Intent intentToTimeline;

    private TextView nameTV;
    private ImageView photoIV;
    private ActionBar actionbar;
    private TextView assistExistingPatienttv;
    private TextView addNewPatienttv;
    private TextView timeLinetv;
    private FloatingActionButton micFb;

    private String[] doctorDetails = {
            "SuperMario", "All rounder", "+91 9876543210", "supermario@gmail.com"
    };

    private String[] patientDetails = {
            "", "", "", "", ""
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_layout);

        nameTV = findViewById(R.id.name);
        photoIV = findViewById(R.id.photo);
        assistExistingPatienttv = findViewById(R.id.assist_patient_text_box);
        addNewPatienttv = findViewById(R.id.new_patient_text_box);
        timeLinetv = findViewById(R.id.timeline_text_box);
        micFb = findViewById(R.id.mic_home_fb);
        actionbar = getSupportActionBar();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(HomePageActivity.this);
        if (acct != null) {

            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            Uri personPhoto = acct.getPhotoUrl();

            doctorDetails[0] = personName;
            doctorDetails[3] = personEmail;

            nameTV.setText(personName);
            actionbar.setTitle(personEmail);
            if (personPhoto != null) {
                Glide.with(this).load(personPhoto).into(photoIV);
            }
        }

        addNewPatienttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToNew();
            }
        });

        assistExistingPatienttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToexisting();
            }
        });

        timeLinetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToTimeline();
            }
        });

        micFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(HomePageActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(HomePageActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {

                    } else {

                        ActivityCompat.requestPermissions(HomePageActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                    }
                } else {
                    if (IS_MIC_ON) {


                        micFb.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_on));
                        IS_MIC_ON = false;
                        speechRecognizer.cancel();
                    } else {
                        IS_MIC_ON = true;
                        micFb.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_off1));

                        initializeSpeechRecognizer();
                        intentRecog = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intentRecog.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intentRecog.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                        intentRecog.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                        speechRecognizer.startListening(intentRecog);
                    }
                }

            }
        });

        File appDirectory = new File(Environment.getExternalStorageDirectory() , "/VoicePrescription");
        if(!appDirectory.exists()){
            appDirectory.mkdir();
        }

    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomePageActivity.this,"Successfully signed out",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HomePageActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    private void intentToexisting(){
        Intent intentToSearchPatient = new Intent(HomePageActivity.this, GatherPatientIdActivity.class);
        intentToSearchPatient.putExtra("DETAILS_OF_DOCTOR", doctorDetails);
        startActivity(intentToSearchPatient);
    }

    private void intentToNew(){
        Intent voiceToDataIntent = new Intent(HomePageActivity.this, GatherPatientInfoActivity.class);
        voiceToDataIntent.putExtra("DETAILS_OF_PATIENT", patientDetails);
        voiceToDataIntent.putExtra("DETAILS_OF_DOCTOR", doctorDetails);
        startActivity(voiceToDataIntent);
    }

    private void intentToTimeline(){
        intentToTimeline = new Intent(HomePageActivity.this, TimelineActivity.class);
        intentToTimeline.putExtra("FOR_PARTICULAR_PATIENT", FOR_PARTICULAR_PATIENT);
        intentToTimeline.putExtra("ID_OF_PATIENT", "0");
        startActivity(intentToTimeline);
    }

    private void initializeSpeechRecognizer(){
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Toast.makeText(HomePageActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
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

                }

                @Override
                public void onError(int error) {
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

    private void processDataResult(String resultText) {
        String purposeData = purposeOfData(resultText.toLowerCase());
        if(purposeData != null) {
            if (purposeData.equals("new")) {
                intentToNew();
            } else if (purposeData.equals("old")) {
                intentToexisting();
            } else if (purposeData.equals("timeline")) {
                intentToTimeline();
            }
        }  else {
            speechRecognizer.stopListening();
            speechRecognizer.startListening(intentRecog);
        }
    }

    private String purposeOfData(String msg){
        if(msg.contains("new")){
            return "new";
        } else if(msg.contains("search") || msg.contains("existing")){
            return "old";
        } else if(msg.contains("record") || msg.contains("timeline") || msg.contains("history")){
            return "timeline";
        }  else{
            return null;
        }
    }

    private void displayPartialResult(String partMessage){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        speechRecognizer.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        micFb.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_on));
        initializeSpeechRecognizer();
    }
}