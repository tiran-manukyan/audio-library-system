package audiohub.dto;

import java.util.Set;

public record DeleteSongsResponse(Set<Long> ids) {
}