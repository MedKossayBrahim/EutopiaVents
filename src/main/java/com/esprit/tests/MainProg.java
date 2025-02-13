package com.esprit.tests;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.models.Reservation;
import com.esprit.services.MaterielService;
import com.esprit.services.CategorieService;
import com.esprit.services.ReservationService;


import java.sql.Timestamp;
import java.util.Calendar;

public class MainProg {
    public static void main(String[] args) {
        //MaterielService ps = new MaterielService();
//       ps.ajouter(new Materiel("flamme","tente ",10,4,70,"link"));
//        ps.modifier(new Materiel(2,"test","chaise", 10,6,2,"im"));
       // ps.supprimer(new Materiel(6));
       //System.out.println(ps.rechercher());
        ///////////////////////////////////////////////////////////////////
//        CategorieService cs = new CategorieService();
//         cs.ajouter(new Categorie("Matériel de Restauration"));
       // cs.ajouter(new Categorie("sport"));
         //cs.modifier(new Categorie(4,"Éclairage & Électricité"));
        //  cs.supprimer(new Categorie(3));
 //        System.out.println(cs.rechercher());
        //////////////////////////////////////////////
       ReservationService rs = new ReservationService();
       rs.ajouter(new Reservation(10,3,86));
         //rs.modifier(new Reservation(7,10,9,1));
//        rs.supprimer(new Reservation(7));
//        System.out.println(rs.rechercher());


    }
}
