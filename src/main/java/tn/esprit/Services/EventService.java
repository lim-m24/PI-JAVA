package tn.esprit.Services;

import tn.esprit.Interfaces.IEvents;
import tn.esprit.Models.Events;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IEvents<Events> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public EventService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(Events event) {
        String qry = "INSERT INTO events (id_community_id, nom, description, started_at, finish_at, lieu, type, cover, link, acces) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, event.getId_community_id());
            pst.setString(2, event.getNom());
            pst.setString(3, event.getDescription());
            pst.setTimestamp(4, java.sql.Timestamp.valueOf(event.getStarted_at()));
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(event.getFinish_at()));
            pst.setString(6, event.getLieu());
            pst.setString(7, event.getType());
            pst.setString(8, event.getCover());
            pst.setString(9, event.getLink());
            pst.setString(10, event.getAcces());
            pst.executeUpdate();

            // Retrieve the generated ID
            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                event.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Events> readAll() {
        List<Events> eventList = new ArrayList<>();
        String requete = "SELECT * FROM events";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                eventList.add(new Events(
                        rs.getInt("id"),
                        rs.getInt("id_community_id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getTimestamp("started_at").toLocalDateTime(),
                        rs.getTimestamp("finish_at").toLocalDateTime(),
                        rs.getString("lieu"),
                        rs.getString("type"),
                        rs.getString("cover"),
                        rs.getString("link"),
                        rs.getString("acces")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return eventList;
    }

    @Override
    public void Update(Events event) {
        String requete = "UPDATE events SET id_community_id=?, nom=?, description=?, started_at=?, finish_at=?, lieu=?, type=?, cover=?, link=?, acces=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, event.getId_community_id());
            pst.setString(2, event.getNom());
            pst.setString(3, event.getDescription());
            pst.setTimestamp(4, java.sql.Timestamp.valueOf(event.getStarted_at()));
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(event.getFinish_at()));
            pst.setString(6, event.getLieu());
            pst.setString(7, event.getType());
            pst.setString(8, event.getCover());
            pst.setString(9, event.getLink());
            pst.setString(10, event.getAcces());
            pst.setInt(11, event.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void DeleteByID(int id) {
        String requete = "DELETE FROM events WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Event deleted with ID: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
        }
    }
}