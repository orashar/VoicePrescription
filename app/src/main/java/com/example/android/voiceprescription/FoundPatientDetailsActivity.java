package com.example.android.voiceprescription;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class FoundPatientDetailsActivity extends Activity {

    private String FOR_PARTICULAR_PATIENT = "true";

    private String[] patientDetails = {
            "", "", "", "", "",
    };
    private String[] detailsDoctor = {
            "", "", "", ""
    };

    private TextView foundPatientPidtv;
    private TextView foundPatientNametv;
    private TextView foundPatientAgetv;
    private TextView foundPatientGendertv;
    private TextView foundPatientRecordstv;

    private String patientEmail;

    private Button viewRecordBtn;
    private Button newPrescriptionBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_patient_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int windowW = dm.widthPixels;
        int windowH= dm.heightPixels;

        getWindow().setLayout((int) (windowW*0.8), (int) (windowH*0.6));

        final Intent intentFromIdActivity = getIntent();
        patientDetails = intentFromIdActivity.getStringArrayExtra("DETAILS_OF_PATIENT");
        detailsDoctor = intentFromIdActivity.getStringArrayExtra("DETAILS_OF_DOCTOR");
        patientEmail = intentFromIdActivity.getStringExtra("EMAIL_PATIENT");

        foundPatientPidtv = findViewById(R.id.found_patient_id_tv);
        foundPatientNametv = findViewById(R.id.found_patient_name_tv);
        foundPatientAgetv = findViewById(R.id.found_patient_age_tv);
        foundPatientGendertv = findViewById(R.id.found_patient_gender_tv);
        foundPatientRecordstv = findViewById(R.id.found_prescription_count_tv);
        viewRecordBtn = findViewById(R.id.view_record_btn);
        newPrescriptionBtn = findViewById(R.id.new_prescription_btn);

        foundPatientPidtv.setText(foundPatientPidtv.getText().toString()+patientDetails[0]);
        foundPatientNametv.setText(foundPatientNametv.getText().toString()+patientDetails[1]);
        foundPatientAgetv.setText(foundPatientAgetv.getText().toString()+patientDetails[2]);
        foundPatientGendertv.setText(foundPatientGendertv.getText().toString()+patientDetails[3]);
        foundPatientRecordstv.setText(foundPatientRecordstv.getText().toString()+patientDetails[4]);

        viewRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToRecord = new Intent(FoundPatientDetailsActivity.this, TimelineActivity.class);
                intentToRecord.putExtra("FOR_PARTICULAR_PATIENT", FOR_PARTICULAR_PATIENT);
                intentToRecord.putExtra("ID_OF_PATIENT", patientDetails[0]);
                startActivity(intentToRecord);
            }
        });

        newPrescriptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patientDetails[4] = patientEmail;
                Intent intentToVoiceActivity = new Intent(FoundPatientDetailsActivity.this, VoiceToDisplayActivity.class);
                intentToVoiceActivity.putExtra("DETAILS_OF_DOCTOR", detailsDoctor);
                intentToVoiceActivity.putExtra("DETAILS_OF_PATIENT", patientDetails);
                intentToVoiceActivity.putExtra("IS_PATIENT_NEW", "false");
                startActivity(intentToVoiceActivity);
            }
        });
    }
}
