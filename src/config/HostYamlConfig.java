package config;

public class HostYamlConfig extends GenericYamlConfig {
    public static final HostConfig config = fromFile("hostconfig.yaml", HostConfig.class);
}
