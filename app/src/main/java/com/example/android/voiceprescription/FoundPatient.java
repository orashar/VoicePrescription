package com.example.android.voiceprescription;

public class FoundPatient {

    private String patientId;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String patientEmail;

    public FoundPatient(String patientId, String patientName, String patientAge, String patientGender, String patientEmail) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientEmail = patientEmail;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public String getPatientEmail() {
        return patientEmail;
    }
}
