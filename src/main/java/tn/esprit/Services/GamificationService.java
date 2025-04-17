package tn.esprit.Services;

import tn.esprit.Interfaces.IGamification;
import tn.esprit.Models.Gamification;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GamificationService implements IGamification<Gamification> {
    private Connection cnx;
    private PreparedStatement pst;
    private Statement ste;
    private ResultSet rs;

    public GamificationService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(Gamification g) {
        String qry = "INSERT INTO gamifications(type_abonnement, nom, description, type, condition_gamification) VALUES (?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(qry);
            pst.setInt(1, g.getTypeAbonnement());
            pst.setString(2, g.getNom());
            pst.setString(3, g.getDescription());
            pst.setString(4, g.getType());
            pst.setInt(5, g.getConditionGamification());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Add Error: " + e.getMessage());
        }
    }

    @Override
    public List<Gamification> readAll() {
        List<Gamification> list = new ArrayList<>();
        String req = "SELECT * FROM gamifications";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(req);
            while (rs.next()) {
                list.add(new Gamification(
                        rs.getInt("id"),
                        rs.getInt("type_abonnement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getInt("condition_gamification")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void Update(Gamification g) {
        String req = "UPDATE gamifications SET type_abonnement=?, nom=?, description=?, type=?, condition_gamification=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(req);
            pst.setInt(1, g.getTypeAbonnement());
            pst.setString(2, g.getNom());
            pst.setString(3, g.getDescription());
            pst.setString(4, g.getType());
            pst.setInt(5, g.getConditionGamification());
            pst.setInt(6, g.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update Error: " + e.getMessage());
        }
    }

    @Override
    public void DeleteByID(int id) {
        String req = "DELETE FROM gamifications WHERE id=?";
        try {
            pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Delete Error: " + e.getMessage());
        }
    }
}
