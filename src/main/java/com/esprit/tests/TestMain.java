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
        //***************************************************event**************************

        EvenementService evenementService = new EvenementService();

        // Ajouter un événement avec un lieu existant
        Evenement event1 = new Evenement(0, "Conférence Tech", "Une conférence sur les nouvelles technologies",
                "2025-05-20 10:00:00", "2025-05-20 18:00:00", 200,
                13, 7, 10, 50.0, "en attente", null, "image1.jpg");
        evenementService.ajouter(event1);


        // Ajouter un événement avec un lieu personnalisé
        Evenement event2 = new Evenement(0, "Workshop IA", "Atelier sur l'intelligence artificielle",
                "2025-06-15 09:00:00", "2025-06-15 17:00:00", 150,
                10, 7, 10, 30.0, "en attente", "Hôtel Royal", "image2.jpg");
        evenementService.ajouter(event2);


        // Modifier un événement existant
        event1.setTitre("Conférence Tech 2025");
        event1.setPrix(55.0);
        evenementService.modifier(event1);


        // Rechercher et afficher tous les événements
        List<Evenement> evenements = evenementService.rechercher();
        System.out.println("Liste des événements :");
        for (Evenement e : evenements) {
            System.out.println(e);
        }

        // Supprimer un événement
        //evenementService.supprimer(event1);

        //*********************************************cathegorie***********************************************************

//        CategoriesEventService categoryService = new CategoriesEventService();
//
//        // Ajouter une nouvelle catégorie
//        CategoriesEvent newCategory = new CategoriesEvent("fddbNNBABBACXEZA");
//        categoryService.ajouter(newCategory);
//
//        // Modifier une catégorie existante
//
//        newCategory.setNom("Updated TqetheCZEChechH");
//        categoryService.modifier(newCategory);
//
//        // Supprimer une catégorie
//        //categoryService.supprimer(newCategory);
//
//        // Rechercher et afficher toutes les catégories
//        System.out.println(categoryService.rechercher());


//**************************************************Reservations*************************************************************************

        ReservationsService ReservationsService = new ReservationsService();


        int userId = 10;
        int eventId = 91;

        // Créer une réservation initiale (1 billet)
        com.esprit.models.Reservations Reservations = new com.esprit.models.Reservations(0, eventId, userId, 1, 0.0, "en_attente");
        ReservationsService.ajouter(Reservations);

        // Afficher toutes les réservation
        ReservationsService.rechercher().forEach(System.out::println);

        // Confirmer l'achat avec une nouvelle quantité
        ReservationsService.confirmerAchat(Reservations.getId(), 3);

        // Afficher toutes les réservations après confirmation
        System.out.println("Liste des réservations après confirmation :");
        ReservationsService.rechercher().forEach(System.out::println);
    }

    }


