package audiohub.util;


import audiohub.exception.InvalidResourceIdException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceIdUtil {

    public static Long parsePositiveId(String id) {
        if (id == null || !id.matches("^[1-9][0-9]*$")) {
            throw new InvalidResourceIdException(
                    "Resource ID must be a positive whole number (no letters, decimals, zero, or negative values)."
            );
        }
        return Long.parseLong(id);
    }

    public static List<Long> parsePositiveIds(String ids) {
        if (ids == null || ids.isBlank()) {
            throw new InvalidResourceIdException("At least one resource ID must be provided.");
        }
        if (ids.length() > 200)
            throw new InvalidResourceIdException("ID CSV must be less than 200 characters.");

        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(ResourceIdUtil::parsePositiveId)
                .collect(Collectors.toList());
    }
}