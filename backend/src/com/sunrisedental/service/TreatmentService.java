package com.sunrisedental.service;

import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.model.Treatment;

import java.sql.SQLException;
import java.util.List;

public class TreatmentService {
    private TreatmentDAO treatmentDAO = new TreatmentDAO();

    public List<Treatment> getAll() throws SQLException { return treatmentDAO.findAll(); }
    public Treatment add(Treatment t) throws SQLException { return treatmentDAO.insert(t); }
    public boolean update(int id, Treatment t) throws SQLException { return treatmentDAO.update(id, t); }
    public boolean delete(int id) throws SQLException { return treatmentDAO.delete(id); }
}