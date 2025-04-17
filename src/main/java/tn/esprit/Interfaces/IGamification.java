package tn.esprit.Interfaces;

import java.util.List;

public interface IGamification<G> {
    void Add(G g);
    List<G> readAll();
    void Update(G g);
    void DeleteByID(int id);
}
