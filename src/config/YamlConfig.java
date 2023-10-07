package config;

import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class YamlConfig extends GenericYamlConfig {
    public static final YamlConfig config = fromFile("config.yaml", YamlConfig.class);
    public List<WorldConfig> worlds;
    public ServerConfig server;
}
