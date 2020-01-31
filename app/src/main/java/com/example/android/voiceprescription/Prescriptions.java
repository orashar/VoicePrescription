package com.example.android.voiceprescription;

public class Prescriptions {

    private String pid;
    private String date;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String patientEmail;
    private String patientPrescription;

    public Prescriptions(String pid, String date, String name, String age, String gender, String email, String prescription){
        this.pid = pid;
        this.date = date;
        patientName = name;
        patientAge = age;
        patientGender = gender;
        patientEmail = email;
        patientPrescription = prescription;
    }

    public String getPid() {
        return pid;
    }

    public String getDate() {
        return date;
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

    public String getPatientPrescription() {
        return patientPrescription;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setDate(String date) {
        this.date = date;
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

    public void setPatientPrescription(String patientPrescription) {
        this.patientPrescription = patientPrescription;
    }
}
