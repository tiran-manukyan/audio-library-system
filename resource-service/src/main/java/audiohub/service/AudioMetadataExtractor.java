package audiohub.service;

import audiohub.dto.request.SongMetadataDto;
import audiohub.exception.InvalidMp3Exception;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class AudioMetadataExtractor {

    public SongMetadataDto extract(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            throw new InvalidMp3Exception("MP3 audio data is required");
        }

        try (InputStream inputStream = new ByteArrayInputStream(audioData)) {
            ContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            Mp3Parser mp3Parser = new Mp3Parser();
            mp3Parser.parse(inputStream, handler, metadata, parseContext);

            String name = metadata.get("dc:title");
            String artist = metadata.get("xmpDM:artist");
            String album = metadata.get("xmpDM:album");
            String duration = convertSecondsToMMSS(metadata.get("xmpDM:duration"));
            String year = parseYear(metadata.get("xmpDM:releaseDate"));

            log.debug("Extracted MP3 metadata - name: '{}', artist: '{}', album: '{}', duration: '{}', year: '{}'",
                    name, artist, album, duration, year);

            return SongMetadataDto.builder()
                    .name(name)
                    .artist(artist)
                    .album(album)
                    .duration(duration)
                    .year(year)
                    .build();
        } catch (Exception e) {
            throw new InvalidMp3Exception("Invalid MP3 file format or corrupted file", e);
        }
    }

    private String parseYear(String year) {
        if (year == null || year.length() < 4) {
            return null;
        }
        return year.substring(0, 4);
    }

    private String convertSecondsToMMSS(String durationInSeconds) {
        if (durationInSeconds == null) {
            return "00:00";
        }

        try {
            double seconds = Double.parseDouble(durationInSeconds);
            int totalSeconds = (int) seconds;
            int minutes = totalSeconds / 60;
            int secs = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, secs);
        } catch (NumberFormatException e) {
            log.warn("Invalid duration format: {}, defaulting to 00:00", durationInSeconds);
            return "00:00";
        }
    }
}