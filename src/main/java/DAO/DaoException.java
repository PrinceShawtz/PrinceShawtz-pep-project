package DAO;

/**
 * This class is responsible for handling exceptions that occur in the DAO layer.
 */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DaoException with the specified error message.
     *
     * @param message The detailed message for the exception. This is saved for
     *                later retrieval by the getMessage() method.
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Constructs a new DaoException with the specified error message and cause.
     *
     * @param message The detailed message for the exception. This is saved for
     *                later retrieval by the getMessage() method.
     * @param cause   The cause of the exception. This is saved for later retrieval
     *                by the getCause() method.
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}