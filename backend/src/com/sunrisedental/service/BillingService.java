package com.sunrisedental.service;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.model.Bill;

import java.sql.SQLException;

public class BillingService {
    private BillDAO billDAO = new BillDAO();

    // If a bill already exists for this appointment, return it.
    // Otherwise call the stored procedure to generate one, then return it.
    public Bill getOrGenerateBill(String appointmentNo) throws SQLException {
        Bill existing = billDAO.findByAppointmentNo(appointmentNo);
        if (existing != null) return existing;

        billDAO.generateBill(appointmentNo);
        return billDAO.findByAppointmentNo(appointmentNo);
    }
}