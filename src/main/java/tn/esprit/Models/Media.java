package tn.esprit.Models;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Media {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty lieuId = new SimpleLongProperty();
    private final StringProperty link = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    
    public Media() {}
    
    public Media(Long id, Long lieuId, String link, String type) {
        this.id.set(id);
        this.lieuId.set(lieuId);
        this.link.set(link);
        this.type.set(type);
    }
    
    // ID
    public Long getId() {
        return id.get();
    }
    
    public LongProperty idProperty() {
        return id;
    }
    
    public void setId(Long id) {
        this.id.set(id);
    }
    
    // Lieu ID
    public Long getLieuId() {
        return lieuId.get();
    }
    
    public LongProperty lieuIdProperty() {
        return lieuId;
    }
    
    public void setLieuId(Long lieuId) {
        this.lieuId.set(lieuId);
    }
    
    // Link
    public String getLink() {
        return link.get();
    }
    
    public StringProperty linkProperty() {
        return link;
    }
    
    public void setLink(String link) {
        this.link.set(link);
    }
    
    // Type
    public String getType() {
        return type.get();
    }
    
    public StringProperty typeProperty() {
        return type;
    }
    
    public void setType(String type) {
        this.type.set(type);
    }
} 