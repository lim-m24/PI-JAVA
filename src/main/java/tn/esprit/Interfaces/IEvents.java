package tn.esprit.Interfaces;

import java.util.List;

public interface IEvents<T> {
    void Add(T Events);
    List<T> readAll();
    void Update(T Events);
    void DeleteByID(int id);
}