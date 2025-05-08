package tn.esprit.Models;

import java.util.prefs.Preferences;

public class UserPreferences {
    private static final String NIGHT_MODE = "nightMode";
    private static final String LANGUAGE = "language";
    private static final String FONT_SIZE = "fontSize";

    private final Preferences prefs;
    private final int userId;

    public UserPreferences(int userId) {
        this.userId = userId;
        this.prefs = Preferences.userRoot().node("com/syncylinky/user/" + userId);
    }

    public boolean isNightMode() {
        return prefs.getBoolean(NIGHT_MODE, false);
    }

    public void setNightMode(boolean enabled) {
        prefs.putBoolean(NIGHT_MODE, enabled);
    }

    public String getLanguage() {
        return prefs.get(LANGUAGE, "en");
    }

    public void setLanguage(String language) {
        prefs.put(LANGUAGE, language);
    }

    public int getFontSize() {
        return prefs.getInt(FONT_SIZE, 12);
    }

    public void setFontSize(int size) {
        prefs.putInt(FONT_SIZE, size);
    }

    public void clearPreferences() {
        try {
            prefs.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}