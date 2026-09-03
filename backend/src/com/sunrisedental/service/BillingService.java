package com.sunrisedental.service;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.model.Bill;

import java.sql.SQLException;

public class BillingService {

    private final BillDAO billDAO =
            new BillDAO();


    public Bill getOrGenerateBill(
            String appointmentNo
    ) throws SQLException {

        if (appointmentNo == null
                || appointmentNo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment number is required."
            );
        }


        appointmentNo =
                appointmentNo.trim();


        Bill existing =
                billDAO.findByAppointmentNo(
                        appointmentNo
                );


        if (existing != null) {

            return existing;
        }


        billDAO.generateBill(
                appointmentNo
        );


        return billDAO.findByAppointmentNo(
                appointmentNo
        );
    }
}