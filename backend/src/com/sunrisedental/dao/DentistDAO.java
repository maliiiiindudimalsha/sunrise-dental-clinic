package com.sunrisedental.dao;

import com.sunrisedental.db.DBConnection;
import com.sunrisedental.model.Dentist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistDAO {

    public List<Dentist> findAll()
            throws SQLException {

        List<Dentist> list =
                new ArrayList<>();

        String sql =
                "SELECT * FROM dentists ORDER BY dentist_id";


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


    public Dentist insert(
            Dentist dentist
    ) throws SQLException {

        String sql =
                "INSERT INTO dentists (name, specialization) VALUES (?, ?)";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {


            ps.setString(
                    1,
                    dentist.getName()
            );

            ps.setString(
                    2,
                    dentist.getSpecialization()
            );

            ps.executeUpdate();


            try (ResultSet rs =
                         ps.getGeneratedKeys()) {

                if (rs.next()) {

                    dentist.setDentistId(
                            rs.getInt(1)
                    );
                }
            }
        }

        return dentist;
    }


    public boolean update(
            int id,
            Dentist dentist
    ) throws SQLException {

        String sql =
                "UPDATE dentists " +
                        "SET name = ?, specialization = ? " +
                        "WHERE dentist_id = ?";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setString(
                    1,
                    dentist.getName()
            );

            ps.setString(
                    2,
                    dentist.getSpecialization()
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
                "DELETE FROM dentists WHERE dentist_id = ?";


        try (Connection conn =
                     DBConnection.getInstance().getConnection();

             PreparedStatement ps =
                     conn.prepareStatement(sql)) {


            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }


    private Dentist mapRow(
            ResultSet rs
    ) throws SQLException {

        Dentist dentist =
                new Dentist();

        dentist.setDentistId(
                rs.getInt(
                        "dentist_id"
                )
        );

        dentist.setName(
                rs.getString(
                        "name"
                )
        );

        dentist.setSpecialization(
                rs.getString(
                        "specialization"
                )
        );

        return dentist;
    }
}