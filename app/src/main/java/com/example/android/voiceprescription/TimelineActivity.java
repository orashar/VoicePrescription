package com.example.android.voiceprescription;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    private PrescriptionDBAdapter prescriptionDBAdapter;
    private String tvData;
    private ListView timelinelv;
    private  List<Prescriptions> presc;

    private String pid;
    private String forParticularPatient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_layout);

        timelinelv = findViewById(R.id.list_view_timeline);

        Intent intent = getIntent();
        forParticularPatient = intent.getStringExtra("FOR_PARTICULAR_PATIENT");
        pid = intent.getStringExtra("ID_OF_PATIENT");

        prescriptionDBAdapter = PrescriptionDBAdapter.getPrescriptionDBAdapterInstance(this);

        if (forParticularPatient.equals("false")) {
            presc = prescriptionDBAdapter.getTimeline();
        } else {
            presc = prescriptionDBAdapter.getPatientRecord(pid);
        }
        if (presc.size() > 0) {

            TimelineAdapter timelineAdapter = new TimelineAdapter(this, presc);

            timelinelv.setAdapter(timelineAdapter);

            timelinelv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Prescriptions prescriptionItem = presc.get(position);

                    Toast.makeText(TimelineActivity.this, prescriptionItem.getPatientName(), Toast.LENGTH_SHORT).show();
                }
            });

            timelinelv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Prescriptions prescriptionItem = presc.get(position);
                    ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("PatientId", prescriptionItem.getPid());
                    clipBoard.setPrimaryClip(clip);
                    Toast.makeText(TimelineActivity.this, "PId copied to clipboard.", Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
        } else{
            findViewById(R.id.tv_no_timeline).setVisibility(View.VISIBLE);
        }
    }
}
