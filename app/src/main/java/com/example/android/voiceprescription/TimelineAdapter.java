package com.example.android.voiceprescription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends ArrayAdapter<Prescriptions> {

    public TimelineAdapter(Context context, List<Prescriptions> listTimeline){
        super(context, 0, listTimeline);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        Prescriptions currentPrescriptionItem = getItem(position);

        TextView pidtvLi = listItemView.findViewById(R.id.patient_id_list_tv);
        TextView datetvLi = listItemView.findViewById(R.id.prescription_date_list_tv);
        TextView patientNametvLi = listItemView.findViewById(R.id.patient_name_list_tv);
        TextView patientAgetvLi = listItemView.findViewById(R.id.patient_age_list_tv);
        TextView patientGendertvLi = listItemView.findViewById(R.id.patient_gender_list_tv);

        pidtvLi.setText(currentPrescriptionItem.getPid());
        datetvLi.setText(currentPrescriptionItem.getDate());
        patientNametvLi.setText(currentPrescriptionItem.getPatientName());
        patientAgetvLi.setText(currentPrescriptionItem.getPatientAge());
        patientGendertvLi.setText(currentPrescriptionItem.getPatientGender());

        return listItemView;
    }
}
