package com.example.android.voiceprescription;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;

public class ShareAndSaveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_save_layout);

        TextView nametv =findViewById(R.id.patient_name_share_item);
        TextView emailtv =findViewById(R.id.patient_email_share_item);

        final String[] patientEmail = {getIntent().getStringExtra("EMAIL")};
        final String pid = getIntent().getStringExtra("PID");
        final String date = getIntent().getStringExtra("DATE");

        final String pdfName = pid+"-"+date;

        nametv.setText(nametv.getText().toString()+getIntent().getStringExtra("NAME"));
        emailtv.setText(emailtv.getText().toString()+patientEmail[0]);

        findViewById(R.id.via_gmail_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendEmail(patientEmail, pdfName);
            }
        });

        findViewById(R.id.via_whatsapp_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWhatsapp(pdfName);
            }
        });
    }

    private void sendWhatsapp(String pdfName){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/VoicePrescription/"+pdfName+".pdf"));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setPackage("com.whatsapp");

        startActivity(intent);
    }

    private void sendEmail(String[] addresses, String pdfName){
        String subject = "voice prescription";
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Uri attachment = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/VoicePrescription/"+pdfName+".pdf"));
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }
}
