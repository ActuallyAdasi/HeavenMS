package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Log4j2
public class YamlConfig {
    public static final YamlConfig config = fromFile("config.yaml");
    
    public List<WorldConfig> worlds;
    public ServerConfig server;

    public static YamlConfig fromFile(final String filename) {
        final long startTime = System.currentTimeMillis();
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            final YamlConfig configToReturn = mapper.readValue(new File(filename), YamlConfig.class);
            final long endTime = System.currentTimeMillis();
            log.info("YamlConfig fromFile returning new config after {} ms.", endTime - startTime);
            return configToReturn;
        } catch (final FileNotFoundException e) {
            String message = "Could not read config file " + filename + ": " + e.getMessage();
            throw new RuntimeException(message);
        } catch (final IOException e) {
            String message = "Could not successfully parse config file " + filename + ": " + e.getMessage();
            throw new RuntimeException(message);
        }
    }
}
