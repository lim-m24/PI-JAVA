package tn.esprit.Models;

public class Gamification {
    private int id;
    private int typeAbonnement;
    private String nom;
    private String description;
    private String type;
    private int conditionGamification;


    public Gamification(int id, int typeAbonnement, String nom, String description, String type, int conditionGamification) {
        this.id = id;
        this.typeAbonnement = typeAbonnement;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.conditionGamification = conditionGamification;
    }

    public Gamification(int typeAbonnement, String nom, String description, String type, int conditionGamification) {
        this.typeAbonnement = typeAbonnement;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.conditionGamification = conditionGamification;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeAbonnement() {
        return typeAbonnement;
    }

    public void setTypeAbonnement(int typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getConditionGamification() {
        return conditionGamification;
    }

    public void setConditionGamification(int conditionGamification) {
        this.conditionGamification = conditionGamification;
    }

    @Override
    public String toString() {
        return "Gamification => Id = " + id + ", AbonnementId = " + typeAbonnement + ", Nom = " + nom + ", Description = " + description + ", Type = " + type + ", Condition = " + conditionGamification;
    }
}
