package tn.esprit.Interfaces;

import java.util.*;

public interface ICategorie<T> {
    void Add (T t);
    ArrayList<T> getAll();
    List<T> readAll();
    /*List<T> TriparNom();
    List<T> TriparEmail();*/
    List<T> Rechreche(String recherche);
    void Update(T t);
    void Delete(T t);
    void DeleteByID(int id);
}

