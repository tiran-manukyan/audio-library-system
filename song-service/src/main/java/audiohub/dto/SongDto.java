package audiohub.dto;

public record SongDto(
    Long resourceId,
    String name,
    String artist,
    String album,
    Integer year,
    String duration
) {}