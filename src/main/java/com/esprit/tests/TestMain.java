package com.esprit.tests;

import com.esprit.models.CategoriesEvent;
import com.esprit.models.Evenement;
import com.esprit.models.Reservations;
import com.esprit.services.CategoriesEventService;
import com.esprit.services.EvenementService;
import com.esprit.services.ReservationsService;
import com.esprit.services.ReservationsService;

import java.util.List;

public class TestMain {
    public static <Reservations> void main(String[] args) {
//
//        EvenementService evenementService = new EvenementService();
//
//        // Ajouter un événement avec un lieu existant
//        Evenement event1 = new Evenement(0, "Conférence Tech", "Une conférence sur les nouvelles technologies",
//                "2025-05-20 10:00:00", "2025-05-20 18:00:00", 200,
//                13, 1, 10, 50.0, "en attente", null, "image1.jpg");
//        evenementService.ajouter(event1);
//        System.out.println("Événement ajouté avec ID : " + event1.getId());
//
//        // Ajouter un événement avec un lieu personnalisé
//        Evenement event2 = new Evenement(0, "Workshop IA", "Atelier sur l'intelligence artificielle",
//                "2025-06-15 09:00:00", "2025-06-15 17:00:00", 150,
//                10, 1, 10, 30.0, "en attente", "Hôtel Royal", "image2.jpg");
//        evenementService.ajouter(event2);
//        System.out.println("Événement ajouté avec ID : " + event2.getId());
//
//        // Modifier un événement existant
//        event1.setTitre("Conférence Tech 2025");
//        event1.setPrix(55.0);
//        evenementService.modifier(event1);
//        System.out.println("Événement modifié : " + event1);
//
//        // Rechercher et afficher tous les événements
//        List<Evenement> evenements = evenementService.rechercher();
//        System.out.println("Liste des événements :");
//        for (Evenement e : evenements) {
//            System.out.println(e);
//        }
//
//        // Supprimer un événement
//        evenementService.supprimer(event1);

 //*********************************************cathegorie***********************************************************

//        CategoriesEventService categoryService = new CategoriesEventService();
//
//        // Ajouter une nouvelle catégorie
//        CategoriesEvent newCategory = new CategoriesEvent("fddb");
//        categoryService.ajouter(newCategory);
//
//        // Modifier une catégorie existante
//
//        newCategory.setNom("Updated Tqethehech");
//        categoryService.modifier(newCategory);
//
//        // Supprimer une catégorie
//        //categoryService.supprimer(newCategory);
//
//        // Rechercher et afficher toutes les catégories
//        System.out.println(categoryService.rechercher());


//**************************************************Reservations*************************************************************************

        ReservationsService reservationsService = new ReservationsService();

        // Simuler un utilisateur et un événement
        int userId = 10;
        int eventId = 62;

        // Créer une réservation de 1 billet
        com.esprit.models.Reservations Reservations = new com.esprit.models.Reservations(0, eventId, userId, 1, 0.0, "en_attente");
        reservationsService.ajouter(Reservations);


        // Confirmer l'achat avec 3 billets
        reservationsService.confirmerAchat(Reservations.getId(), 3);

        // Afficher la réservation après confirmation
        System.out.println("Réservations après confirmation :");
        reservationsService.rechercher().forEach(System.out::println);
        reservationsService.supprimer(Reservations.getId());
    }

    }


