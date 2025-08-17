package infrastructure.config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import domain.model.Configuration;

public class YamlConfigurationLoader {
    public Configuration load(String path) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(path);

        Map<String, Object> obj = yaml.load(inputStream);

        return new Configuration.Builder()
                .port((Integer) ((Map<String, Object>) obj.get("server")).get("port"))
                .build();
    }
}
