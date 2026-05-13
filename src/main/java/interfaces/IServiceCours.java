package interfaces;

import java.util.List;



public interface IServiceCours<T> {
    void add(T p);
    List<T> getAll();
    void delete(T c);
    void update(T p);
}


