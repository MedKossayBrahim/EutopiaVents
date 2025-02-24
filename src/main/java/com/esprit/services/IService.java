package com.esprit.services;

import java.util.List;

public interface IService<T> {
    void ajouter(T var1);

    void modifier(T var1);

    void supprimer(T t);


    List<T> rechercher();
}
