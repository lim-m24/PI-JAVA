package tn.esprit.Services;

import tn.esprit.Interfaces.IAbonnement;
import tn.esprit.Models.Abonnements;
import tn.esprit.utils.DataBase;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;

public class AbonnementService implements IAbonnement<Abonnements> {
    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    private static final String EXPIRY_FILE_PATH = "src/abonnement_expiries.properties";
    private static final Map<Integer, LocalDate> abonnementExpiries = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final long DEFAULT_EXPIRY_DAYS = 7;

    private void saveExpiries() {
        java.io.FileOutputStream output = null;
        java.util.Properties properties = new java.util.Properties();
        try {
            output = new java.io.FileOutputStream(EXPIRY_FILE_PATH);
            for (Map.Entry<Integer, LocalDate> entry : abonnementExpiries.entrySet()) {
                properties.setProperty(String.valueOf(entry.getKey()), entry.getValue().toString());
            }
            properties.store(output, "Abonnement Expiry Dates");
        } catch (java.io.IOException e) {
            System.err.println("Error saving expiry dates: " + e.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (java.io.IOException e) {
                    System.err.println("Error closing output stream: " + e.getMessage());
                }
            }
        }
    }
    public AbonnementService() {
        cnx = DataBase.getInstance().getConnection();
        startExpiryCheckTask();
        loadExistingExpiries();
    }

    private void loadExistingExpiries() {
        java.util.Properties properties = new java.util.Properties();
        try (java.io.FileInputStream input = new java.io.FileInputStream(EXPIRY_FILE_PATH)) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                int abonnementId = Integer.parseInt(key);
                LocalDate expiryDate = LocalDate.parse(properties.getProperty(key));
                abonnementExpiries.put(abonnementId, expiryDate);
                System.out.println("Loaded expiry for Abonnement ID " + abonnementId + ": " + expiryDate);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error loading expiry dates (file might not exist yet): " + e.getMessage());
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            System.err.println("Error parsing expiry data from file: " + e.getMessage());
        }
    }

    public void setExpiryDate(int abonnementId, LocalDate expiryDate) {
        abonnementExpiries.put(abonnementId, expiryDate);
        System.out.println("Expiry date set for Abonnement ID " + abonnementId + " to " + expiryDate + ". Map contents: " + abonnementExpiries);
        saveExpiries();
    }

    public LocalDate getExpiryDate(int abonnementId) {
        return abonnementExpiries.get(abonnementId);
    }

    private void startExpiryCheckTask() {
        scheduler.scheduleAtFixedRate(this::checkAndExpireAbonnements, 1, 1, TimeUnit.DAYS);
        System.out.println("Abonnement expiry check task started.");
    }

    private void checkAndExpireAbonnements() {
        LocalDate now = LocalDate.now(java.time.ZoneId.of("Africa/Tunis")); // Important: Use Tunis time zone
        List<Integer> expiredAbonnementIds = new ArrayList<>();
        for (Map.Entry<Integer, LocalDate> entry : abonnementExpiries.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                int abonnementId = entry.getKey();
                deleteAbonnementFromDatabase(abonnementId);
                expiredAbonnementIds.add(abonnementId);
            }
        }
        expiredAbonnementIds.forEach(abonnementExpiries::remove);
    }

    private void deleteAbonnementFromDatabase(int id) {
        String requete = "DELETE FROM abonnements WHERE id = ?";
        try (PreparedStatement deletePst = cnx.prepareStatement(requete)) {
            deletePst.setInt(1, id);
            deletePst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting abonnement from database: " + e.getMessage());
        }
    }

    @Override
    public void Add(Abonnements abonnements) {
        String qry = "insert into abonnements(nom,prix,avantages,type) values(?,?,?,?)";
        try (PreparedStatement insertPst = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            insertPst.setString(1, abonnements.getNom());
            insertPst.setDouble(2, abonnements.getPrix());
            insertPst.setString(3, abonnements.getAvantages());
            insertPst.setString(4, abonnements.getType());
            insertPst.executeUpdate();

            // Get the generated ID
            try (ResultSet generatedKeys = insertPst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newAbonnementId = generatedKeys.getInt(1);
                    // Set a default expiry date for the new abonnement
                    setExpiryDate(newAbonnementId, LocalDate.now(java.time.ZoneId.of("Africa/Tunis")).plusDays(DEFAULT_EXPIRY_DAYS));
                } else {
                    throw new SQLException("Creating abonnement failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Abonnements> readAll(){
        List<Abonnements> AbList = new ArrayList<>();
        String requete = "Select * from abonnements";
        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(requete)) {
            while (rs.next()){
                AbList.add(new Abonnements(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getString("avantages"),
                        rs.getString("type")));
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return AbList;
    }

    @Override
    public void Update(Abonnements abonnements) {
        String requete = "UPDATE abonnements SET nom=?, prix=?, avantages=?, type=? WHERE id=?";
        try (PreparedStatement updatePst = cnx.prepareStatement(requete)) {
            updatePst.setString(1, abonnements.getNom());
            updatePst.setDouble(2, abonnements.getPrix());
            updatePst.setString(3, abonnements.getAvantages());
            updatePst.setString(4, abonnements.getType());
            updatePst.setInt(5, abonnements.getId());
            updatePst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void DeleteByID(int id) {
        String requete = "DELETE FROM abonnements WHERE id = ?";
        try (PreparedStatement deletePst = cnx.prepareStatement(requete)) {
            deletePst.setInt(1, id);
            deletePst.executeUpdate();
            System.out.println("Abonnement deleted with ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting abonnement: " + e.getMessage());
        }
        abonnementExpiries.remove(id); // Remove from expiry tracking as well
    }
}