package models;



public class Consommation {

    private int id_con;
    private int id_cov;
    private float energie_con;
    private float prix_unite;
    private int id_user;



public Consommation(int id_con,float energie_con,float prix_unite,int id_cov) {
    this.id_con = id_con;
    this.energie_con = energie_con;
    this.prix_unite = prix_unite;
    this.id_cov = id_cov;

}
    public Consommation ( int id_con, int id_cov, float energie_con, float prix_unite, int id_user) {

        this.id_con = id_con;
        this.id_cov = id_cov;
        this.energie_con = energie_con;
        this.prix_unite = prix_unite;
        this.id_user = id_user;

    }

    public Consommation(int idCov, float energieCon, float prixUnite) {
        this.id_cov = idCov; // Ensure this line is present
        this.energie_con = energieCon;
        this.prix_unite = prixUnite;
    }

    public int getId_con() {
        return id_con;
    }

    public void setId_con(int id_con) {
        this. id_con =  id_con;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public float getEnergie_con() {
        return energie_con;
    }

    public void setEnergie_con(float energie_con) {
        this.energie_con = energie_con;
    }

    public float getPrix_unite() {
        return prix_unite;
    }

    public void setPrix_unite(float prix_unite) {this.prix_unite = prix_unite;}

    public int getId_cov() {
        return id_cov;
    }

    public void setId_cov(int id_cov) {
        this. id_cov=  id_cov;
    }




    @Override
    public String toString() {
        return "Consommation{" +
                "id_con=" +  id_con +
                ", id_cov='" + id_cov + '\'' +
                ", energie_con='" + energie_con + '\'' +
                ", prix_unite='" + prix_unite + '\'' +
                ", id_user='" + id_user + '\'' +

                '}';
    }
}
