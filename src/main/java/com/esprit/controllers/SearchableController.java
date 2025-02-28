package com.esprit.controllers;

import java.sql.SQLException;

public interface SearchableController {
    void handleSearch(String searchText) throws SQLException;
}