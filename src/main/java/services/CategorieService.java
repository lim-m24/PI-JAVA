package services;

import models.Categories;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public CategorieService(){
        cnx= MyDabase.getInstance().getConnection();
    }
    public List<Categories> readAll(){
        List<Categories> CatList = new ArrayList<>();
        String requete = "Select * from categories";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()){
                CatList.add(new Categories(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("cover"),
                        rs.getTimestamp("date_creation").toLocalDateTime()));
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return CatList;
    }

    public Categories readById(int id) {
        String requete = "SELECT * FROM categories WHERE id = ?";
        Categories cat = null;

        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();

            if (rs.next()) {
                cat = new Categories(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("cover"),
                        rs.getTimestamp("date_creation").toLocalDateTime()
                );
            } else {
                System.out.println("❌ No category found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return cat;
    }


    public void Create(Categories cat) {
        String requete = "insert into categories(nom,description,cover,date_creation) values(?,?,?,?)";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, cat.getNom());
            pst.setString(2, cat.getDescription());
            pst.setString(3, cat.getCover());
            pst.setTimestamp(4, Timestamp.valueOf(cat.getDate_creation()));
            pst.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Update(Categories cat) {
        String requete = " update categories set nom=?,description=?,cover=? where id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, cat.getNom());
            pst.setString(2, cat.getDescription());
            pst.setString(3, cat.getCover());
            pst.setInt(4, cat.getId());
            pst.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Categories cat) {
        String requete = " delete from categories where id=" + cat.getId();
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
