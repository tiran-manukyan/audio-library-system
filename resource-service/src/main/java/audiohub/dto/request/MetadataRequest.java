package audiohub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataRequest {
    private Long resourceId;
    private String name;
    private String artist;
    private String album;
    private Integer year;
    private String duration; // mm:ss
}