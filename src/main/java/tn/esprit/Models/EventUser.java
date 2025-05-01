package tn.esprit.Models;

public class EventUser {
    private int id;
    private int user_id;
    private Integer event_id; // Nullable as per schema
    private String type; // "Participate" or "Interested"

    public EventUser(int id, int user_id, Integer event_id, String type) {
        this.id = id;
        this.user_id = user_id;
        this.event_id = event_id;
        this.type = type;
    }

    public EventUser(int user_id, Integer event_id, String type) {
        this.user_id = user_id;
        this.event_id = event_id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public Integer getEvent_id() {
        return event_id;
    }

    public void setEvent_id(Integer event_id) {
        this.event_id = event_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "EventUser{id=" + id + ", user_id=" + user_id + ", event_id=" + event_id + ", type=" + type + "}";
    }
}