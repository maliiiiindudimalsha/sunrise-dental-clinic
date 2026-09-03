package com.sunrisedental.service;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.model.Dentist;

import java.sql.SQLException;
import java.util.List;

public class DentistService {
    private DentistDAO dentistDAO = new DentistDAO();

    public List<Dentist> getAll() throws SQLException { return dentistDAO.findAll(); }
    public Dentist add(Dentist d) throws SQLException { return dentistDAO.insert(d); }
    public boolean update(int id, Dentist d) throws SQLException { return dentistDAO.update(id, d); }
    public boolean delete(int id) throws SQLException { return dentistDAO.delete(id); }
}