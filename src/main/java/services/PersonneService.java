package services;
import models.Personne;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements IService<Personne>{

   private Connection connection;
   public PersonneService(){
       connection = MyDabase.getInstance().getConnection();
   }
    @Override
    public void ajouter(Personne personne) throws SQLException {
        String sql = "insert into personne (nom, prenom,age) " +
                "values('" + personne.getNom() + "','" + personne.getPrenom() + "'"
                +  "," + personne.getAge() + ")";

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    @Override
    public void modifier(Personne personne) throws SQLException {
        String sql = "update personne set nom = ?, prenom = ?, age = ? where id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, personne.getNom());
        preparedStatement.setString(2, personne.getPrenom());
        preparedStatement.setInt(3,personne.getAge());
        preparedStatement.setInt(4,personne.getId());
        preparedStatement.executeUpdate();

    }

    @Override
    public void sipprimer(int id) throws SQLException {
String req = "DELETE FROM `personne` WHERE id=?";
PreparedStatement preparedStatement = connection.prepareStatement(req);
preparedStatement.setInt(1,id);
preparedStatement.executeUpdate();
    }

    @Override
    public List<Personne> recuperer() throws SQLException {
       String sql = "select * from personne";
       Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
        List<Personne> list = new ArrayList<>();
        while (rs.next()){
            Personne p = new Personne();
            p.setId(rs.getInt("id"));
            p.setAge(rs.getInt("age"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            list.add(p);

        }
        return list;
   }
}
