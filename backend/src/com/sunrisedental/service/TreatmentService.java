package com.sunrisedental.service;

import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.model.Treatment;

import java.sql.SQLException;
import java.util.List;

public class TreatmentService {

    private final TreatmentDAO treatmentDAO =
            new TreatmentDAO();


    public List<Treatment> getAll()
            throws SQLException {

        return treatmentDAO.findAll();
    }


    public Treatment add(
            Treatment treatment
    ) throws SQLException {

        validate(treatment);

        return treatmentDAO.insert(
                treatment
        );
    }


    public boolean update(
            int id,
            Treatment treatment
    ) throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "Invalid treatment ID."
            );
        }

        validate(treatment);

        return treatmentDAO.update(
                id,
                treatment
        );
    }


    public boolean delete(int id)
            throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "Invalid treatment ID."
            );
        }

        return treatmentDAO.delete(id);
    }


    private void validate(
            Treatment treatment
    ) {

        if (treatment.getTreatmentName() == null ||
                treatment
                        .getTreatmentName()
                        .trim()
                        .length() < 2) {

            throw new IllegalArgumentException(
                    "Treatment name is required."
            );
        }


        if (treatment.getConsultationFee() < 0) {

            throw new IllegalArgumentException(
                    "Consultation fee cannot be negative."
            );
        }


        treatment.setTreatmentName(
                treatment
                        .getTreatmentName()
                        .trim()
        );
    }
}