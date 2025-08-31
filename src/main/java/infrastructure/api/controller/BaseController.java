package infrastructure.api.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Configuration;
import domain.model.Controller;
import domain.model.Request;
import domain.model.Response;
import infrastructure.api.Route;
import infrastructure.container.Container;

public class BaseController implements Controller {
        private static final Logger logger = LogManager.getLogger(BaseController.class);

        public void baseHandler(Request request, OutputStreamWriter output) throws IOException {
                Response response = new Response.Builder()
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentLength("2")
                                .body("OK")
                                .build();

                output.write(response.getRawResponse());
                output.flush();
        }

        public void echoHandler(Request request, OutputStreamWriter output) throws IOException {
                String[] pathParts = request.getPath().split("/");
                Response.Builder responseBuilder = new Response.Builder();

                if (pathParts.length < 2) {
                        responseBuilder
                                        .statusCode(400)
                                        .statusMessage("Bad Request")
                                        .contentLength("11")
                                        .contentType("text/plain")
                                        .addHeader("Connection", "close")
                                        .body("Bad Request");
                        output.write(responseBuilder.build().getRawResponse());
                        return;
                }

                String message = pathParts[pathParts.length - 1]; // get last part of the path
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("text/plain")
                                .contentLength(String.valueOf(message.length()))
                                .addHeader("Connection", "close")
                                .body(message);

                output.write(responseBuilder.build().getRawResponse());
        }

        public void userAgentHandler(Request request, OutputStreamWriter output) throws IOException {
                String userAgent = request.getHeaders().get("User-Agent");
                Response.Builder responseBuilder = new Response.Builder();

                if (userAgent == null) {
                        responseBuilder
                                        .statusCode(400)
                                        .statusMessage("Bad Request")
                                        .contentLength("11")
                                        .contentType("text/plain")
                                        .addHeader("Connection", "close")
                                        .body("Bad Request");

                        output.write(responseBuilder.build().getRawResponse());
                        return;
                }

                String trimmedUserAgent = userAgent.trim();
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("text/plain")
                                .contentLength(String.valueOf(trimmedUserAgent.length()))
                                .addHeader("Connection", "close")
                                .body(trimmedUserAgent);

                output.write(responseBuilder.build().getRawResponse());
        }

        public void fileHandler(Request request, OutputStreamWriter output) throws IOException {
                Response.Builder responseBuilder = new Response.Builder();
                Configuration config = Container.getInstance(Configuration.class)
                                .orElseThrow(() -> new RuntimeException("Configuration not found in container"));

                String filesPathDirectory = config.getDirectory();
                String[] pathParts = request.getPath().split("/");
                if (pathParts.length < 3) {
                        responseBuilder
                                        .statusCode(400)
                                        .statusMessage("Bad Request")
                                        .contentLength("11")
                                        .contentType("text/plain")
                                        .body("Bad Request");

                        output.write(responseBuilder.build().getRawResponse());
                        return;
                }

                String fileName = pathParts[2];
                logger.info("Full Path of Requested File: {}/{}", filesPathDirectory, fileName);
                File file = new File(filesPathDirectory, fileName);
                logger.info("Full path to file: {}", file.getAbsolutePath());
                if (!file.exists() || !file.isFile() || !file.canRead()) {
                        responseBuilder
                                        .statusCode(404)
                                        .statusMessage("Not Found")
                                        .contentLength("9")
                                        .contentType("text/plain")
                                        .body("Not Found");

                        output.write(responseBuilder.build().getRawResponse());
                        return;
                }

                String body = Files.readString(file.toPath());
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("application/octet-stream")
                                .contentLength(String.valueOf(body.length()))
                                .body(body);

                output.write(responseBuilder.build().getRawResponse());
        }

        @Override
        public List<Route> getRoutes() {
                String className = this.getClass().getName();
                return List.of(
                                new Route.Builder()
                                                .setPath("/")
                                                .setMethod("GET")
                                                .setHandler("baseHandler")
                                                .setClassName(className)
                                                .setDescription("Base handler")
                                                .setMiddlewares(new String[] {})
                                                .build(),
                                new Route.Builder()
                                                .isSearchByStartsWith(true)
                                                .setPath("/echo")
                                                .setMethod("GET")
                                                .setHandler("echoHandler")
                                                .setClassName(className)
                                                .setDescription("Echoes back the request")
                                                .setMiddlewares(new String[] {})
                                                .build(),
                                new Route.Builder()
                                                .setPath("/user-agent")
                                                .setMethod("GET")
                                                .setHandler("userAgentHandler")
                                                .setClassName(className)
                                                .setDescription("Returns the User-Agent header")
                                                .setMiddlewares(new String[] {})
                                                .build(),
                                new Route.Builder()
                                                .setPath("/files")
                                                .isSearchByStartsWith(true)
                                                .setMethod("GET")
                                                .setHandler("fileHandler")
                                                .setClassName(className)
                                                .setDescription("Returns the file requested as second part of the path")
                                                .setMiddlewares(new String[] {})
                                                .build());
        }
}
