package audiohub.exception;

public class SongAlreadyExistsException extends RuntimeException {
    public SongAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}