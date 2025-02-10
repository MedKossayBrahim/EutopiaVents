package com.esprit.tests;

import com.esprit.models.*;
import com.esprit.services.*;

public class MainProg {
    public static void main(String[] args) {
        //  Connection connection = DataSource.getInstance().getConnection();
        // CategorieServiceImpl categorieService = new CategorieServiceImpl();
        // LieuServiceImpl lieuService = new LieuServiceImpl();
        //ReservationServiceImpl reservationService = new ReservationServiceImpl();
        // Scanner scanner = new Scanner(System.in);
        //  boolean exit = false;
        //  while (!exit) {
        //  System.out.println("\n====== MENU PRINCIPAL ======");
        //  System.out.println("1. Gestion des catégories");
        //   System.out.println("2. Gestion des lieux");
        //   System.out.println("3. Gestion des réservations");
        //   System.out.println("4. Quitter");
        //   System.out.print("Votre choix : ");
        //   int choix = scanner.nextInt();
        //   scanner.nextLine();

        //   switch (choix) {
        //     case 1:
        //     manageCategories(categorieService);
        //   break;
        //    case 2:
        //  manageLieux(categorieService, lieuService);
        //      break;
        //   case 3:
        //       manageReservations(reservationService);
        //       break;
        //  case 4:
        //     exit = true;
        //     break;
        //  default:
        //      System.out.println("Option invalide. Réessayez.");
        //   }
        //  }
        //  System.out.println("\n====== Fin du programme ======");
        //  scanner.close();

    }

    //private static void manageCategories(CategorieServiceImpl categorieService) {
    // System.out.println("\n====== GESTION DES CATÉGORIES DE SALLE ======");
    // Affichage des catégories existantes
    //   List<categorie_salle> categories = categorieService.rechercher();
    //  System.out.println("Catégories existantes :");
    //  categories.forEach(System.out::println);

    // Tentative d'ajout de nouvelles catégories
    //   String[] nomsCategories = {"Salle de Conférence", "Salle de Formation", "Salle de Spectacle"};
    // String[] descriptionsCategories = {
    //     "Grande salle pour conférences et séminaires",
    //    "Salle équipée pour des formations professionnelles",
    //    "Salle adaptée pour des spectacles et performances"
    //  };

    //   for (int i = 0; i < nomsCategories.length; i++) {
    //    categorie_salle nouvelleCategorieSalle = new categorie_salle(nomsCategories[i], descriptionsCategories[i]);
    //    System.out.println("\nTentative d'ajout de la catégorie : " + nouvelleCategorieSalle);
    //      categorieService.ajouter(nouvelleCategorieSalle);
    // }

    //  System.out.println("\nCatégories après tentatives d'ajout :");
    //  categorieService.rechercher().forEach(System.out::println);

    // Modification d'une catégorie existante
    //   List<categorie_salle> categoriesExistantes = categorieService.rechercher();
    //   if (!categoriesExistantes.isEmpty()) {
    //  categorie_salle categorieSalleAModifier = categoriesExistantes.get(0);
    //  String ancienNom = categorieSalleAModifier.getNom();
    //  String ancienneDescription = categorieSalleAModifier.getDescription();
    //   String nouveauNom = "Salle de Conférence VIP";
    //  String nouvelleDescription = "Espace premium équipé de technologies de pointe";

    //  categorieSalleAModifier.setNom(nouveauNom);
    //   categorieSalleAModifier.setDescription(nouvelleDescription);

    //  System.out.println("\nTentative de modification de la catégorie :");
    //   System.out.println("Ancien nom: " + ancienNom + " → Nouveau nom: " + nouveauNom);
    //   System.out.println("Ancienne description: " + ancienneDescription + " → Nouvelle description: " + nouvelleDescription);
    //   categorieService.modifier(categorieSalleAModifier);

    //  System.out.println("\nCatégories après modification :");
    //   categorieService.rechercher().forEach(System.out::println);
    //   }

    // Suppression d'une catégorie
    //   categoriesExistantes = categorieService.rechercher();
    //   if (!categoriesExistantes.isEmpty()) {
    //   categorie_salle categorieSalleASupprimer = categoriesExistantes.get(categoriesExistantes.size() - 1);
    //   System.out.println("\nTentative de suppression de la catégorie : " + categorieSalleASupprimer);
    //   categorieService.supprimer(categorieSalleASupprimer);

