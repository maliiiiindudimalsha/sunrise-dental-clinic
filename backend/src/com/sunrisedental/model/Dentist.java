package com.sunrisedental.model;

public class Dentist {
    private int dentistId;
    private String name;
    private String specialization;

    public Dentist() {}

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}