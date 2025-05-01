package tn.esprit.Models;

import java.time.LocalDateTime;

public class Events {
    private int id;
    private int id_community_id;
    private String nom;
    private String description;
    private LocalDateTime started_at;
    private LocalDateTime finish_at;
    private String lieu;
    private String type;
    private String cover;
    private String link;
    private String acces;

    public Events(int id, int id_community_id, String nom, String description, LocalDateTime started_at, LocalDateTime finish_at, String lieu, String type, String cover, String link, String acces) {
        this.id = id;
        this.id_community_id = id_community_id;
        this.nom = nom;
        this.description = description;
        this.started_at = started_at;
        this.finish_at = finish_at;
        this.lieu = lieu;
        this.type = type;
        this.cover = cover;
        this.link = link;
        this.acces = acces;
    }

    public Events(int id_community_id, String nom, String description, LocalDateTime started_at, LocalDateTime finish_at, String lieu, String type, String cover, String link, String acces) {
        this.id_community_id = id_community_id;
        this.nom = nom;
        this.description = description;
        this.started_at = started_at;
        this.finish_at = finish_at;
        this.lieu = lieu;
        this.type = type;
        this.cover = cover;
        this.link = link;
        this.acces = acces;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_community_id() {
        return id_community_id;
    }

    public void setId_community_id(int id_community_id) {
        this.id_community_id = id_community_id;
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

    public LocalDateTime getStarted_at() {
        return started_at;
    }

    public void setStarted_at(LocalDateTime started_at) {
        this.started_at = started_at;
    }

    public LocalDateTime getFinish_at() {
        return finish_at;
    }

    public void setFinish_at(LocalDateTime finish_at) {
        this.finish_at = finish_at;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAcces() {
        return acces;
    }

    public void setAcces(String acces) {
        this.acces = acces;
    }

    @Override
    public String toString() {
        return nom;
    }
}