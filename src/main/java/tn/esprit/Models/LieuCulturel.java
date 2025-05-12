package tn.esprit.Models;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

@Data
public class LieuCulturel {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty villeId = new SimpleLongProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty link3d = new SimpleStringProperty();
    private final StringProperty cover = new SimpleStringProperty();
    
    public LieuCulturel() {}
    
    public LieuCulturel(Long id, Long villeId, String nom, String description, String link3d, String cover) {
        setId(id);
        setVilleId(villeId);
        setNom(nom);
        setDescription(description);
        setLink3d(link3d);
        setCover(cover);
    }
    
    // ID
    public Long getId() {
        return id.get();
    }
    
    public void setId(Long id) {
        this.id.set(id != null ? id : 0);
    }
    
    public LongProperty idProperty() {
        return id;
    }
    
    // Ville ID
    public Long getVilleId() {
        return villeId.get();
    }
    
    public void setVilleId(Long villeId) {
        this.villeId.set(villeId != null ? villeId : 0);
    }
    
    public LongProperty villeIdProperty() {
        return villeId;
    }
    
    // Nom
    public String getNom() {
        return nom.get();
    }
    
    public void setNom(String nom) {
        this.nom.set(nom != null ? nom : "");
    }
    
    public StringProperty nomProperty() {
        return nom;
    }
    
    // Description
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description != null ? description : "");
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    // Link3D
    public String getLink3d() {
        return link3d.get();
    }
    
    public void setLink3d(String link3d) {
        this.link3d.set(link3d != null ? link3d : "");
    }
    
    public StringProperty link3dProperty() {
        return link3d;
    }
    
    // Cover
    public String getCover() {
        return cover.get();
    }
    
    public void setCover(String cover) {
        this.cover.set(cover != null ? cover : "");
    }
    
    public StringProperty coverProperty() {
        return cover;
    }
} 