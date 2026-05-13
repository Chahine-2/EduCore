package interfaces;

import java.util.List;




public interface IService<T> {
    void add(T p);
    List<T> getAll();
    T getById(int id);
    void delete(int id);
    void update(T p);
}




