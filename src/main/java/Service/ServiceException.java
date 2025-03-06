package Service;

/**
 * This class is responsible for handling exceptions that occur in the Service layer.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructor that takes in a custom message for the exception
     *
     * @param message The error message associated with this exception
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructor that takes in the original exception that causes this exception
     *
     * @param cause The original exception that caused this ServiceException
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor that takes in a custom message and the original exception that causes this exception
     *
     * @param message The error message associated with this exception
     * @param cause   The original exception that caused this ServiceException
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}