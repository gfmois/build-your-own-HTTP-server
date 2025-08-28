package infrastructure.api.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import domain.model.Controller;
import domain.model.Request;
import infrastructure.api.Route;

public class BaseController implements Controller {
    public void baseHandler(Request request, OutputStreamWriter output) throws IOException {
        output.write("HTTP/1.1 200 OK\r\n\r\n");
        output.flush();
    }

    public void echoHandler(Request request, OutputStreamWriter output) throws IOException {
        String[] pathParts = request.getPath().split("/");

        if (pathParts.length < 2) {
            output.write("HTTP/1.1 400 Bad Request\r\n");
            output.write("Content-Type: text/plain\r\n");
            output.write("Content-Length: 11\r\n");
            output.write("Connection: close\r\n");

            output.write("\r\n"); // End of headers
            output.write("Bad Request");
            return;
        }

        String message = pathParts[pathParts.length - 1]; // get last part of the path
        output.write("HTTP/1.1 200 OK\r\n");
        output.write("Content-Type: text/plain\r\n");
        output.write("Content-Length: " + message.length() + "\r\n");
        output.write("Connection: close\r\n");

        output.write("\r\n"); // End of headers
        output.write(message);
    }

    public void userAgentHandler(Request request, OutputStreamWriter output) throws IOException {
        String userAgent = request.getHeaders().get("User-Agent");

        if (userAgent == null) {
            output.write("HTTP/1.1 400 Bad Request\r\n");
            output.write("Content-Type: text/plain\r\n");
            output.write("Content-Length: 11\r\n");
            output.write("Connection: close\r\n");

            output.write("\r\n"); // End of headers
            output.write("Bad Request");
            return;
        }

        output.write("HTTP/1.1 200 OK\r\n");
        output.write("Content-Type: text/plain\r\n");
        output.write("Connection: close\r\n");

        String trimmedUserAgent = userAgent.trim();
        output.write("Content-Length: " + trimmedUserAgent.length() + "\r\n");
        output.write("\r\n");
        output.write(trimmedUserAgent);
    }

    @Override
    public List<Route> getRoutes() {
        return List.of(
                new Route.Builder()
                        .isSearchByStartsWith(true)
                        .setPath("/echo")
                        .setMethod("GET")
                        .setHandler("echoHandler")
                        .setClassName(this.getClass().toString())
                        .setDescription("Echoes back the request")
                        .setMiddlewares(new String[] {})
                        .build(),
                new Route.Builder()
                        .setPath("/user-agent")
                        .setMethod("GET")
                        .setHandler("userAgentHandler")
                        .setClassName(this.getClass().toString())
                        .setDescription("Returns the User-Agent header")
                        .setMiddlewares(new String[] {})
                        .build(),
                new Route.Builder()
                        .setPath("/")
                        .setMethod("GET")
                        .setHandler("baseHandler")
                        .setClassName(this.getClass().toString())
                        .setDescription("Base handler")
                        .setMiddlewares(new String[] {})
                        .build());
    }
}
