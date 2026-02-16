package audiohub.exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String message = error.getDefaultMessage();

            if (details.containsKey(field)) {
                if (message != null && message.contains("is required")) {
                    details.put(field, message);
                }

            } else {
                details.put(field, message);
            }
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                details
        );
    }

    @ExceptionHandler(InvalidMp3Exception.class)
    public ResponseEntity<ApiError> handleInvalidMp3(InvalidMp3Exception ex) {
        log.warn("Invalid MP3 file: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidSongMetadataException.class)
    public ResponseEntity<ApiError> handleInvalidSongMetadata(InvalidSongMetadataException ex) {
        log.warn("Song metadata validation error: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getViolations()) {
            String field = v.getPropertyPath().toString();
            details.put(field, v.getMessage());
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid parameter type: {}", ex.getMessage());

        String paramName = ex.getName();
        Object rejectedValue = ex.getValue();

        String message = String.format(
                "Invalid value '%s' for parameter '%s'",
                rejectedValue,
                paramName
        );

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        MediaType contentType = ex.getContentType();
        String ct = (contentType == null) ? "unknown" : contentType.toString();

        log.warn("Unsupported media type: {}", ct);

        String message = "Invalid file format: %s. Only MP3 files are allowed".formatted(ct);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String msg = ex.getMessage();
        log.warn("Invalid request body: {}", msg);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body"
        );
    }

    @ExceptionHandler(InvalidResourceIdException.class)
    public ResponseEntity<ApiError> handleInvalidResourceId(InvalidResourceIdException ex) {
        log.warn("Invalid resource ID: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidCsvException.class)
    public ResponseEntity<ApiError> handleInvalidCsv(InvalidCsvException ex) {
        log.warn("Invalid CSV: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(SongServiceException.class)
    public ResponseEntity<ApiError> handleSongServiceException(SongServiceException ex) {
        log.error("Song Service error: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to communicate with Song Service"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiError(message, String.valueOf(status.value())));
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status,
                                                        String message,
                                                        Map<String, String> details) {
        return ResponseEntity
                .status(status)
                .body(new ApiError(message, details, String.valueOf(status.value())));
    }
}