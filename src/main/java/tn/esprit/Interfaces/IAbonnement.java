package tn.esprit.Interfaces;

import java.util.List;

public interface IAbonnement<A> {
    void Add (A a);
    List<A> readAll();
    void Update(A a);
    void DeleteByID(int id);
}
