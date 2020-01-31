package com.example.android.voiceprescription;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoiceToDisplayActivity extends AppCompatActivity {

    private PrescriptionDBAdapter prescriptionDBAdapter;

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecog;

    private AlertDialog.Builder alertBuilder;
    private AlertDialog alert;


    private String[] keywords = {
            "prescription", "symptom",
            "advice", "diagnosis",
    };

    private TextView doctorNametv;
    private TextView doctorSpecialtv;
    private TextView doctorEmailtv;
    private TextView doctorPhonetv;
    private TextView patientIdtv;
    private TextView datetv;
    private TextView patientNametv;
    private TextView patientAgetv;
    private TextView patientGendertv;
    private TextView patientEmailtv;

    private String prescriptionData;
    private String symptomsData;
    private String adviceData;
    private String diagnosisData;


    private LinearLayout prescriptionll;
    private LinearLayout symptomsll;
    private LinearLayout advicell;
    private LinearLayout diagnosisll;

    private TextView micBtn;
    private Button continueBtn;
    private TextView prescriptiontv;
    private TextView prescriptionDatatv;
    private TextView symptomstv;
    private TextView symptomsDatatv;
    private TextView advicetv;
    private TextView adviceDatatv;
    private TextView diagnosistv;
    private TextView diagnosisDatatv;


    private String priorData;
    private String foundKeyword = "";
    private int whoseMenu;
    private String[] dataString;
    private String[] patientDetails = {
        "Mr. Patient", "age",
        "gender", "patient@email.com",""
    };
    private String[] priorPatientData = {
            "Mr. Patient", "age",
            "gender", "patient@email.com"
    };
    private String[] doctorDetails = {
            "Mr. Super Mario", "All rounder Speacilist",
            "+91 9876543210", "supermario84@gmail.com"
    };
    private String[] dataRowForDB = {
            "", "", "", "", "", "", ""
    };


    private boolean IS_LISTENING = false;
    private String isPatientNew;
    private String currentKeyword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_to_display_layout);

        ActionBar actionBarVoice = getSupportActionBar();
        actionBarVoice.hide();

        Intent intentForDetails = getIntent();
        isPatientNew = intentForDetails.getStringExtra("IS_PATIENT_NEW");
        doctorDetails = intentForDetails.getStringArrayExtra("DETAILS_OF_DOCTOR");
        Log.v("detailsOfdoctor", doctorDetails[0]+doctorDetails[1]+doctorDetails[2]+doctorDetails[3]);
        patientDetails = intentForDetails.getStringArrayExtra("DETAILS_OF_PATIENT");
        Log.v("detalisofPatient:", patientDetails[0]+patientDetails[1]+patientDetails[2]+patientDetails[3]);


        setDoctorDetails();
        setPatientDetails();

        fetchTextViews();

        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.alert_dialog_title);
        alertBuilder.setMessage(R.string.alert_dialog_message);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                createPdfOfData();
            }
        });
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        micBtn = findViewById(R.id.mic_btn);
        continueBtn=findViewById(R.id.continue_btn);


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert = alertBuilder.create();
                alert.show();
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(VoiceToDisplayActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(VoiceToDisplayActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {

                    } else {

                        ActivityCompat.requestPermissions(VoiceToDisplayActivity.this,
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

    private void initializeSpeechRecognizer(){
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    IS_LISTENING = true;
                    Toast.makeText(VoiceToDisplayActivity.this, "listening..", Toast.LENGTH_SHORT).show();
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
                    micBtn.setText("Listening...");
                }

                @Override
                public void onError(int error) {
                    Log.e("ErrorDetectingText:", ""+error);
                    micBtn.setText("Listening...");
                    speechRecognizer.cancel();
                    speechRecognizer.startListening(intentRecog);
                }

                @Override
                public void onResults(Bundle results) {
                    List<String> resultArr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(resultArr.get(0));
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

    private void processResult(String resultText) {


        resultText = resultText.toLowerCase();

        foundKeyword = searchForKeyword(resultText);
        if(!foundKeyword.equals("") && !foundKeyword.contains("clear")){
            micBtn.setText(foundKeyword);
            speechRecognizer.cancel();

            /**
             * finding prior data of the keyword
             */

            setPriorData(foundKeyword);
            startDataWriterIntent(foundKeyword, priorData);
        } else if(!foundKeyword.equals("") && foundKeyword.contains("clear")){
            if(foundKeyword.contains(keywords[0])){
                clearData(4);
            } else if(foundKeyword.contains(keywords[1])){
                clearData(5);
            } else if(foundKeyword.contains(keywords[2])){
                clearData(2);
            } else if(foundKeyword.contains(keywords[3])){
                clearData(3);
            } else {
                Toast.makeText(this, "Unable to understand.", Toast.LENGTH_SHORT).show();
                micBtn.setText("Tap To Talk.");
                speechRecognizer.startListening(intentRecog);
            }
        }
        else{
            micBtn.setText("Listening...");
            speechRecognizer.cancel();
            speechRecognizer.startListening(intentRecog);
        }

    }

    private void setPriorData(String ParentKeyword){
        if(ParentKeyword.equals(keywords[0])){
            priorData = prescriptionDatatv.getText().toString();
        } else if(ParentKeyword.equals(keywords[1])){
            priorData = symptomsDatatv.getText().toString();
        } else if(ParentKeyword.equals(keywords[2])){
            priorData = adviceDatatv.getText().toString();
        } else if(ParentKeyword.equals(keywords[3])){
            priorData = diagnosisDatatv.getText().toString();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && data != null){
            if(resultCode == RESULT_OK){
                Log.v("GotBachOnVoice", "***********************************************");
                try{
                    dataString = data.getStringArrayExtra("DATA_FROM_DATA_WRITER_WINDOW");
                    setDataToDeservingTV(dataString);
                    Toast.makeText(VoiceToDisplayActivity.this, dataString[1], Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e){
                    Log.e("NullDataReceived", e.getMessage());
                }
                //patientNametv.setText(dataString.toString());
            }
            else{
                patientNametv.setText("No Data Recieved");
            }
        }
        else if(requestCode == 1 && data != null){
            if(resultCode == RESULT_OK){
                patientDetails = data.getStringArrayExtra("DETAILS_OF_PATIENT");

                patientNametv.setText(patientDetails[0]);
                patientAgetv.setText(patientDetails[1]);
                patientGendertv.setText(patientDetails[2]);
            }
        }
    }

    private void setDataToDeservingTV(String[] dataToTV){

        if(dataToTV[0].equals(keywords[0])){
            prescriptionData = dataToTV[1];
            prescriptionDatatv.setText(prescriptionData);
        } else if(dataToTV[0].equals(keywords[1])){
            symptomsData = dataToTV[1];
            symptomsDatatv.setText(symptomsData);
        } else if(dataToTV[0].equals(keywords[2])){
            adviceData = dataToTV[1];
            adviceDatatv.setText(adviceData);
        } else if(dataToTV[0].equals(keywords[3])){
            diagnosisData = dataToTV[1];
            diagnosisDatatv.setText(diagnosisData);
        }

    }

    private String searchForKeyword(String dataMsg){
        String resultOfKeywordSearch = "";

        if(dataMsg.contains("clear") || dataMsg.contains("delete") || dataMsg.contains("reset")){
            for (String keyword : keywords) {
                if (dataMsg.contains(keyword)) {
                    resultOfKeywordSearch = keyword;
                    break;
                }
            }
            return resultOfKeywordSearch+" clear";
        } else {
            for (String keyword : keywords) {
                if (dataMsg.contains(keyword)) {
                    resultOfKeywordSearch = keyword;
                }
            }
            return resultOfKeywordSearch;
        }
    }

    private void displayPartialResult(String partMessage){
        if(IS_LISTENING){
            if(partMessage != null)
            micBtn.setText(partMessage);
        }
        else{
            micBtn.setText("Tap To Talk");
        }
    }

    private void initializeTextToSpeech(){
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (tts.getEngines().size() == 0 ){
                    Toast.makeText(VoiceToDisplayActivity.this, getString(R.string.tts_no_engines),Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    tts.setLanguage(Locale.US);
                    micBtn.setText("Tap To Talk");
                }

            }
        });
    }

    private void speak(String message){
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
              tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    private void createPdfOfData(){
        Toast.makeText(this, "Creating Pdf...", Toast.LENGTH_SHORT).show();
        Document document = new Document();
        String Pid = patientIdtv.getText().toString();
        String date= datetv.getText().toString();
        String mFilename = Pid+"-"+date;

        String mPath = Environment.getExternalStorageDirectory() + "/VoicePrescription/" + mFilename +".pdf";
        Paragraph testPara = new Paragraph("Voice Prescription",FontFactory.getFont(FontFactory.COURIER_BOLD,20,Font.BOLD, BaseColor.BLACK));
        Paragraph doctName = new Paragraph(doctorNametv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,15,Font.BOLD, BaseColor.BLACK));

        Paragraph speciality = new Paragraph(doctorSpecialtv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph email = new Paragraph(doctorEmailtv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph Ptid = new Paragraph("Patient ID:"+Pid+"                                                          Date:"+date,FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph Ptname = new Paragraph("Patient Name: "+patientNametv.getText().toString()+"                           Age: "+patientAgetv.getText().toString()+"\nGender: "+patientGendertv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph prescription = new Paragraph(prescriptiontv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,15,Font.BOLD, BaseColor.BLACK));
        Paragraph datapresc = new Paragraph(prescriptionDatatv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph datasymp = new Paragraph(symptomsDatatv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph dataadv = new Paragraph(adviceDatatv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph datadiag = new Paragraph(diagnosisDatatv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,10,Font.NORMAL, BaseColor.BLACK));
        Paragraph symptoms = new Paragraph(symptomstv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,15,Font.BOLD, BaseColor.BLACK));
        Paragraph advice = new Paragraph(advicetv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,15,Font.BOLD, BaseColor.BLACK));
        Paragraph diagnosis = new Paragraph(diagnosistv.getText().toString(),FontFactory.getFont(FontFactory.COURIER,15,Font.BOLD, BaseColor.BLACK));
        testPara.setAlignment(Element.ALIGN_CENTER);
        doctName.setIndentationLeft(10);
        speciality.setIndentationLeft(10);
        email.setIndentationLeft(10);
        datapresc.setSpacingAfter(50);
        datasymp.setSpacingAfter(50);
        dataadv.setSpacingAfter(50);
        datadiag.setSpacingAfter(50);

        try
        {

            PdfWriter.getInstance(document,new FileOutputStream(mPath));
            document.open();
            document.addAuthor("salik");

            document.add(testPara);
            document.add(doctName);
            document.add(speciality);
            document.add(email);


            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            document.add(Ptid);
            document.add(Ptname);

            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));


            if(!prescriptionDatatv.getText().toString().equals("")) {
                document.add(prescription);
                document.add(datapresc);
            }
            if(!symptomsDatatv.getText().toString().equals("")) {
                document.add(symptoms);
                document.add(datasymp);
            }
            if(!adviceDatatv.getText().toString().equals("")) {
                document.add(advice);
                document.add(dataadv);
            }
            if(!diagnosisDatatv.getText().toString().equals("")) {
                document.add(diagnosis);
                document.add(datadiag);
            }

            document.close();
            //Toast.makeText(this, mFilename +".pdf\nis saved to "+mPath,Toast.LENGTH_SHORT).show();

        } catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        prescriptionDBAdapter = PrescriptionDBAdapter.getPrescriptionDBAdapterInstance(this);
        insertDataInDB();
    }

    private void insertDataInDB(){
        dataRowForDB[0] = patientIdtv.getText().toString();
        dataRowForDB[1] = datetv.getText().toString();
        dataRowForDB[2] = patientNametv.getText().toString();
        dataRowForDB[3] = patientAgetv.getText().toString();
        dataRowForDB[4] = patientGendertv.getText().toString();
        dataRowForDB[5] = patientEmailtv.getText().toString();
        dataRowForDB[6] = patientIdtv.getText().toString();

        boolean insertResponse = prescriptionDBAdapter.insert(dataRowForDB);
        if(!insertResponse){
           Toast.makeText(this, "Problem inserting in storage", Toast.LENGTH_LONG).show();
        } else{
            Intent intentToShareSave = new Intent(VoiceToDisplayActivity.this, ShareAndSaveActivity.class);
            intentToShareSave.putExtra("NAME", patientNametv.getText().toString());
            intentToShareSave.putExtra("EMAIL", patientEmailtv.getText().toString());
            intentToShareSave.putExtra("DATE", datetv.getText().toString());
            intentToShareSave.putExtra("PID", patientIdtv.getText().toString());
            startActivity(intentToShareSave);
        }
    }

    /*private void gatherPatientDetails(){
        speechRecognizer.cancel();
        tts.shutdown();
        priorPatientData[0] = patientNametv.getText().toString();
        priorPatientData[1] = patientAgetv.getText().toString();
        priorPatientData[2] = patientGendertv.getText().toString();
        priorPatientData[3] = patientEmailtv.getText().toString();
        Intent patientDetailsIntent = new Intent(VoiceToDisplayActivity.this, GatherPatientInfoActivity.class);
        patientDetailsIntent.putExtra("DETAILS_OF_DOCTOR", doctorDetails);
        patientDetailsIntent.putExtra("DETAILS_OF_PATIENT", priorPatientData);
        startActivityForResult(patientDetailsIntent, 1);
    }*/

    private void setPatientDetails(){
        patientIdtv = findViewById(R.id.text_patient_id);
        datetv = findViewById(R.id.text_date);
        patientNametv = findViewById(R.id.text_patient_name);
        patientAgetv = findViewById(R.id.text_patient_age);
        patientGendertv =findViewById(R.id.text_patient_gender);
        patientEmailtv = findViewById(R.id.text_patient_email);

        String currDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        datetv.setText(currDate);

        if(isPatientNew.equals("true")){
            prescriptionDBAdapter = PrescriptionDBAdapter.getPrescriptionDBAdapterInstance(this);
            String generatedPid = patientDetails[1].toLowerCase().substring(0, 2)+prescriptionDBAdapter.returnNextIndex()+currDate.replace("-", "");
            patientIdtv.setText(generatedPid);
        } else{
            patientIdtv.setText(patientDetails[0]);
        }

        patientNametv.setText(patientDetails[1]);
        patientAgetv.setText(patientDetails[2]);
        patientGendertv.setText(patientDetails[3]);
        patientEmailtv.setText(patientDetails[4]);
    }

    private void setDoctorDetails(){

        doctorNametv = findViewById(R.id.text_doctor_name);
        doctorSpecialtv = findViewById(R.id.text_doctor_speciality);
        doctorEmailtv = findViewById(R.id.text_doctor_email);
        doctorPhonetv = findViewById(R.id.text_doctor_phone);

        doctorNametv.setText(doctorDetails[0]);
        doctorSpecialtv.setText(doctorDetails[1]);
        doctorPhonetv.setText(doctorDetails[2]);
        doctorEmailtv.setText(doctorDetails[3]);
    }

    private void startDataWriterIntent(String keyword, String dataForEditText){
        speechRecognizer.cancel();
        tts.stop();
        Intent dataWriterIntent = new Intent(VoiceToDisplayActivity.this, DataWriterActivity.class);
        dataWriterIntent.putExtra("KEYWORD_NAME_FOR_HEADING", keyword);
        dataWriterIntent.putExtra("PRIOR_DATA_FOR_EDIT_TEXT", dataForEditText);
        startActivityForResult(dataWriterIntent, 0);
    }

    private void fetchTextViews(){
        prescriptiontv = findViewById(R.id.list_item_keyword_prescription);
        prescriptionDatatv = findViewById(R.id.list_item_data_prescription);
        prescriptionll = findViewById(R.id.linear_layout_prescription);
        symptomstv = findViewById(R.id.list_item_keyword_symptoms);
        symptomsDatatv = findViewById(R.id.list_item_data_symptoms);
        symptomsll = findViewById(R.id.linear_layout_symptoms);
        advicetv = findViewById(R.id.list_item_keyword_advice);
        adviceDatatv = findViewById(R.id.list_item_data_advice);
        advicell = findViewById(R.id.linear_layout_advice);
        diagnosistv = findViewById(R.id.list_item_keyword_diagnosis);
        diagnosisDatatv = findViewById(R.id.list_item_data_diagnosis);
        diagnosisll = findViewById(R.id.linear_layout_diagnosis);

        prescriptiontv.setText(keywords[0]);
        symptomstv.setText(keywords[1]);
        advicetv.setText(keywords[2]);
        diagnosistv.setText(keywords[3]);

        registerForContextMenu(prescriptionll);
        registerForContextMenu(symptomsll);
        registerForContextMenu(advicell);
        registerForContextMenu(diagnosisll);

        prescriptionll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataWriterIntent(keywords[0], prescriptionData);
            }
        });

        symptomsll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataWriterIntent(keywords[1], symptomsData);
            }
        });

        advicell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataWriterIntent(keywords[2], adviceData);
            }
        });

        diagnosisll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataWriterIntent(keywords[3], diagnosisData);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        whoseMenu = v.getId();
        getMenuInflater().inflate(R.menu.edit_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                editData(whoseMenu%10);
                return true;

            case R.id.clear:
                clearData(whoseMenu%10);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void clearData(int idOfKeyword){
        if(idOfKeyword == 4){
            prescriptionData = "";
            prescriptionDatatv.setText(prescriptionData);
        } else if(idOfKeyword == 5){
            symptomsData = "";
            symptomsDatatv.setText(symptomsData);
        } else if(idOfKeyword == 2){
            adviceData = "";
            adviceDatatv.setText(adviceData);
        } else if(idOfKeyword == 3){
            diagnosisData = "";
            diagnosisDatatv.setText(diagnosisData);
        } else{
            Toast.makeText(this, "Unable to process request.", Toast.LENGTH_SHORT).show();
        }
    }

    private void editData(int idOfKeyword){
        if(idOfKeyword == 4){
            setPriorData(keywords[0]);
            startDataWriterIntent(keywords[0], priorData);
        } else if(idOfKeyword == 5){
            setPriorData(keywords[1]);
            startDataWriterIntent(keywords[1], priorData);
        } else if(idOfKeyword == 2){
            setPriorData(keywords[2]);
            startDataWriterIntent(keywords[2], priorData);
        } else if(idOfKeyword == 3){
            setPriorData(keywords[3]);
            startDataWriterIntent(keywords[3], priorData);
        } else{
            Toast.makeText(this, "Unable to process request.", Toast.LENGTH_SHORT).show();
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

}
