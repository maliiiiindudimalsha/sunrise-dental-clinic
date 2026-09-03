package com.sunrisedental.service;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.model.Dentist;

import java.sql.SQLException;
import java.util.List;

public class DentistService {

    private final DentistDAO dentistDAO =
            new DentistDAO();


    public List<Dentist> getAll()
            throws SQLException {

        return dentistDAO.findAll();
    }


    public Dentist add(Dentist dentist)
            throws SQLException {

        validate(dentist);

        return dentistDAO.insert(
                dentist
        );
    }


    public boolean update(
            int id,
            Dentist dentist
    ) throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "Invalid dentist ID."
            );
        }

        validate(dentist);

        return dentistDAO.update(
                id,
                dentist
        );
    }


    public boolean delete(int id)
            throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "Invalid dentist ID."
            );
        }

        return dentistDAO.delete(id);
    }


    private void validate(
            Dentist dentist
    ) {

        if (dentist.getName() == null ||
                dentist.getName()
                        .trim()
                        .length() < 2) {

            throw new IllegalArgumentException(
                    "Dentist name is required."
            );
        }


        dentist.setName(
                dentist.getName().trim()
        );


        if (dentist.getSpecialization() != null) {

            dentist.setSpecialization(
                    dentist
                            .getSpecialization()
                            .trim()
            );
        }
    }
}