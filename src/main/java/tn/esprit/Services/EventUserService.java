package tn.esprit.Services;

import tn.esprit.Interfaces.IEvents;
import tn.esprit.Models.EventUser;
import tn.esprit.utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventUserService implements IEventUser<EventUser> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public EventUserService() {
        cnx = DataBase.getInstance().getConnection();
    }

    @Override
    public void Add(EventUser eventUser) {
        String qry = "INSERT INTO participation_event (user_id, event_id, type) VALUES (?, ?, ?)";
        try {
            pst = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, eventUser.getUser_id());
            pst.setInt(2, eventUser.getEvent_id() != null ? eventUser.getEvent_id() : 0); // Handle null event_id
            pst.setString(3, eventUser.getType());
            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                eventUser.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<EventUser> readAll() {
        List<EventUser> eventUserList = new ArrayList<>();
        String requete = "SELECT * FROM participation_event";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                eventUserList.add(new EventUser(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("event_id"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return eventUserList;
    }

    public Map<Integer, int[]> getEventStats() {
        Map<Integer, int[]> stats = new HashMap<>();
        String requete = "SELECT event_id, type, COUNT(*) as count FROM participation_event GROUP BY event_id, type";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                String type = rs.getString("type");
                int count = rs.getInt("count");
                stats.computeIfAbsent(eventId, k -> new int[2])[type.equals("Participate") ? 0 : 1] = count;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stats;
    }
}

interface IEventUser<T> {
    void Add(T eventUser);
    List<T> readAll();
}