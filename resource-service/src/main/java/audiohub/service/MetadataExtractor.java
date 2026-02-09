package audiohub.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MetadataExtractor {
    Map<String, Object> extract(MultipartFile file);
}
