package tn.esprit.Models;

import java.time.LocalDateTime;

public class Community {
    private int id;
    private int id_categorie_id;
    private String nom;
    private String description;
    private String cover;
    private LocalDateTime created_at;
    private int nbr_membre;
    private byte statut;

    public Community(int id, int id_categorie_id, String nom, String description, String cover, LocalDateTime created_at, int nbr_membre, byte statut) {
        this.id = id;
        this.id_categorie_id = id_categorie_id;
        this.nom = nom;
        this.description = description;
        this.cover = cover;
        this.created_at = created_at;
        this.nbr_membre = nbr_membre;
        this.statut = statut;
    }

    public Community(int id_categorie_id, String nom, String description, String cover, LocalDateTime created_at, int nbr_membre, byte statut) {
        this.id_categorie_id = id_categorie_id;
        this.nom = nom;
        this.description = description;
        this.cover = cover;
        this.created_at = created_at;
        this.nbr_membre = nbr_membre;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_categorie_id() {
        return id_categorie_id;
    }

    public void setId_categorie_id(int id_categorie_id) {
        this.id_categorie_id = id_categorie_id;
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

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public int getNbr_membre() {
        return nbr_membre;
    }

    public void setNbr_membre(int nbr_membre) {
        this.nbr_membre = nbr_membre;
    }

    public byte getstatut() {
        return statut;
    }

    public void setstatut(byte statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return nom;
    }
}
