package audiohub.dto;

import java.util.List;

public record DeleteSongsResponse(List<Long> ids) {}