package infrastructure.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private final int timeout = 30000;
    private boolean keepAlive = true;

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
            clientSocket.setSoTimeout(timeout);
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
        OutputStream output = clientSocket.getOutputStream();

        int i = 0;
        Request request = new Request();

        while (this.keepAlive) {
            // Read line
            String line = input.readLine();
            if (line == null)
                break; // Socket closed by client

            String[] parts = line.split(" ");
            if (parts.length < 3)
                break;
            logger.debug("line: {}", line);

            try {
                request.setMethod(RequestMethod.valueOf(parts[0]));
                request.setPath(parts[1]);
                request.setVersion(parts[2]);
            } catch (Exception e) {
                logger.error("Exception: {}", e.getMessage());
            }

            while (!(line = input.readLine()).isEmpty()) {
                String[] headers = line.split(":", 2);
                if (headers.length == 2) {
                    String key = headers[0].trim();
                    String value = headers[1].trim();
                    request.getHeaders().put(key, value);

                    // Check for keep-alive
                    if (key.equalsIgnoreCase("Connection") && value.equalsIgnoreCase("close")) {
                        this.keepAlive = false;
                    }
                }
            }

            if (request.getHeaders().containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(request.getHeaders().get("Content-Length").trim());
                char[] bodyChars = new char[contentLength];
                input.read(bodyChars, 0, contentLength);
                request.setBody(new String(bodyChars));
            }

            logger.info("Request: {}", request);

            // Process the request
            Router router = Container.getOrCreate(Router.class);
            router.perform(request, output);

            if (request.getVersion().equalsIgnoreCase("HTTP/1.0") &&
                    !request.getHeaders().getOrDefault("Connection", "").equalsIgnoreCase("keep-alive")) {
                this.keepAlive = false;
            }
        }

        clientSocket.close();
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }
}
