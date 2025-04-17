package tn.esprit.Models;

import java.time.LocalDateTime;

public class Categories {
    private int id;
    private String nom;
    private String description;
    private String cover;
    private LocalDateTime date_creation	;


    public Categories(int id, String nom, String description, String cover, LocalDateTime date_creation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.cover = cover;
        this.date_creation = date_creation;
    }

    public Categories(String nom, String description, String cover, LocalDateTime date_creation) {
        this.nom = nom;
        this.description = description;
        this.cover = cover;
        this.date_creation = date_creation;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public LocalDateTime getDate_creation() {
        return date_creation;
    }
    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Categorie => Id = " + id + ", Nom = " + nom + ", Description = " + description +
                ", Cover = " + cover + ", Date = " + date_creation;
    }

}
