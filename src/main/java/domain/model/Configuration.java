package domain.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);
    private Integer port;
    private String directory = "/tmp";

    public Configuration(Integer port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    public Configuration(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public String getDirectory() {
        return directory;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public static class Builder {
        private Integer port;
        private String directory = "/tmp";

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder directory(String directory) {
            this.directory = directory;
            return this;
        }

        public Configuration build() {
            return new Configuration(this.port);
        }
    }

    public static void checkForAppArgs(String[] args, Configuration config) {
        for (String arg : args) {
            String lowerArg = arg.toLowerCase();
            String[] parts = lowerArg.split("=");
            if (parts.length != 2) {
                logger.warn("Ignoring invalid argument: {}", arg);
                continue;
            }

            String command = parts[0];
            String value = parts[1];

            ServerConfigurationCommands matchedCommand = ServerConfigurationCommands.valueOfCommand(command);
            logger.debug("Matched command: {}", matchedCommand);

            switch (matchedCommand) {
                case PORT:
                    try {
                        int portValue = Integer.parseInt(value);
                        config.setPort(portValue);
                        logger.info("Set port to {}", portValue);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid port value: {}", value);
                    }
                    break;

                case DIRECTORY:
                    config.setDirectory(value);
                    logger.info("Set directory to {}", value);
                    break;
                default:
                    break;
            }

        }
    }

    private enum ServerConfigurationCommands {
        PORT("--port"),
        DIRECTORY("--directory");

        private final String command;

        ServerConfigurationCommands(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public static ServerConfigurationCommands valueOfCommand(String command) {
            for (ServerConfigurationCommands cmd : values()) {
                if (cmd.getCommand().equals(command)) {
                    return cmd;
                }
            }
            throw new IllegalArgumentException("No enum constant with command: " + command);
        }
    }
}
