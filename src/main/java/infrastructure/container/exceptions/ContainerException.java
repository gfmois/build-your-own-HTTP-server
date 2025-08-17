package infrastructure.container.exceptions;

/**
 * Custom exception for container-related errors.
 */
public class ContainerException extends RuntimeException {
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}