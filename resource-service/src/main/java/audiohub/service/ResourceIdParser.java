package audiohub.service;

import audiohub.exception.InvalidCsvException;
import audiohub.exception.InvalidResourceIdException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResourceIdParser {

    private static final int MAX_CSV_LENGTH = 200;
    private static final Pattern POSITIVE_ID_PATTERN = Pattern.compile("[1-9]\\d*");

    public Set<Long> parsePositiveIds(String idsCsv) {
        if (idsCsv == null || idsCsv.isBlank()) {
            throw new InvalidCsvException("At least one resource ID must be provided");
        }

        if (idsCsv.length() > MAX_CSV_LENGTH) {
            throw new InvalidCsvException(
                    "CSV string is too long: received %d characters, maximum allowed is %s"
                            .formatted(idsCsv.length(), MAX_CSV_LENGTH)
            );
        }

        return Arrays.stream(idsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .map(this::parsePositiveId)
                .collect(Collectors.toSet());
    }

    public Long parsePositiveId(String id) {
        if (!POSITIVE_ID_PATTERN.matcher(id).matches()) {
            throw new InvalidResourceIdException(
                    "Invalid ID format: '%s'. Only positive integers are allowed".formatted(id)
            );
        }

        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new InvalidResourceIdException(
                    "Invalid ID format: '%s'. Only positive integers are allowed".formatted(id)
            );
        }
    }
}