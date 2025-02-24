package DAO;

/**
 * DaoException is a custom unchecked exception class that encapsulates and
 * handles exceptions that may occur within the DAO layer.
 * Extending RuntimeException allows DaoException to be an unchecked exception,
 * which does not have to be declared in method signatures
 * or explicitly caught in the code.
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
     * Note that the detail message associated with cause is not automatically
     * incorporated into this exception's detail message.
     *
     * @param message The detailed message for the exception. This is saved for
     *                later retrieval by the getMessage() method.
     * @param cause   The cause of the exception (which is saved for later retrieval
     *                by the getCause() method).
     *                A null value is permitted, and indicates that the cause is
     *                nonexistent or
     *                unknown.
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}