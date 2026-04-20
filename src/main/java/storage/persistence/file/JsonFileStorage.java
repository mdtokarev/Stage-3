package storage.persistence.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import storage.persistence.file.dto.DataSnapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonFileStorage {
    private final ObjectMapper objectMapper;

    public JsonFileStorage() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save(Path path, DataSnapshot snapshot) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writeValue(path.toFile(), snapshot);
    }

    public DataSnapshot load(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), DataSnapshot.class);
    }
}
