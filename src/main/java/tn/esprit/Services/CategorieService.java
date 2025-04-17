package tn.esprit.Services;

import tn.esprit.Interfaces.ICategorie;
import tn.esprit.Models.Categories;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements ICategorie<Categories> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;


    public CategorieService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(Categories categorie) {
        String qry = "insert into categories(nom,description,cover,date_creation) values(?,?,?,?)";
        try {
            pst = cnx.prepareStatement(qry);
            pst.setString(1, categorie.getNom());
            pst.setString(2, categorie.getDescription());
            pst.setString(3, categorie.getCover());
            pst.setTimestamp(4, java.sql.Timestamp.valueOf(categorie.getDate_creation()));
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
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

    @Override
    public void Update(Categories categories) {
        String requete = "UPDATE categories SET nom=?, description=?, cover=?, date_creation=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);

            pst.setString(1, categories.getNom());
            pst.setString(2, categories.getDescription());
            pst.setString(3, categories.getCover());

            LocalDateTime date = categories.getDate_creation();
            Timestamp timestamp = Timestamp.valueOf(date);
            pst.setTimestamp(4, timestamp);

            pst.setInt(5, categories.getId());

            pst.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void DeleteByID(int id) {
        String requete = "DELETE FROM categories WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Category deleted with ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }
}
