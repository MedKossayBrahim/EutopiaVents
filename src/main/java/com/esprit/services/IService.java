package com.esprit.services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    void ajouter(T t);
    void modifier(T t) throws SQLException;
    void supprimer(T t);
    List<T> rechercher();
}
