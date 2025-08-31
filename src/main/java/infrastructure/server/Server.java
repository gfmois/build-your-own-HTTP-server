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
    private final String filesDirectory;

    public Server(Configuration configuration) throws IOException {
        super(configuration.getPort());
        this.filesDirectory = configuration.getDirectory();
        logger.info("Server started on port {} serving files from {}",
                configuration.getPort(),
                this.filesDirectory);
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
        while (true) {
            Socket clientSocket = this.accept();
            logger.debug("Accepted new connection from {}", clientSocket.getInetAddress());

            Thread requestThread = new Thread(() -> {
                try {
                    manageRequestProcessing(clientSocket);
                } catch (IOException e) {
                    logger.error("IOException while processing request: {}", e.getMessage());
                }
            }, "Request-Handler-Thread");
            requestThread.start();
        }
    }

    private void manageRequestProcessing(Socket clientSocket) throws IOException {
        // Handle client connection (read/write data)
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

        String line;
        int i = 0;
        Request request = new Request();

        while ((line = input.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(" ");
            logger.debug("line: {}", line);

            if (i == 0) { // i = 0 -> request line
                try {
                    request.setMethod(RequestMethod.valueOf(parts[0]));
                    request.setPath(parts[1]);
                    request.setVersion(parts[2]);
                } catch (Exception e) {
                    logger.error("Exception: {}", e.getMessage());
                }
            } else if (line.contains(":")) { // i > 0 -> headers
                String[] headers = line.split(":");
                request.getHeaders().put(headers[0], headers[1]);
            }

            i++;
        }

        // Read body if present
        StringBuilder bodyBuilder = new StringBuilder();
        while (input.ready()) {
            bodyBuilder.append((char) input.read());
        }

        if (bodyBuilder.length() > 0) {
            request.setBody(bodyBuilder.toString());
        }

        logger.info("Request: {}", request);

        // Process the request
        Router router = Container.getOrCreate(Router.class);
        router.perform(request, output);
    }
}
