package audiohub.dto.response;

import java.util.Set;

public record DeleteResourcesResponse(Set<Long> ids) {
}