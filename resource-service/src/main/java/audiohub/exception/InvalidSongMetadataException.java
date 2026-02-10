package audiohub.exception;


import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidSongMetadataException extends RuntimeException {

    private final Set<? extends ConstraintViolation<?>> violations;

    public InvalidSongMetadataException(String message, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations;
    }

}