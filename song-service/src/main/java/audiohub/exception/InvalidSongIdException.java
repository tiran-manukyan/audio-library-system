package audiohub.exception;

public class InvalidSongIdException extends RuntimeException {
    public InvalidSongIdException(String message) {
        super(message);
    }
}