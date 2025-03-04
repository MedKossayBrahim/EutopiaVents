package com.esprit.services;

import com.esprit.models.Lieu;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.sql.SQLException;
import java.util.*;

public class StatistiquesServiceImpl {
    private final LieuServiceImpl lieuService;

    public StatistiquesServiceImpl() throws SQLException {
        this.lieuService = new LieuServiceImpl();
    }

    public String getStatistiquesJson() {
        try {
            JSONObject statistiques = new JSONObject();
            statistiques.put("general", new JSONObject(getStatistiquesGenerales()));
            statistiques.put("categories", new JSONArray(getStatistiquesParCategorie()));
            statistiques.put("distribution", new JSONArray(getDistributionCapacite()));

            statistiques.put("topLieux", new JSONArray(getTopLieux(5)));

            return statistiques.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private Map<String, Object> getStatistiquesGenerales() {
        List<Lieu> lieux = lieuService.rechercher();
        DescriptiveStatistics stats = new DescriptiveStatistics();

        lieux.forEach(lieu -> stats.addValue(lieu.getCapacite()));

        Map<String, Object> statistiques = new HashMap<>();
        statistiques.put("nombreTotal", lieux.size());
        statistiques.put("capaciteMin", (int) stats.getMin());
        statistiques.put("capaciteMax", (int) stats.getMax());
        statistiques.put("capaciteMoyenne", Math.round(stats.getMean()));
        statistiques.put("capaciteMediane", Math.round(stats.getPercentile(50)));
        statistiques.put("capaciteTotale", (int) stats.getSum());

        return statistiques;
    }

    private List<Map<String, Object>> getStatistiquesParCategorie() {
        Map<String, List<Integer>> capacitesParCategorie = new HashMap<>();

        lieuService.rechercher().forEach(lieu -> {
            String categorie = lieu.getCategorie().getNom();
            capacitesParCategorie
                    .computeIfAbsent(categorie, k -> new ArrayList<>())
                    .add(lieu.getCapacite());
        });

        List<Map<String, Object>> statistiques = new ArrayList<>();
        capacitesParCategorie.forEach((categorie, capacites) -> {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            capacites.forEach(stats::addValue);

            Map<String, Object> categorieStats = new HashMap<>();
            categorieStats.put("categorie", categorie);
            categorieStats.put("nombre", capacites.size());
            categorieStats.put("capaciteMoyenne", Math.round(stats.getMean()));
            categorieStats.put("capaciteMax", (int) stats.getMax());
            statistiques.add(categorieStats);
        });

        return statistiques;
    }

    private List<Map<String, Object>> getDistributionCapacite() {
        Map<String, Integer> distribution = new TreeMap<>();

        lieuService.rechercher().forEach(lieu -> {
            String tranche = getTrancheCapacite(lieu.getCapacite());
            distribution.merge(tranche, 1, Integer::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        distribution.forEach((tranche, nombre) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("tranche", tranche);
            entry.put("nombre", nombre);
            result.add(entry);
        });

        return result;
    }

    private String getTrancheCapacite(int capacite) {
        if (capacite <= 50) return "0-50";
        if (capacite <= 100) return "51-100";
        if (capacite <= 200) return "101-200";
        if (capacite <= 500) return "201-500";
        return "500+";
    }



    private List<Map<String, Object>> getTopLieux(int limit) {
        return lieuService.rechercher().stream()
                .sorted(Comparator.comparingInt(Lieu::getCapacite).reversed())
                .limit(limit)
                .map(lieu -> {
                    Map<String, Object> lieuMap = new HashMap<>();
                    lieuMap.put("nom", lieu.getNom());
                    lieuMap.put("capacite", lieu.getCapacite());
                    lieuMap.put("categorie", lieu.getCategorie().getNom());
                    lieuMap.put("ville", lieu.getVille());
                    return lieuMap;
                })
                .toList();
    }
}
