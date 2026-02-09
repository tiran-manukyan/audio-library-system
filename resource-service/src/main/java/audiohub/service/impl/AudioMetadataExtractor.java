package audiohub.service.impl;

import audiohub.service.MetadataExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AudioMetadataExtractor implements MetadataExtractor {

    @Override
    public Map<String, Object> extract(MultipartFile file) {
        Map<String, Object> meta = new HashMap<>();

        try (InputStream inputstream = file.getInputStream()) {
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            Mp3Parser mp3Parser = new Mp3Parser();
            mp3Parser.parse(inputstream, handler, metadata, parseContext);

            meta.put("title", metadata.get("dc:title"));
            meta.put("artist", metadata.get("xmpDM:artist"));
            meta.put("album", metadata.get("xmpDM:album"));
            meta.put("year", parseYear(metadata.get("xmpDM:releaseDate")));
            meta.put("duration", convertDuration(metadata.get("xmpDM:duration")));
        } catch (Exception e) {
            log.error("MP3 extraction error for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("MP3 metadata extraction failed", e);
        }
        return meta;
    }

    private static Integer parseYear(String year) {
        if (year == null) return null;
        try {
            return Integer.parseInt(year.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertDuration(String duration) {
        if (duration == null) return null;
        try {
            double seconds = Double.parseDouble(duration);
            int mins = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            return String.format("%02d:%02d", mins, secs);
        } catch (Exception e) {
            log.warn("Invalid duration: {}", duration, e);
            return null;
        }
    }
}