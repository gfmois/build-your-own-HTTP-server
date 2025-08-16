import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.RequestMethod;
import model.Request;

public class Main {
  private final static Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting server");

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      
      try (Socket clientSocket = serverSocket.accept()) {
        logger.info("accepted new connection");
        
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

        String line;
        int i = 0;
        Request request = new Request();

        while ((line = input.readLine()) != null && !line.isEmpty()) {
          String[] parts = line.split(" ");
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

        logger.info("Request: {}", request);

        if (request.getPath().equals("/") || request.getPath().equals("")) {
          output.write("HTTP/1.1 200 OK\r\n\r\n");
          output.flush();
        } else {
          output.write("HTTP/1.1 404 Not Found\r\n\r\n");
          output.flush();
        }
      } catch (IOException e) {
        logger.error("IOException: {}", e.getMessage());
      }      
    } catch (IOException e) {
      logger.error("IOException: {}", e.getMessage());
    }
  }
}
