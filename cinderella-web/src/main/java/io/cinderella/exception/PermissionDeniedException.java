package io.cinderella.exception;

/**
 * @author Shane Witbeck
 * @since 10/5/12
 */
public class PermissionDeniedException extends RuntimeException {
    private static final long serialVersionUID = -5816172859594116468L;

    public PermissionDeniedException() {
    }

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(Throwable e) {
        super(e);
    }

    public PermissionDeniedException(String message, Throwable e) {
        super(message, e);
    }
}
