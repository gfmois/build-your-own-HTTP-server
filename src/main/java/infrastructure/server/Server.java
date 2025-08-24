package infrastructure.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Configuration;
import domain.model.Controller;
import domain.model.Request;
import enums.RequestMethod;
import infrastructure.api.Router;
import infrastructure.container.Container;

public class Server extends ServerSocket {
    private final static Logger logger = LogManager.getLogger(Server.class);

    public Server(Configuration configuration) throws IOException {
        super(configuration.getPort());
    }

    public void start(boolean reuseAddress) {
        try {
            this.setReuseAddress(reuseAddress);
            handleConnections();
        } catch (IOException e) {
            logger.error("Failed to start server: {}", e.getMessage());
        }
    }

    public void registerControllers(List<Controller> controllers) {
        Router.registerControllers(controllers);
    }

    public void handleConnections() throws IOException {
        try (Socket clientSocket = this.accept()) {
            logger.debug("Accepted new connection from {}", clientSocket.getInetAddress());

            // Handle client connection (read/write data)
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

            String line;
            int i = 0;
            Request request = new Request();

            while ((line = input.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split(" ");
                logger.info("line: {}", line);
                if (i == 0) { // i = 0 -> request line
                    try {
                        request.setMethod(RequestMethod.valueOf(parts[0]));
                        request.setPath(parts[1]);
                        request.setVersion(parts[2]);
                    } catch (Exception e) {
                        logger.error("Exception: {}", e.getMessage());
                    }
                } else {
                    String[] headers = line.split(":");
                    request.getHeaders().put(headers[0], headers[1]);
                }

                i++;
            }

            // Process the request
            Router router = Container.register(Router.class.getSimpleName(), Router.class);
            router.perform(request, output);
        }
    }
}
