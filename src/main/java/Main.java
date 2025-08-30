import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Configuration;
import domain.model.Controller;
import infrastructure.api.controller.BaseController;
import infrastructure.config.YamlConfigurationLoader;
import infrastructure.container.Container;
import infrastructure.server.Server;

public class Main {
  private final static Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Reading configuration file...");
    YamlConfigurationLoader yamlReader = new YamlConfigurationLoader();
    Configuration serverConfiguration = yamlReader.load("configuration.yaml");
    logger.info("Configuration loaded.");

    Configuration.checkForAppArgs(args, serverConfiguration);

    // Controllers
    List<Controller> controllers = List.of(
        Container.getOrCreate(BaseController.class));

    logger.info("Starting server");
    try (Server serverSocket = Container.register(Server.class.getSimpleName(), Server.class, serverConfiguration)) {
      serverSocket.registerControllers(controllers);
      serverSocket.start(true);
    } catch (IOException e) {
      logger.error("IOException: {}", e.getMessage());
    }
  }
}
