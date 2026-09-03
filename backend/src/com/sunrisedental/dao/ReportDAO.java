package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {


    public int countAppointments(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM appointments a " +
                        "WHERE a.appointment_date BETWEEN ? AND ? " +
                        "AND a.status <> 'CANCELLED'" +
                        dentistFilter(
                                dentistId,
                                "a"
                        );


        try (Connection conn =
                     DBConnection
                             .getInstance()
                             .getConnection();

             PreparedStatement ps =
                     prepare(
                             conn,
                             sql,
                             from,
                             to,
                             dentistId
                     );

             ResultSet rs =
                     ps.executeQuery()) {


            return rs.next()
                    ? rs.getInt(1)
                    : 0;
        }
    }


    public double totalRevenue(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        String sql =
                "SELECT COALESCE(SUM(b.total_amount), 0) " +
                        "FROM bills b " +
                        "JOIN appointments a " +
                        "ON b.appointment_id = a.appointment_id " +
                        "WHERE a.appointment_date BETWEEN ? AND ? " +
                        "AND a.status <> 'CANCELLED'" +
                        dentistFilter(
                                dentistId,
                                "a"
                        );


        try (Connection conn =
                     DBConnection
                             .getInstance()
                             .getConnection();

             PreparedStatement ps =
                     prepare(
                             conn,
                             sql,
                             from,
                             to,
                             dentistId
                     );

             ResultSet rs =
                     ps.executeQuery()) {


            return rs.next()
                    ? rs.getDouble(1)
                    : 0;
        }
    }


    public String busiestDentist(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        String sql =
                "SELECT d.name " +
                        "FROM appointments a " +
                        "JOIN dentists d " +
                        "ON a.dentist_id = d.dentist_id " +
                        "WHERE a.appointment_date BETWEEN ? AND ? " +
                        "AND a.status <> 'CANCELLED'" +
                        dentistFilter(
                                dentistId,
                                "a"
                        ) +
                        " GROUP BY d.dentist_id, d.name " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1";


        try (Connection conn =
                     DBConnection
                             .getInstance()
                             .getConnection();

             PreparedStatement ps =
                     prepare(
                             conn,
                             sql,
                             from,
                             to,
                             dentistId
                     );

             ResultSet rs =
                     ps.executeQuery()) {


            return rs.next()
                    ? rs.getString("name")
                    : "N/A";
        }
    }


    public String topTreatment(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        String sql =
                "SELECT t.treatment_name " +
                        "FROM appointments a " +
                        "JOIN treatments t " +
                        "ON a.treatment_id = t.treatment_id " +
                        "WHERE a.appointment_date BETWEEN ? AND ? " +
                        "AND a.status <> 'CANCELLED'" +
                        dentistFilter(
                                dentistId,
                                "a"
                        ) +
                        " GROUP BY t.treatment_id, t.treatment_name " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 1";


        try (Connection conn =
                     DBConnection
                             .getInstance()
                             .getConnection();

             PreparedStatement ps =
                     prepare(
                             conn,
                             sql,
                             from,
                             to,
                             dentistId
                     );

             ResultSet rs =
                     ps.executeQuery()) {


            return rs.next()
                    ? rs.getString(
                    "treatment_name"
            )
                    : "N/A";
        }
    }


    public List<String[]> breakdown(
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        List<String[]> rows =
                new ArrayList<>();


        String sql =
                "SELECT " +
                        "a.appointment_date, " +
                        "d.name AS dentist, " +
                        "t.treatment_name, " +
                        "COUNT(*) AS cnt, " +
                        "COALESCE(SUM(b.total_amount),0) AS revenue " +

                        "FROM appointments a " +

                        "JOIN dentists d " +
                        "ON a.dentist_id = d.dentist_id " +

                        "JOIN treatments t " +
                        "ON a.treatment_id = t.treatment_id " +

                        "LEFT JOIN bills b " +
                        "ON b.appointment_id = a.appointment_id " +

                        "WHERE a.appointment_date BETWEEN ? AND ? " +
                        "AND a.status <> 'CANCELLED'" +

                        dentistFilter(
                                dentistId,
                                "a"
                        ) +

                        " GROUP BY " +
                        "a.appointment_date, " +
                        "d.dentist_id, " +
                        "d.name, " +
                        "t.treatment_id, " +
                        "t.treatment_name " +

                        "ORDER BY a.appointment_date";


        try (Connection conn =
                     DBConnection
                             .getInstance()
                             .getConnection();

             PreparedStatement ps =
                     prepare(
                             conn,
                             sql,
                             from,
                             to,
                             dentistId
                     );

             ResultSet rs =
                     ps.executeQuery()) {


            while (rs.next()) {

                rows.add(
                        new String[]{

                                String.valueOf(
                                        rs.getDate(
                                                "appointment_date"
                                        )
                                ),

                                rs.getString(
                                        "dentist"
                                ),

                                rs.getString(
                                        "treatment_name"
                                ),

                                String.valueOf(
                                        rs.getInt(
                                                "cnt"
                                        )
                                ),

                                String.valueOf(
                                        rs.getDouble(
                                                "revenue"
                                        )
                                )
                        }
                );
            }
        }


        return rows;
    }


    private String dentistFilter(
            String dentistId,
            String alias
    ) {

        return dentistId != null &&
                !dentistId.isBlank()

                ? " AND " +
                alias +
                ".dentist_id = ?"

                : "";
    }


    private PreparedStatement prepare(
            Connection conn,
            String sql,
            String from,
            String to,
            String dentistId
    ) throws SQLException {

        PreparedStatement ps =
                conn.prepareStatement(sql);


        ps.setString(
                1,
                from
        );


        ps.setString(
                2,
                to
        );


        if (dentistId != null &&
                !dentistId.isBlank()) {

            ps.setInt(
                    3,
                    Integer.parseInt(
                            dentistId
                    )
            );
        }


        return ps;
    }
}