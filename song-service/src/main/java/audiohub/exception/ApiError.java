package audiohub.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    String errorMessage,
    Map<String, String> details,
    String errorCode
) {
    public ApiError(String errorMessage, String errorCode) {
        this(errorMessage, null, errorCode);
    }
}