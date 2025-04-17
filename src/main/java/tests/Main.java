package tests;

import models.Personne;
import services.PersonneService;
import utils.MyDabase;

import java.sql.SQLException;

public class Main {

    public static void main(String [] args){


        //MyDabase d=MyDabase.getInstance();

        PersonneService ps=new PersonneService();

        try {
            ps.ajouter(new Personne(24,"Belkneni","Maroua"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        try {
            ps.modifier(new Personne(1,25,"Belkneni","sarra"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        try {
           System.out.println(ps.recuperer());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