    //  System.out.println("\nCatégories après tentative de suppression :");
    //   categorieService.rechercher().forEach(System.out::println);
    //   }
    //  }
    //   private static void manageLieux(CategorieServiceImpl categorieService, LieuServiceImpl lieuService) {
    //  System.out.println("\n====== GESTION DES LIEUX ======");
    //  List<categorie_salle> categoriesPourLieux = categorieService.rechercher();
    //  if (categoriesPourLieux.isEmpty()) {
    //   System.out.println("Aucune catégorie disponible pour associer aux lieux.");
    //      return;
    //  }
    // Vérifiez les lieux existants
    //  List<Lieu> lieuxExistants = lieuService.rechercher();
    //  System.out.println("Lieux existants :");
    //  lieuxExistants.forEach(System.out::println);

    // Ajout d'un nouveau lieu seulement s'il n'y a pas déjà de lieu
    //  if (lieuxExistants.isEmpty()) {
    // Lieu nouveauLieu = new Lieu(
    //    "Palais des Événements",
    //     "Avenue des Célébrations",
    //     "Tunis",
    //     "1001",
    //    500,
    //      "palais_evenements.png",
    //       categoriesPourLieux.get(0), // Association avec la première catégorie disponible
    //          1500.0
    //    );
    //    System.out.println("\nTentative d'ajout du lieu : " + nouveauLieu);
    //    lieuService.ajouter(nouveauLieu);
    //   lieuxExistants = lieuService.rechercher(); // Récupérer les lieux après ajout
    //   System.out.println("\nLieux après tentative d'ajout :");
    //   lieuxExistants.forEach(System.out::println);
    //    }

    // Modification d'un lieu existant
    //  if (!lieuxExistants.isEmpty()) {
    //    Lieu lieuAModifier = lieuxExistants.get(0);
    //   String ancienNom = lieuAModifier.getNom();
    //  String ancienneAdresse = lieuAModifier.getAdresse();
    //   String nouveauNom = "Palais des Événements VIP";
    //   String nouvelleAdresse = "Avenue des Célébrations VIP";

    //   lieuAModifier.setNom(nouveauNom);
    //    lieuAModifier.setAdresse(nouvelleAdresse);

    //  System.out.println("\nTentative de modification du lieu :");
    //   System.out.println("Ancien nom: " + ancienNom + " → Nouveau nom: " + nouveauNom);
    //   System.out.println("Ancienne adresse: " + ancienneAdresse + " → Nouvelle adresse: " + nouvelleAdresse);
    //   lieuService.modifier(lieuAModifier);

    //  System.out.println("\nLieux après modification :");
    //   lieuService.rechercher().forEach(System.out::println);
    //    }

    // Ne supprimer un lieu que s'il y en a plus d'un
    //  lieuxExistants = lieuService.rechercher();
    //     if (lieuxExistants.size() > 1) {
    // Lieu lieuASupprimer = lieuxExistants.get(lieuxExistants.size() - 1);
    //  System.out.println("\nTentative de suppression du lieu : " + lieuASupprimer);
    //  lieuService.supprimer(lieuASupprimer);

    //  System.out.println("\nLieux après tentative de suppression :");
    //    lieuService.rechercher().forEach(System.out::println);
    //    } else {
    //    System.out.println("\nImpossible de supprimer le lieu, il doit en rester au moins un.");
    //  }
    //   }
    //  private static void manageReservations(ReservationServiceImpl reservationService) {
    //  System.out.println("\n====== GESTION DES RÉSERVATIONS ======");

    // Utilisation de lieu_id 7 et d'événements existants (62 et 63)
    //   int validLieuId = 7;
    //  int validEventId1 = 62;
    //  int validEventId2 = 63;

    // Ajouter des réservations
    //   reservation1 reservation1 = new reservation1(0, validLieuId, validEventId1, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
    //   reservationService.ajouter(reservation1);

    //  reservation1 reservation2 = new reservation1(0, validLieuId, validEventId2, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3));
    //  reservationService.ajouter(reservation2);

    // Afficher la liste des réservations après ajout
    //  System.out.println("Liste des réservations après ajout :");
    //  for (reservation1 reservation : reservationService.rechercher()) {
    //       System.out.println(reservation);
//}

// Modifier la première réservation
//  reservation1 modifiedReservation = new reservation1(reservation1.getId(), validLieuId, validEventId1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));
//  reservationService.modifier(modifiedReservation);
//   System.out.println("Réservation modifiée : " + modifiedReservation);

// Afficher la liste des réservations après modification
//  System.out.println("Liste des réservations après modification :");
//  for (reservation1 reservation : reservationService.rechercher()) {
//      System.out.println(reservation);
//     }

// Supprimer la première réservation
//    reservationService.supprimer(reservation1);
//    System.out.println("Réservation supprimée : " + reservation1);

// Afficher la liste des réservations après suppression
//  System.out.println("Liste des réservations après suppression :");
//   for (reservation1 reservation : reservationService.rechercher()) {
//       System.out.println(reservation);
//   }

    }

