package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public Appointment register(Appointment a) throws SQLException {

        String patientSql =
                "INSERT INTO patients (name, address, contact_number) VALUES (?, ?, ?)";

        String appointmentSql =
                "INSERT INTO appointments " +
                        "(patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        String numberSql =
                "UPDATE appointments SET appointment_no = ? WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {

                int patientId;
                int appointmentId;

                try (PreparedStatement ps = conn.prepareStatement(
                        patientSql,
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, a.getPatientName());
                    ps.setString(2, a.getAddress());
                    ps.setString(3, a.getContactNumber());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException("Failed to create patient.");
                        }

                        patientId = rs.getInt(1);
                    }
                }


                try (PreparedStatement ps = conn.prepareStatement(
                        appointmentSql,
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, patientId);
                    ps.setInt(2, a.getDentistId());
                    ps.setInt(3, a.getTreatmentId());
                    ps.setString(4, a.getAppointmentDate());
                    ps.setString(5, a.getAppointmentTime());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException("Failed to create appointment.");
                        }

                        appointmentId = rs.getInt(1);
                    }
                }


                String appointmentNo =
                        String.format("APT%05d", appointmentId);

                try (PreparedStatement ps =
                             conn.prepareStatement(numberSql)) {

                    ps.setString(1, appointmentNo);
                    ps.setInt(2, appointmentId);

                    ps.executeUpdate();
                }

                conn.commit();

                return findById(appointmentId);

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }


    // NEW - Returns all appointments for the dropdown
    public List<Appointment> findAll() throws SQLException {

        List<Appointment> appointments = new ArrayList<>();

        String sql =
                "SELECT a.*, " +
                        "p.name AS patient_name, " +
                        "p.address, " +
                        "p.contact_number, " +
                        "d.name AS dentist_name, " +
                        "t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.patient_id " +
                        "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                        "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_no IS NOT NULL " +
                        "ORDER BY a.appointment_id DESC";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {
                appointments.add(mapRow(rs));
            }
        }

        return appointments;
    }


    public Appointment findByAppointmentNo(String appointmentNo)
            throws SQLException {

        String sql =
                "SELECT a.*, " +
                        "p.name AS patient_name, " +
                        "p.address, " +
                        "p.contact_number, " +
                        "d.name AS dentist_name, " +
                        "t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.patient_id " +
                        "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                        "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_no = ?";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, appointmentNo);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? mapRow(rs)
                        : null;
            }
        }
    }


    private Appointment findById(int appointmentId)
            throws SQLException {

        String sql =
                "SELECT a.*, " +
                        "p.name AS patient_name, " +
                        "p.address, " +
                        "p.contact_number, " +
                        "d.name AS dentist_name, " +
                        "t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.patient_id " +
                        "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                        "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_id = ?";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? mapRow(rs)
                        : null;
            }
        }
    }


    public boolean update(
            String appointmentNo,
            Appointment a
    ) throws SQLException {

        String findSql =
                "SELECT patient_id FROM appointments WHERE appointment_no = ?";

        String patientSql =
                "UPDATE patients " +
                        "SET name = ?, address = ?, contact_number = ? " +
                        "WHERE patient_id = ?";

        String appointmentSql =
                "UPDATE appointments " +
                        "SET appointment_date = ?, appointment_time = ? " +
                        "WHERE appointment_no = ?";

        try (Connection conn =
                     DBConnection.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {

                int patientId;

                try (PreparedStatement ps =
                             conn.prepareStatement(findSql)) {

                    ps.setString(1, appointmentNo);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            conn.rollback();
                            return false;
                        }

                        patientId =
                                rs.getInt("patient_id");
                    }
                }


                try (PreparedStatement ps =
                             conn.prepareStatement(patientSql)) {

                    ps.setString(1, a.getPatientName());
                    ps.setString(2, a.getAddress());
                    ps.setString(3, a.getContactNumber());
                    ps.setInt(4, patientId);

                    ps.executeUpdate();
                }


                try (PreparedStatement ps =
                             conn.prepareStatement(appointmentSql)) {

                    ps.setString(1, a.getAppointmentDate());
                    ps.setString(2, a.getAppointmentTime());
                    ps.setString(3, appointmentNo);

                    if (ps.executeUpdate() == 0) {

                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }


    public boolean cancel(String appointmentNo)
            throws SQLException {

        String sql =
                "UPDATE appointments " +
                        "SET status = 'CANCELLED' " +
                        "WHERE appointment_no = ? " +
                        "AND status <> 'CANCELLED'";

        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, appointmentNo);

            return ps.executeUpdate() > 0;
        }
    }


    private Appointment mapRow(ResultSet rs)
            throws SQLException {

        Appointment a =
                new Appointment();

        a.setAppointmentId(
                rs.getInt("appointment_id")
        );

        a.setAppointmentNo(
                rs.getString("appointment_no")
        );

        a.setPatientName(
                rs.getString("patient_name")
        );

        a.setAddress(
                rs.getString("address")
        );

        a.setContactNumber(
                rs.getString("contact_number")
        );

        a.setDentistName(
                rs.getString("dentist_name")
        );

        a.setTreatmentName(
                rs.getString("treatment_name")
        );

        a.setAppointmentDate(
                String.valueOf(
                        rs.getDate("appointment_date")
                )
        );

        Time time =
                rs.getTime("appointment_time");

        a.setAppointmentTime(
                time == null
                        ? ""
                        : time.toLocalTime().toString()
        );

        a.setStatus(
                rs.getString("status")
        );

        return a;
    }
}