package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Treatment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {

    public List<Treatment> findAll()
            throws SQLException {

        List<Treatment> list =
                new ArrayList<>();

        String sql =
                "SELECT * FROM treatments ORDER BY treatment_id";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {


            while (rs.next()) {

                list.add(
                        mapRow(rs)
                );
            }
        }

        return list;
    }


    public Treatment insert(
            Treatment treatment
    ) throws SQLException {

        String sql =
                "INSERT INTO treatments " +
                        "(treatment_name, consultation_fee) " +
                        "VALUES (?, ?)";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {


            ps.setString(
                    1,
                    treatment.getTreatmentName()
            );

            ps.setDouble(
                    2,
                    treatment.getConsultationFee()
            );


            ps.executeUpdate();


            try (ResultSet rs =
                         ps.getGeneratedKeys()) {

                if (rs.next()) {

                    treatment.setTreatmentId(
                            rs.getInt(1)
                    );
                }
            }
        }


        return treatment;
    }


    public boolean update(
            int id,
            Treatment treatment
    ) throws SQLException {

        String sql =
                "UPDATE treatments " +
                        "SET treatment_name = ?, consultation_fee = ? " +
                        "WHERE treatment_id = ?";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setString(
                    1,
                    treatment.getTreatmentName()
            );

            ps.setDouble(
                    2,
                    treatment.getConsultationFee()
            );

            ps.setInt(
                    3,
                    id
            );


            return ps.executeUpdate() > 0;
        }
    }


    public boolean delete(int id)
            throws SQLException {

        String sql =
                "DELETE FROM treatments WHERE treatment_id = ?";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    id
            );


            return ps.executeUpdate() > 0;
        }
    }


    private Treatment mapRow(
            ResultSet rs
    ) throws SQLException {

        Treatment treatment =
                new Treatment();


        treatment.setTreatmentId(
                rs.getInt(
                        "treatment_id"
                )
        );


        treatment.setTreatmentName(
                rs.getString(
                        "treatment_name"
                )
        );


        treatment.setConsultationFee(
                rs.getDouble(
                        "consultation_fee"
                )
        );


        return treatment;
    }
}