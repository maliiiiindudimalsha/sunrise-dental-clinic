package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public Patient insert(Patient patient) throws SQLException {

        String sql =
                "INSERT INTO patients (name, address, contact_number) " +
                        "VALUES (?, ?, ?)";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setString(1, patient.getName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNumber());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    patient.setPatientId(rs.getInt(1));
                }
            }

            return patient;
        }
    }


    public Patient findById(int patientId)
            throws SQLException {

        String sql =
                "SELECT patient_id, name, address, contact_number " +
                        "FROM patients WHERE patient_id = ?";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }


    public List<Patient> findAll()
            throws SQLException {

        List<Patient> patients =
                new ArrayList<>();

        String sql =
                "SELECT patient_id, name, address, contact_number " +
                        "FROM patients ORDER BY patient_id DESC";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {
                patients.add(mapRow(rs));
            }
        }

        return patients;
    }


    public boolean update(
            int patientId,
            Patient patient
    ) throws SQLException {

        String sql =
                "UPDATE patients " +
                        "SET name = ?, address = ?, contact_number = ? " +
                        "WHERE patient_id = ?";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, patient.getName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNumber());
            ps.setInt(4, patientId);

            return ps.executeUpdate() > 0;
        }
    }


    private Patient mapRow(ResultSet rs)
            throws SQLException {

        Patient patient = new Patient();

        patient.setPatientId(
                rs.getInt("patient_id")
        );

        patient.setName(
                rs.getString("name")
        );

        patient.setAddress(
                rs.getString("address")
        );

        patient.setContactNumber(
                rs.getString("contact_number")
        );

        return patient;
    }
}