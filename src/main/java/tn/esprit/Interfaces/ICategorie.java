package tn.esprit.Interfaces;

import java.util.*;

public interface ICategorie<C> {
    void Add (C c);
    List<C> readAll();
    void Update(C c);
    void DeleteByID(int id);
}

