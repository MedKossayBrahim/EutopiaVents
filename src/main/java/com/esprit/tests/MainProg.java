package com.esprit.tests;

import com.esprit.models.Participant;
import com.esprit.models.Role;
import com.esprit.services.ParticipantService;

public class MainProg {
    public static void main(String[] args) {
        //********************************MODULE MATERIEL************************************************
        // MaterielService ps = new MaterielService();
//       ps.ajouter(new Materiel("flamme","tente ",10,4,70,"link"));
        // ps.modifier(new Materiel(3,"chaise", "chaise",6,2));
        // ps.supprimer(new Materiel(6));
        //System.out.println(ps.rechercher());
        ///////////////////////////////////////////////////////////////////
        //CategorieService cs = new CategorieService();
//         cs.ajouter(new Categorie("Matériel de Restauration"));
        // cs.ajouter(new Categorie("sport"));
        //cs.modifier(new Categorie(4,"Éclairage & Électricité"));
        //  cs.supprimer(new Categorie(3));
        //System.out.println(cs.rechercher());
        //////////////////////////////////////////////
        // ReservationService rs = new ReservationService();
        // rs.ajouter(new Reservation(10,1,62));
//         //rs.modifier(new Reservation(7,10,9,1));
        // rs.supprimer(new Reservation(7));
        // System.out.println(rs.rechercher());
        //********************************MODULE MATERIEL************************************************


        //********************************MODULE FORUM************************************************

        //Post CRUD
        // PostService ps = new PostService();
        // Adding
        //ps.ajouter(new com.eutopia.models.Post(10,"Test Title", "Test Content", "Test Author", 7));
        // Editing
        //ps.modifier(new Post(32, "Updated Title", "Updated Content"));
        //Showing
        //ps.rechercher();
        // Deleting
        //ps.supprimer(new Post(32));


        //Comment CRUD
        //CommentService cs = new CommentService();
        //adding
        //cs.ajouter(new com.eutopia.models.Comment(31,10,"ksskkkk"));
        //editing
        //cs.modifier(new Comment(56, "He is missing"));
        //showing
        //System.out.println("\nAll comments:");
        //cs.rechercher();
        //deleting
        //cs.supprimer(new Comment(56, null));


        //Like CRUD
        //LikeService ls = new LikeService();
        //Adding
        //ls.ajouter(new Like(31, 1));
        //Showing
        //ls.rechercher();
        //Removing
        //ls.supprimer(new Like(31, 1));

        //Category CRUD
        //CategoryService cats = new CategoryService();
        //Adding
        //cats.ajouter(new Category("warnings", "warning posts"));
        //Editing
        //cats.modifier(new Category(1, "announcements", "announcement posts"));
        //Showing
        //System.out.println("\nAll categories:");
        //cats.rechercher();
        //Deletings
        //cats.supprimer(new Category(1, null, null));
        //Showing again
        //System.out.println("\nAfter deletion:");
        //cats.rechercher();
        //********************************MODULE FORUM************************************************

        //***************************************************event**************************

        //  EvenementService evenementService = new EvenementService();
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

//       ReservationsService ReservationsService = new ReservationsService();
//
//        // Simuler un utilisateur connecté (ID = 1) et un événement (ID = 1)
//        int userId = 1;
//        int eventId = 67;
//
//        // Créer une réservation initiale (1 billet)
//        com.esprit.models.Reservations Reservations = new com.esprit.models.Reservations(0, eventId, userId, 1, 0.0, "en_attente");
//        ReservationsService.ajouter(Reservations);
//
//        // Afficher toutes les réservations
//        System.out.println("Liste des réservations avant confirmation :");
//        ReservationsService.rechercher().forEach(System.out::println);
//
//        // Confirmer l'achat avec une nouvelle quantité
//        ReservationsService.confirmerAchat(Reservations.getId(), 3);
//
//        // Afficher toutes les réservations après confirmation
//        System.out.println("Liste des réservations après confirmation :");
//        ReservationsService.rechercher().forEach(System.out::println);
        //***************************************************event**************************
//********************************************************* user *******************************
        Participant participant = new Participant();
        participant.setNom("Doe");
        participant.setPrenom("John");
        participant.setUserName("johndoe");
        participant.setphone(123456789);
        participant.setEmail("john.doe@example.com");
        participant.setPasswd("password123");
        participant.setImage("fffff");
        participant.setActive(true);
        participant.setRole(Role.Admin);

        ParticipantService participantService=new ParticipantService();
        participantService.ajouter(participant);
        participant.setNom("aaaa");
        participant.setUserID(20);
        participantService.modifier(participant);
        //participantService.supprimer(participant);
        //System.out.println(participantService.rechercher());
        //*****************************  user   ********************************


    }
}
