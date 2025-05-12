package tn.esprit.Services;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DataUpdateService {
    private static DataUpdateService instance;
    private final List<Consumer<String>> listeners = new ArrayList<>();
    
    private DataUpdateService() {}
    
    public static DataUpdateService getInstance() {
        if (instance == null) {
            instance = new DataUpdateService();
        }
        return instance;
    }
    
    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }
    
    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }
    
    public void notifyUpdate(String type) {
        Platform.runLater(() -> {
            for (Consumer<String> listener : listeners) {
                listener.accept(type);
            }
        });
    }
} 