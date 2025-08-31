package infrastructure.config;

import java.io.InputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import domain.model.Configuration;

public class YamlConfigurationLoader {
    private final static Logger logger = LogManager.getLogger(YamlConfigurationLoader.class);

    public Configuration load(String path) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(path);

        Map<String, Object> obj = yaml.load(inputStream);

        if (!(obj.get("server") instanceof Map)) {
            logger.error("Invalid server configuration format in YAML file. Using default configuration.");
            return new Configuration();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> serverConfig = (Map<String, Object>) obj.get("server");
        Configuration.Builder builder = new Configuration.Builder();

        if (serverConfig == null || serverConfig.isEmpty()) {
            logger.warn("Server configuration is missing or empty in the YAML file. Using default values.");
            return new Configuration();
        }

        // Port
        if (serverConfig.containsKey("port")) {
            try {
                Integer port = (Integer) serverConfig.get("port");
                builder.port(port);
            } catch (ClassCastException e) {
                logger.error("Invalid type for port in configuration file. Using default port 8080.");
            }
        } else {
            logger.warn("Port not specified in configuration file. Using default port 8080.");
        }

        // Directory
        if (serverConfig.containsKey("directory")) {
            try {
                String directory = (String) serverConfig.get("directory");
                builder.directory(directory);
            } catch (ClassCastException e) {
                logger.error("Invalid type for directory in configuration file. Using default directory /tmp.");
            }
        } else {
            logger.warn("Directory not specified in configuration file. Using default directory /tmp.");
        }

        return builder.build();
    }
}
