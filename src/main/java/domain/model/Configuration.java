package domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);
    private Integer port;
    private String directory = "/tmp";

    private static final String COMMAND = "command";
    private static final String VALUE = "value";

    public Configuration(Integer port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    public Configuration(Configuration other) {
        this.port = other.port;
        this.directory = other.directory;
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
        Map<String, List<String>> foundCommandsMap = new HashMap<>();

        foundCommandsMap.putIfAbsent(COMMAND, new ArrayList<>());
        foundCommandsMap.putIfAbsent(VALUE, new ArrayList<>());

        for (String arg : args) {
            String lowerArg = arg.toLowerCase();
            if (lowerArg.startsWith("--")) {
                foundCommandsMap.get(COMMAND).add(lowerArg);
                continue;
            }

            foundCommandsMap.get(VALUE).add(arg);
        }

        foundCommandsMap.entrySet().forEach(entry -> {
            if (foundCommandsMap.get(COMMAND).isEmpty() || foundCommandsMap.get(VALUE).isEmpty()) {
                logger.warn("No {} or {} found in arguments", COMMAND, VALUE);
                return;
            }

            String commandString = foundCommandsMap.get(COMMAND).get(0);
            String value = foundCommandsMap.get(VALUE).get(0);

            ServerConfigurationCommand command = ServerConfigurationCommand.valueOfCommand(commandString);

            switch (command) {
                case PORT:
                    try {
                        int port = Integer.parseInt(value);
                        config.setPort(port);
                        logger.info("Port set to {}", port);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid port number: {}", value);
                    }
                    break;
                case DIRECTORY:
                    config.setDirectory(value);
                    logger.info("Directory set to {}", value);
                    break;
                default:
                    break;
            }

            // Remove processed command and its value to avoid re-processing
            foundCommandsMap.get(COMMAND).remove(0);
            foundCommandsMap.get(VALUE).remove(0);
        });
    }

    private enum ServerConfigurationCommand {
        PORT("--port"),
        DIRECTORY("--directory");

        private final String command;

        ServerConfigurationCommand(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public static ServerConfigurationCommand valueOfCommand(String command) {
            for (ServerConfigurationCommand cmd : values()) {
                if (cmd.getCommand().equals(command)) {
                    return cmd;
                }
            }
            throw new IllegalArgumentException("No enum constant with command: " + command);
        }
    }

    @Override
    public String toString() {
        return "Configuration [port=" + port + ", directory=" + directory + "]";
    }
}
