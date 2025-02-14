package com.esprit.tests;
import com.esprit.models.*;
import com.esprit.services.*;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class MainProg {
    public static void main(String[] args) {
        //********************************MODULE STORE************************************************

        //CRUD categorie
//        CategorieProduitService cs = new CategorieProduitService();
//        cs.ajouter(new categorie_produit("kfjkd", "dsffsd"));
//        cs.modifier(new categorie_produit(1,"Youssef", "Ahmed"));
//        cs.supprimer(new categorie_produit(1,"", ""));
//        System.out.println(cs.rechercher());

        /*****************************************************************************************/
        // CRUD produit
//        ProduitService ps = new ProduitService();
//        ps.ajouter(new produit("ttt","wsdhghd",15.90,10,10));
//        ps.modifier(new produit(1,"ooo","tt",15.90,15,4));
//        ps.supprimer(new produit(2,"ooo","tt",15.90,15,4));
//        System.out.println(ps.rechercher());

        /************************************************************************************************/
        //CRUD  commande
//        CommandeService cos = new CommandeService();
//        cos.ajouter(new commande(10, 12,25));
//        cos.modifier(new commande(1,1,1,3));
//        cos.supprimer(new commande(1));
//        System.out.println(cos.rechercher());

        //********************************MODULE STORE************************************************


        //********************************MODULE MATERIEL************************************************
//         MaterielService ps = new MaterielService();
//       ps.ajouter(new Materiel("flamme","tente ",10,4,70,"link"));
//         ps.modifier(new Materiel(1,"test","chaise", 20,6,2,"im"));
//         ps.supprimer(new Materiel(2));
//        System.out.println(ps.rechercher());
        ///////////////////////////////////////////////////////////////////
//        CategorieService cs = new CategorieService();
//         cs.ajouter(new Categorie("Matériel de Restauration"));
//         cs.ajouter(new Categorie("sport"));
//        cs.modifier(new Categorie(4,"Éclairage & Électricité"));
//          cs.supprimer(new Categorie(3));
//        System.out.println(cs.rechercher());
        //////////////////////////////////////////////
        //ReservationService rs = new ReservationService();
        //rs.ajouter(new Reservation(10,1,67));
        //rs.modifier(new Reservation(21,10,9,67));
        //rs.supprimer(new Reservation(20));
        //System.out.println(rs.rechercher());
        //********************************MODULE MATERIEL************************************************


        //********************************MODULE FORUM************************************************

//        //Post CRUD
        //PostService ps = new PostService();
//        // Adding
        //ps.ajouter(new Post(10,"Test Title", "Test Content", "Test Author", 10));
//        // Editing
        //ps.modifier(new Post(13, "Updated Title", "Updated Content"));
//        //Showing
        //ps.rechercher();
//        // Deleting
        //ps.supprimer(new Post(13));

//
//        //Comment CRUD
//        CommentService cs = new CommentService();
//        //adding
//        cs.ajouter(new Comment(13,10,"ksskkkk"));
//        //editing
//        cs.modifier(new Comment(10, "He is missing"));
//        //showing
//        System.out.println("\nAll comments:");
//        cs.rechercher();
//        //deleting
//        cs.supprimer(new Comment(56, null));


//        //Like CRUD
//        LikeService ls = new LikeService();
//        //Adding
//        //ls.ajouter(new Like(13, 10));
//
//        //Showing
//        ls.rechercher();
//        //Removing
//        ls.supprimer(new Like(13, 10));

//        //Category CRUD
//        CategoryService cats = new CategoryService();
//        //Adding
//        cats.ajouter(new Category("warnings", "warning posts"));
//        //Editing
//        cats.modifier(new Category(10, "announcements", "announcement posts"));
//        //Showing
//        System.out.println("\nAll categories:");
//        cats.rechercher();
//        //Deletings
//        cats.supprimer(new Category(1, null, null));
//        //Showing again
//        System.out.println("\nAfter deletion:");
//        cats.rechercher();
        //********************************MODULE FORUM************************************************

//        //***************************************************  EVENT   **************************
//
//          EvenementService evenementService = new EvenementService();
//
//        // Ajouter un événement avec un lieu existant
//        Evenement event1 = new Evenement(0, "Conférence Tech", "Une conférence sur les nouvelles technologies",
//                "2025-05-20 10:00:00", "2025-05-20 18:00:00", 200,
//                13, 7, 10, 50.0, "en attente", null, "image1.jpg");
//        evenementService.ajouter(event1);
//
//
//        // Ajouter un événement avec un lieu personnalisé
//        Evenement event2 = new Evenement(0, "Workshop IA", "Atelier sur l'intelligence artificielle",
//                "2025-06-15 09:00:00", "2025-06-15 17:00:00", 150,
//                10, 7, 10, 30.0, "en attente", "Hôtel Royal", "image2.jpg");
//        evenementService.ajouter(event2);
//
//
//        // Modifier un événement existant
//        event1.setTitre("Conférence Tech 2025");
//        event1.setPrix(55.0);
//        evenementService.modifier(event1);
//
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
//
//        //*********************************************cathegorie***********************************************************
//
////        CategoriesEventService categoryService = new CategoriesEventService();
////
////        // Ajouter une nouvelle catégorie
////        CategoriesEvent newCategory = new CategoriesEvent("fddbNNBABBACXEZA");
////        categoryService.ajouter(newCategory);
////
////        // Modifier une catégorie existante
////
////        newCategory.setNom("Updated TqetheCZEChechH");
////        categoryService.modifier(newCategory);
////
////        // Supprimer une catégorie
////        //categoryService.supprimer(newCategory);
////
////        // Rechercher et afficher toutes les catégories
////        System.out.println(categoryService.rechercher());
//
//
////**************************************************Reservations*************************************************************************
//
//       ReservationsService ReservationsService = new ReservationsService();
//
//        // Simuler un utilisateur connecté (ID = 1) et un événement (ID = 1)
//        int userId = 10;
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
//        //***************************************************event**************************
//********************************************************* user *******************************
//        Participant participant = new Participant();
//        participant.setNom("Doe");
//        participant.setPrenom("John");
//        participant.setUserName("johndoe");
//        participant.setphone(123456789);
//        participant.setEmail("john.doe@example.com");
//        participant.setPasswd("password123");
//        participant.setImage("fffff");
//        participant.setActive(true);
//        participant.setRole(Role.Admin);
//
//        ParticipantService participantService=new ParticipantService();
//        participantService.ajouter(participant);
//        participant.setNom("aaaa");
//        participant.setUserID(20);
//        participantService.modifier(participant);
//        //participantService.supprimer(participant);
//        //System.out.println(participantService.rechercher());
//        //*****************************  user   ********************************
//*****************************  module lieux   ********************************
      /* Connection connection = DataSource.getInstance().getConnection();
        CategorieServiceImpl categorieService = new CategorieServiceImpl();
        LieuServiceImpl lieuService = new LieuServiceImpl();
        ReservationServiceImpl reservationService = new ReservationServiceImpl();
        PhotoLieuServiceImpl photoService = new PhotoLieuServiceImpl();

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\n====== MENU PRINCIPAL ======");
            System.out.println("1. Gestion des catégories");
            System.out.println("2. Gestion des lieux");
            System.out.println("3. Gestion des réservations");
            System.out.println("4. Gestion des photos");
            System.out.println("5. Quitter");
            System.out.print("Votre choix : ");
            int choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1:
                    manageCategories(categorieService);
                    break;
                case 2:
                    manageLieux(categorieService, lieuService);
                    break;
                case 3:
                    manageReservations(reservationService);
                    break;
                case 4:
                    managePhotos(photoService, lieuService);
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("Option invalide. Réessayez.");
            }
        }
        System.out.println("\n====== Fin du programme ======");
        scanner.close();
    }

    private static void manageCategories(CategorieServiceImpl categorieService) {
        System.out.println("\n====== GESTION DES CATÉGORIES DE SALLE ======");
        System.out.println("Catégories existantes :");
        categorieService.rechercher().forEach(System.out::println);
        String[] nomsCategories = {"Salle plein air", "Salle de Formation", "Salle de Spectacle"};
        String[] descriptionsCategories = {
                "Grande salle en plein air",
                "Salle équipée pour des formations professionnelles",
                "Salle adaptée pour des spectacles et performances"
        };
        categorieService.addCategories(nomsCategories, descriptionsCategories);
        System.out.println("\nCatégories après ajout :");
        categorieService.rechercher().forEach(System.out::println);
        categorieService.modifyFirstCategory("Salle de Conférence VIP", "Espace premium équipé de technologies de pointe");
        System.out.println("\nCatégories après modification :");
        categorieService.rechercher().forEach(System.out::println);
        categorieService.deleteLastCategory();
        System.out.println("\nCatégories après suppression :");
        categorieService.rechercher().forEach(System.out::println);
    }

    private static void manageLieux(CategorieServiceImpl categorieService, LieuServiceImpl lieuService) {
        System.out.println("\n====== GESTION DES LIEUX ======");
        List<categorie_salle> categoriesPourLieux = categorieService.rechercher();
        if (categoriesPourLieux.isEmpty()) {
            System.out.println("Aucune catégorie disponible pour associer aux lieux.");
            return;
        }
        System.out.println("Lieux existants :");
        lieuService.afficherLieux();
        List<Lieu> lieuxExistants = lieuService.rechercher();
        if (lieuxExistants.isEmpty()) {
            Lieu nouveauLieu = new Lieu("Palais des Événements", "Avenue des Célébrations", "Tunis", "1001", 500, "palais_evenements.png", categoriesPourLieux.get(0), 1500.0);
            System.out.println("\nTentative d'ajout du lieu : " + nouveauLieu);
            lieuService.ajouter(nouveauLieu);
            System.out.println("\nLieux après ajout :");
            lieuService.afficherLieux();
        }
        Lieu premierLieu = lieuService.getPremierLieu();
        if (premierLieu != null) {
            String ancienNom = premierLieu.getNom();
            String ancienneAdresse = premierLieu.getAdresse();
            premierLieu.setNom("Palais des Événements VIP");
            premierLieu.setAdresse("Avenue des Célébrations VIP");
            System.out.println("\nTentative de modification du premier lieu :");
            System.out.println("Ancien nom: " + ancienNom + " → Nouveau nom: " + premierLieu.getNom());
            System.out.println("Ancienne adresse: " + ancienneAdresse + " → Nouvelle adresse: " + premierLieu.getAdresse());
            lieuService.modifier(premierLieu);
            System.out.println("\nLieux après modification :");
            lieuService.afficherLieux();
        }
        List<Lieu> tousLesLieux = lieuService.rechercher();
        if (tousLesLieux.size() > 1) {
            Lieu dernierLieu = lieuService.getDernierLieu();
            System.out.println("\nTentative de suppression du dernier lieu : " + dernierLieu);
            lieuService.supprimer(dernierLieu);
            System.out.println("\nLieux après suppression :");
            lieuService.afficherLieux();
        } else {
            System.out.println("\nImpossible de supprimer le lieu, il doit en rester au moins un.");
        }
    }

    private static void manageReservations(ReservationServiceImpl reservationService) {
        System.out.println("\n====== GESTION DES RÉSERVATIONS ======");

        // Assurez-vous ici que l'ID 67 correspond à un événement existant
        int validLieuId = 7;
        int validEventId1 = 67;

        // Définir des dates qui respectent la période de l'événement associé à l'ID 67
        // (Adaptez ces dates en fonction des données réelles dans votre base)
        LocalDateTime reservationDebut = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime reservationFin   = LocalDateTime.of(2025, 3, 1, 12, 0);

        // 1. Ajout d'une première réservation par défaut (qui respecte la période de l'événement)
        reservation1 res1 = new reservation1(0, validLieuId, validEventId1,
                reservationDebut, reservationFin);
        reservationService.ajouterEtAfficher(res1);

        // 2. Tenter d'ajouter une réservation avec un événement inexistant (pour tester la validation)
        int invalidEventId = 63;
        reservation1 res2 = new reservation1(0, validLieuId, invalidEventId,
                reservationDebut.plusDays(1), reservationFin.plusDays(1));
        reservationService.ajouterEtAfficher(res2);

        // 3. Modification de la première réservation (changement d'horaires dans la période autorisée)
        reservation1 modifiedReservation = new reservation1(res1.getId(), validLieuId, validEventId1,
                reservationDebut.plusHours(1), reservationFin.plusHours(1));
        reservationService.modifierEtAfficher(modifiedReservation);

        // 4. Suppression de la première réservation
        reservationService.supprimerEtAfficher(res1);

        // 5. Récupération et affichage de tous les événements depuis la table events
        System.out.println("\n====== LISTE DES ÉVÉNEMENTS ======");
        List<java.util.Map<String, Object>> evenements = reservationService.getAllEvenements();
        for (java.util.Map<String, Object> event : evenements) {
            System.out.println(event);
        }

        // 6. Récupération et affichage de tous les lieux depuis la table lieu
        System.out.println("\n====== LISTE DES LIEUX ======");
        List<java.util.Map<String, Object>> lieux = reservationService.getAllLieux();
        for (java.util.Map<String, Object> lieu : lieux) {
            System.out.println(lieu);
        }
    }



    private static void managePhotos(PhotoLieuServiceImpl photoService, LieuServiceImpl lieuService) {
        System.out.println("\n====== GESTION DES PHOTOS DE LIEUX ======");

        // Récupérer la liste des lieux existants
        List<Lieu> lieux = lieuService.rechercher();
        if (lieux.isEmpty()) {
            System.out.println("Aucun lieu existant. Veuillez ajouter un lieu d'abord.");
            return;
        }
        // Choix du premier lieu pour le test
        Lieu testLieu = lieux.get(0);
        System.out.println("Lieu sélectionné pour le test des photos : " + testLieu);

        // Test : Ajouter deux photos pour le lieu sélectionné
        PhotoLieu photo1 = new PhotoLieu(testLieu.getId(), "chemin/vers/image1.jpg");
        PhotoLieu photo2 = new PhotoLieu(testLieu.getId(), "chemin/vers/image2.jpg");
        photoService.ajouter(photo1);
        photoService.ajouter(photo2);

        // Afficher les photos du lieu
        List<PhotoLieu> photos = photoService.rechercherParLieu(testLieu.getId());
        System.out.println("Photos associées au lieu " + testLieu.getId() + " :");
        for (PhotoLieu p : photos) {
            System.out.println(p);
        }

        // Test : Modifier la première photo
        photo1.setUrlImage("chemin/vers/image1_modifiee.jpg");
        photoService.modifier(photo1);
        photos = photoService.rechercherParLieu(testLieu.getId());
        System.out.println("Photos après modification :");
        for (PhotoLieu p : photos) {
            System.out.println(p);
        }

        // Test : Supprimer la deuxième photo
        photoService.supprimer(photo2);
        photos = photoService.rechercherParLieu(testLieu.getId());
        System.out.println("Photos après suppression de la deuxième photo :");
        for (PhotoLieu p : photos) {
            System.out.println(p);
        }

*/

    }

}


