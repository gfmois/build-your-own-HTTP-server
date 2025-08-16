import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;

public class Main {
  private final static Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    logger.info("Starting server");

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      serverSocket.accept(); // Wait for connection from client.
      logger.info("accepted new connection");
    } catch (IOException e) {
      logger.error("IOException: " + e.getMessage());
    }
  }
}
