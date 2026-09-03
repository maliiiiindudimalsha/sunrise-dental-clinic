package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Appointment;

import java.sql.*;

public class AppointmentDAO {

    public Appointment register(
            Appointment appointment
    ) throws SQLException {

        String patientSql =
                "INSERT INTO patients " +
                        "(name, address, contact_number) " +
                        "VALUES (?, ?, ?)";

        String appointmentSql =
                "INSERT INTO appointments " +
                        "(patient_id, dentist_id, treatment_id, " +
                        "appointment_date, appointment_time, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn =
                     DBConnection.getInstance()
                             .getConnection()) {

            conn.setAutoCommit(false);

            try {

                int patientId;

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     patientSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    ps.setString(
                            1,
                            appointment.getPatientName()
                    );

                    ps.setString(
                            2,
                            appointment.getAddress()
                    );

                    ps.setString(
                            3,
                            appointment.getContactNumber()
                    );

                    ps.executeUpdate();

                    try (ResultSet rs =
                                 ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Failed to create patient."
                            );
                        }

                        patientId =
                                rs.getInt(1);
                    }
                }

                int appointmentId;

                try (PreparedStatement ps =
                             conn.prepareStatement(
                                     appointmentSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    ps.setInt(
                            1,
                            patientId
                    );

                    ps.setInt(
                            2,
                            appointment.getDentistId()
                    );

                    ps.setInt(
                            3,
                            appointment.getTreatmentId()
                    );

                    ps.setString(
                            4,
                            appointment.getAppointmentDate()
                    );

                    ps.setString(
                            5,
                            appointment.getAppointmentTime()
                    );

                    ps.executeUpdate();

                    try (ResultSet rs =
                                 ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Failed to create appointment."
                            );
                        }

                        appointmentId =
                                rs.getInt(1);
                    }
                }

                conn.commit();

                return findById(
                        appointmentId
                );

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }


    public Appointment findByAppointmentNo(
            String appointmentNo
    ) throws SQLException {

        String sql =
                "SELECT a.*, " +
                        "p.name AS patient_name, " +
                        "p.address, " +
                        "p.contact_number, " +
                        "d.name AS dentist_name, " +
                        "t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN patients p " +
                        "ON a.patient_id = p.patient_id " +
                        "JOIN dentists d " +
                        "ON a.dentist_id = d.dentist_id " +
                        "JOIN treatments t " +
                        "ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_no = ?";

        try (Connection conn =
                     DBConnection.getInstance()
                             .getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    appointmentNo
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return mapRow(rs);
                }
            }
        }

        return null;
    }


    private Appointment findById(
            int appointmentId
    ) throws SQLException {

        String sql =
                "SELECT a.*, " +
                        "p.name AS patient_name, " +
                        "p.address, " +
                        "p.contact_number, " +
                        "d.name AS dentist_name, " +
                        "t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN patients p " +
                        "ON a.patient_id = p.patient_id " +
                        "JOIN dentists d " +
                        "ON a.dentist_id = d.dentist_id " +
                        "JOIN treatments t " +
                        "ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_id = ?";

        try (Connection conn =
                     DBConnection.getInstance()
                             .getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    appointmentId
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return mapRow(rs);
                }
            }
        }

        return null;
    }


    public boolean update(
            String appointmentNo,
            Appointment appointment
    ) throws SQLException {

        String findSql =
                "SELECT patient_id " +
                        "FROM appointments " +
                        "WHERE appointment_no = ?";

        String patientSql =
                "UPDATE patients " +
                        "SET name = ?, address = ?, contact_number = ? " +
                        "WHERE patient_id = ?";

        String appointmentSql =
                "UPDATE appointments " +
                        "SET appointment_date = ?, appointment_time = ? " +
                        "WHERE appointment_no = ?";

        try (Connection conn =
                     DBConnection.getInstance()
                             .getConnection()) {

            conn.setAutoCommit(false);

            try {

                int patientId;

                // Find patient linked to appointment
                try (PreparedStatement ps =
                             conn.prepareStatement(findSql)) {

                    ps.setString(
                            1,
                            appointmentNo
                    );

                    try (ResultSet rs =
                                 ps.executeQuery()) {

                        if (!rs.next()) {

                            conn.rollback();
                            return false;
                        }

                        patientId =
                                rs.getInt("patient_id");
                    }
                }


                // Update patient details
                try (PreparedStatement ps =
                             conn.prepareStatement(patientSql)) {

                    ps.setString(
                            1,
                            appointment.getPatientName()
                    );

                    ps.setString(
                            2,
                            appointment.getAddress()
                    );

                    ps.setString(
                            3,
                            appointment.getContactNumber()
                    );

                    ps.setInt(
                            4,
                            patientId
                    );

                    ps.executeUpdate();
                }


                // Update appointment date/time
                // Double-booking UPDATE trigger will also run here.
                try (PreparedStatement ps =
                             conn.prepareStatement(appointmentSql)) {

                    ps.setString(
                            1,
                            appointment.getAppointmentDate()
                    );

                    ps.setString(
                            2,
                            appointment.getAppointmentTime()
                    );

                    ps.setString(
                            3,
                            appointmentNo
                    );

                    int affectedRows =
                            ps.executeUpdate();

                    if (affectedRows == 0) {

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


    public boolean cancel(
            String appointmentNo
    ) throws SQLException {

        String sql =
                "UPDATE appointments " +
                        "SET status = 'CANCELLED' " +
                        "WHERE appointment_no = ? " +
                        "AND status <> 'CANCELLED'";

        try (Connection conn =
                     DBConnection.getInstance()
                             .getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    appointmentNo
            );

            return ps.executeUpdate() > 0;
        }
    }


    private Appointment mapRow(
            ResultSet rs
    ) throws SQLException {

        Appointment appointment =
                new Appointment();

        appointment.setAppointmentId(
                rs.getInt("appointment_id")
        );

        appointment.setAppointmentNo(
                rs.getString("appointment_no")
        );

        appointment.setPatientName(
                rs.getString("patient_name")
        );

        appointment.setAddress(
                rs.getString("address")
        );

        appointment.setContactNumber(
                rs.getString("contact_number")
        );

        appointment.setDentistName(
                rs.getString("dentist_name")
        );

        appointment.setTreatmentName(
                rs.getString("treatment_name")
        );

        appointment.setAppointmentDate(
                String.valueOf(
                        rs.getDate("appointment_date")
                )
        );

        Time time =
                rs.getTime("appointment_time");

        appointment.setAppointmentTime(
                time == null
                        ? ""
                        : time.toLocalTime()
                        .toString()
        );

        appointment.setStatus(
                rs.getString("status")
        );

        return appointment;
    }
}