package com.example.android.voiceprescription;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionDBAdapter {

    private static final String TAG = PrescriptionDBAdapter.class.getSimpleName();

    private static final String DB_NAME = "vpresc.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_PRESCRIPTION = "table_priscription";


    private static final String PID = "pid";
    private static final String DATE = "date";
    private static final String PATIENT_NAME = "patient_name";
    private static final String PATIENT_AGE = "patient_age";
    private static final String PATIENT_GENDER = "patient_gender";
    private static final String PATIENT_EMAIL = "patient_email";
    private static final String PATIENT_PRESCRIPTION = "patient_prescription";

    private static final String CREATE_TABLE_PRESCRIPTION = "CREATE TABLE " + TABLE_PRESCRIPTION+"(" + PID +" TEXT, " + DATE + " TEXT, " + PATIENT_NAME + " TEXT, " + PATIENT_AGE + " TEXT, " + PATIENT_GENDER + " TEXT, " + PATIENT_EMAIL + " TEXT, " + PATIENT_PRESCRIPTION + " TEXT)";

    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private static PrescriptionDBAdapter prescriptionDBAdapterInstance;

    private PrescriptionDBAdapter(Context context){
        this.context = context;
        sqLiteDatabase = new PrescriptionDBHelper(this.context, DB_NAME, null, DB_VERSION).getWritableDatabase();
    }


    public static PrescriptionDBAdapter getPrescriptionDBAdapterInstance(Context context){
        if(prescriptionDBAdapterInstance == null){
            prescriptionDBAdapterInstance = new PrescriptionDBAdapter(context);
        }
        return prescriptionDBAdapterInstance;
    }

    public boolean insert(String[] dataRow){
        ContentValues contentValues = new ContentValues();
        contentValues.put(PID, dataRow[0]);
        contentValues.put(DATE, dataRow[1]);
        contentValues.put(PATIENT_NAME, dataRow[2]);
        contentValues.put(PATIENT_AGE, dataRow[3]);
        contentValues.put(PATIENT_GENDER, dataRow[4]);
        contentValues.put(PATIENT_EMAIL, dataRow[5]);
        contentValues.put(PATIENT_PRESCRIPTION, dataRow[6]);

        return sqLiteDatabase.insert(TABLE_PRESCRIPTION, null, contentValues) > 0;
    }

    public List<FoundPatient> getPatientDetailsById(String pid){
        List<FoundPatient> foundPatientDetails = new ArrayList<FoundPatient>();

        String[] selectionArgs = {pid};
        Cursor cursor = sqLiteDatabase.query(TABLE_PRESCRIPTION, new String[]{PID, PATIENT_NAME, PATIENT_AGE, PATIENT_GENDER, PATIENT_EMAIL}, "pid = ?", selectionArgs, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                FoundPatient foundPatient = new FoundPatient(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                foundPatientDetails.add(foundPatient);
            }while (cursor.moveToNext());
        }
        return foundPatientDetails;
    }

    public List<Prescriptions> getPatientRecord(String pid){
        List<Prescriptions> foundRecordList = new ArrayList<Prescriptions>();

        String[] selectionArgs = {pid};
        Cursor cursor = sqLiteDatabase.query(TABLE_PRESCRIPTION, new String[]{PID, DATE, PATIENT_NAME, PATIENT_AGE, PATIENT_GENDER, PATIENT_EMAIL, PATIENT_PRESCRIPTION}, "pid = ?", selectionArgs, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                Prescriptions foundRecord = new Prescriptions(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                Log.v("Result: ", "gotOneItem************************");
                foundRecordList.add(foundRecord);
            } while (cursor.moveToNext());
        }
        return foundRecordList;
    }

    public List<Prescriptions> getTimeline(){
        List<Prescriptions> prescriptionList = new ArrayList<Prescriptions>();

        Cursor cursor = sqLiteDatabase.query(TABLE_PRESCRIPTION, new String[]{PID, DATE, PATIENT_NAME, PATIENT_AGE, PATIENT_GENDER, PATIENT_EMAIL, PATIENT_PRESCRIPTION}, null, null, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            do {
                Prescriptions prescription = new Prescriptions(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                prescriptionList.add(prescription);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return prescriptionList;
    }

    public int returnNextIndex(){
        Cursor cursor = sqLiteDatabase.query(TABLE_PRESCRIPTION, new String[]{PID}, null, null, null, null, null, null);
        return cursor.getCount()+1;
    }

    private  class PrescriptionDBHelper extends SQLiteOpenHelper {

        public PrescriptionDBHelper(@Nullable Context context, @Nullable String databaseName, @Nullable SQLiteDatabase.CursorFactory factory, int dbVersion) {
            super(context, databaseName, factory, dbVersion);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            Log.v(TAG,"Inside onConfigure");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_PRESCRIPTION);
            Log.v(TAG,"Inside onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(TAG,"Inside onUpdate");
        }
    }
}
