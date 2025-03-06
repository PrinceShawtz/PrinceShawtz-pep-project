package DAO;

import java.util.List;
import java.util.Optional;

/**
 * This interface defines the standard operations to be performed on a model
 * object in the system.
 *
 * @param <T> The type of the model object to be managed.
 */

public interface BaseDao<T> {
    /**
     * Retrieves an object by its ID.
     *
     * @param id The ID of the object to retrieve.
     * @return An Optional containing the object of type T if found, or an empty
     *         Optional if the object is not found.
     */
    Optional<T> getById(int id);

    /**
     * Retrieves all objects of type T from the system.
     *
     * @return A List of all objects of type T.
     */
    List<T> getAll();

    /**
     * Inserts a new object into the system.
     *
     * @param t The object of type T to insert into the database.
     * @return The object of type T that was inserted into the database.
     */
    T insert(T t);

    /**
     * Updates an existing object in the system.
     *
     * @param t The object of type T to update in the database.
     * @return true if the update was successful; false if the object was not found
     *         in the database.
     */
    boolean update(T t);

    /**
     * Deletes an object from the system.
     *
     * @param t The object of type T to delete from the database.
     * @return true if the deletion was successful; false if the object was not
     *         found in the database.
     */
    boolean delete(T t);
}
