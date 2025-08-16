import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
  private final static Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting server");

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      
      try (Socket clientSocket = serverSocket.accept()) {
        logger.info("accepted new connection");
        
        // InputStreamReader input = new InputStreamReader(clientSocket.getInputStream());
        OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

        output.write("HTTP/1.1 200 OK\r\n\r\n");
        output.flush();
      } catch (IOException e) {
        logger.error("IOException: {}", e.getMessage());
      }      
    } catch (IOException e) {
      logger.error("IOException: {}", e.getMessage());
    }
  }
}
