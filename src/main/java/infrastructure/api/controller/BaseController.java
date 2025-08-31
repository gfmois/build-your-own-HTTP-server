package infrastructure.api.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

        public Response baseHandler(Request request, OutputStream output) throws IOException {
                Response response = new Response.Builder()
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentLength("2")
                                .body("OK")
                                .build();

                return response;
        }

        public Response echoHandler(Request request, OutputStream output) throws IOException {
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
                        return responseBuilder.build();
                }

                String message = pathParts[pathParts.length - 1]; // get last part of the path
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("text/plain")
                                .contentLength(String.valueOf(message.length()))
                                .body(message);

                return responseBuilder.build();
        }

        public Response userAgentHandler(Request request, OutputStream output) throws IOException {
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

                        return responseBuilder.build();
                }

                String trimmedUserAgent = userAgent.trim();
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("text/plain")
                                .contentLength(String.valueOf(trimmedUserAgent.length()))
                                .body(trimmedUserAgent);

                return responseBuilder.build();
        }

        public Response fileHandler(Request request, OutputStream output) throws IOException {
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

                        return responseBuilder.build();
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

                        return responseBuilder.build();
                }

                String body = Files.readString(file.toPath());
                responseBuilder
                                .statusCode(200)
                                .statusMessage("OK")
                                .contentType("application/octet-stream")
                                .contentLength(String.valueOf(body.length()))
                                .body(body);

                return responseBuilder.build();
        }

        public Response saveFileHandler(Request request, OutputStream output) throws IOException {
                Response.Builder responseBuilder = new Response.Builder();
                Configuration config = Container.getInstance(Configuration.class)
                                .orElseThrow(() -> new RuntimeException("Configuration not found in container"));

                String filesPathDirectory = config.getDirectory();
                String[] pathParts = request.getPath().split("/");
                if (pathParts.length < 3 || request.getBody() == null || request.getBody().isEmpty()) {
                        responseBuilder
                                        .statusCode(400)
                                        .statusMessage("Bad Request")
                                        .contentLength("11")
                                        .contentType("text/plain")
                                        .body("Bad Request");

                        return responseBuilder.build();
                }

                String fileName = pathParts[2];
                String body = request.getBody();
                File file = new File(filesPathDirectory, fileName);
                Files.writeString(file.toPath(), body);
                logger.info("File saved: {}", file.getAbsolutePath());
                responseBuilder
                                .statusCode(201)
                                .statusMessage("Created");

                return responseBuilder.build();
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
                                                .build(),
                                new Route.Builder()
                                                .setPath("/files")
                                                .isSearchByStartsWith(true)
                                                .setMethod("POST")
                                                .setHandler("saveFileHandler")
                                                .setClassName(className)
                                                .setDescription("Saves the file sent in the body")
                                                .setMiddlewares(new String[] {})
                                                .build());
        }
}
