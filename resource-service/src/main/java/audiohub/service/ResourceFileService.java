package audiohub.service;

import audiohub.dto.response.DeleteResourcesResponse;
import audiohub.dto.response.ResourceFileResponse;
import audiohub.entity.ResourceFileEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceFileService {

    ResourceFileResponse store(MultipartFile file);

    ResourceFileEntity findById(String id);

    DeleteResourcesResponse deleteResources(String idCsv);
}