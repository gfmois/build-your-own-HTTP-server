package infrastructure.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Configuration;

public class Server extends ServerSocket {
    private final static Logger logger = LogManager.getLogger(Server.class);

    public Server(Configuration configuration) throws IOException {
        super(configuration.getPort());
    }
}
