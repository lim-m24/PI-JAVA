package tn.esprit.Services;

import tn.esprit.Interfaces.ICommunity;
import tn.esprit.Models.Community;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommunityService implements ICommunity<Community> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public CommunityService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(Community community) {
        String qry = "INSERT INTO community(id_categorie_id, nom, description, cover, created_at, nbr_membre, statut) VALUES(?,?,?,?,?,?,?)";
        try {
            pst = cnx.prepareStatement(qry);
            pst.setInt(1, community.getId_categorie_id());
            pst.setString(2, community.getNom());
            pst.setString(3, community.getDescription());
            pst.setString(4, community.getCover());
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(community.getCreated_at()));
            pst.setInt(6, community.getNbr_membre());
            pst.setByte(7, community.getstatut());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Community> readAll() {
        List<Community> communityList = new ArrayList<>();
        String requete = "SELECT * FROM community";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                communityList.add(new Community(
                        rs.getInt("id"),
                        rs.getInt("id_categorie_id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("cover"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("nbr_membre"),
                        rs.getByte("statut")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return communityList;
    }

    @Override
    public void Update(Community community) {
        String requete = "UPDATE community SET id_categorie_id=?, nom=?, description=?, cover=?, created_at=?, nbr_membre=?, statut=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, community.getId_categorie_id());
            pst.setString(2, community.getNom());
            pst.setString(3, community.getDescription());
            pst.setString(4, community.getCover());
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(community.getCreated_at()));
            pst.setInt(6, community.getNbr_membre());
            pst.setByte(7, community.getstatut());
            pst.setInt(8, community.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void DeleteByID(int id) {
        String requete = "DELETE FROM community WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Community deleted with ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting community: " + e.getMessage());
        }
    }
}
