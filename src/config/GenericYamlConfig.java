package config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Log4j2
public class GenericYamlConfig {
    public static <T> T fromFile(final String filename, final Class<T> clazz) {
        final long startTime = System.currentTimeMillis();
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final T contentToReturn = mapper.readValue(new File(filename), clazz);
            final long endTime = System.currentTimeMillis();
            log.info("YamlConfig fromFile {} took {} ms.", filename, endTime - startTime);
            return contentToReturn;
        } catch (final FileNotFoundException e) {
            String message = "Could not read config file " + filename + ": " + e.getMessage();
            log.error(message);
            throw new RuntimeException(message);
        } catch (final IOException e) {
            String message = "Could not successfully parse config file " + filename + ": " + e.getMessage();
            log.error(message);
            throw new RuntimeException(message);
        }
    }
}
