package tn.esprit.Models;

import java.time.LocalDateTime;

public class Abonnements {
    private int id;
    private String nom;
    private Double prix;
    private String avantages;
    private String type	;

    public Abonnements(int id, String nom, Double prix, String avantages, String type) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.avantages = avantages;
        this.type = type;
    }

    public Abonnements(String nom,Double prix,String avantages,String type){
        this.nom = nom;
        this.prix = prix;
        this.avantages = avantages;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public Double getPrix() {
        return prix;
    }
    public void setPrix(Double prix) {
        this.prix = prix;
    }
    public String getAvantages() {
        return avantages;
    }
    public void setAvantages(String avantages) {
        this.avantages = avantages;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Abonnement => Id = "+ id +", Nom = " + nom + ", Prix = " + prix + ", Avantages = " + avantages + ", Type = " + type;
    }
}
