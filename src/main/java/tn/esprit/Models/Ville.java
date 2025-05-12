package tn.esprit.Models;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

@Data
public class Ville {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    
    public Ville() {}
    
    public Ville(Long id, String nom, String description, String position) {
        setId(id);
        setNom(nom);
        setDescription(description);
        setPosition(position);
    }
    
    // ID
    public Long getId() {
        return id.get();
    }
    
    public void setId(Long id) {
        this.id.set(id);
    }
    
    public LongProperty idProperty() {
        return id;
    }
    
    // Nom
    public String getNom() {
        return nom.get();
    }
    
    public void setNom(String nom) {
        this.nom.set(nom);
    }
    
    public StringProperty nomProperty() {
        return nom;
    }
    
    // Description
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    // Position
    public String getPosition() {
        return position.get();
    }
    
    public void setPosition(String position) {
        this.position.set(position);
    }
    
    public StringProperty positionProperty() {
        return position;
    }
} 