package audiohub.mapper;

import audiohub.dto.SongDto;
import audiohub.entity.SongEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SongMapper {

    SongEntity toEntity(SongDto dto);

    SongDto toDto(SongEntity entity);
}