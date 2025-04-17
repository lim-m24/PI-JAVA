package tn.esprit.Services;

import tn.esprit.Interfaces.IAbonnement;
import tn.esprit.Models.Abonnements;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService implements IAbonnement<Abonnements> {
    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;


    public AbonnementService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(Abonnements abonnements) {
        String qry = "insert into abonnements(nom,prix,avantages,type) values(?,?,?,?)";
        try {
            pst = cnx.prepareStatement(qry);
            pst.setString(1, abonnements.getNom());
            pst.setDouble(2, abonnements.getPrix());
            pst.setString(3, abonnements.getAvantages());
            pst.setString(4, abonnements.getType());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Abonnements> readAll(){
        List<Abonnements> AbList = new ArrayList<>();
        String requete = "Select * from abonnements";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
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
        try {
            pst = cnx.prepareStatement(requete);

            pst.setString(1, abonnements.getNom());
            pst.setDouble(2, abonnements.getPrix());
            pst.setString(3, abonnements.getAvantages());
            pst.setString(4, abonnements.getType());
            pst.setInt(5, abonnements.getId());
            pst.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void DeleteByID(int id) {
        String requete = "DELETE FROM abonnements WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Abonnement deleted with ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting abonnement: " + e.getMessage());
        }
    }
}
