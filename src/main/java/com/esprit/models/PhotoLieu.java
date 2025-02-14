package com.esprit.models;

public class PhotoLieu {
    private int id;
    private int lieuId;
    private String urlImage;

    public PhotoLieu() {
    }

    public PhotoLieu(int id, int lieuId, String urlImage) {
        this.id = id;
        this.lieuId = lieuId;
        this.urlImage = urlImage;
    }

    public PhotoLieu(int lieuId, String urlImage) {
        this.lieuId = lieuId;
        this.urlImage = urlImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLieuId() {
        return lieuId;
    }

    public void setLieuId(int lieuId) {
        this.lieuId = lieuId;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    @Override
    public String toString() {
        return "PhotoLieu{" +
                "id=" + id +
                ", lieuId=" + lieuId +
                ", urlImage='" + urlImage + '\'' +
                '}';
    }
}
