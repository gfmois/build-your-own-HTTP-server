package infrastructure.api;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Controller;
import domain.model.Header;
import domain.model.Request;
import domain.model.Response;
import enums.HttpEncoding;
import infrastructure.container.Container;

public class Router {
    private final static Logger logger = LogManager.getLogger(Router.class);
    private final static List<Route> routes = new ArrayList<>();

    public Router() {
    }

    // Accept multiple controllers and aggregate their routes
    public static void registerControllers(List<Controller> controllers) {
        List<Route> allRoutes = new ArrayList<>();
        for (Controller controller : controllers) {
            allRoutes.addAll(controller.getRoutes());
        }

        routes.clear();
        routes.addAll(allRoutes);
    }

    public void perform(Request request, OutputStream output) {
        Route route = resolve(request, output);
        try {
            Class<?> controllerClass = Class.forName(route.getClassName().replace("class ", ""));
            Object controllerInstance = Container.getOrCreate(controllerClass);
            Object responseObj = controllerClass.getMethod(route.getHandler(), Request.class, OutputStream.class)
                    .invoke(controllerInstance, request, output);

            if (responseObj instanceof Response response) {
                ensureConnectionHeader(response, output);

                logger.info("Response: {}", response);
                boolean hasBeenEncoded = encodeResponse(request, output, response);
                output.write(response.getRawResponse().getBytes(StandardCharsets.UTF_8));

                if (hasBeenEncoded) {
                    output.write(response.getByteBody());
                }

                output.flush();
            } else {
                throw new IllegalStateException("Handler did not return a Response object");
            }
        } catch (Exception e) {
            logger.error("Error performing route action: {} - {}", e.getMessage(), e.getCause());
        }
    }

    public Route resolve(Request request, OutputStream output) {
        for (Route route : routes) {
            if (route.isSeachByStartsWith() && request.getPath().startsWith(route.getPath())
                    && route.getMethod().equals(request.getMethod().name())) {
                return route;
            }

            if (route.getPath().equals(request.getPath()) && route.getMethod().equals(request.getMethod().name())) {
                return route;
            }
        }

        return new Route.Builder()
                .setPath("/404")
                .setMethod("GET")
                .setHandler("notFoundHandler")
                .setClassName(this.getClass().toString())
                .setDescription("Route not found")
                .setMiddlewares(new String[] {})
                .build();
    }

    public void notFoundHandler(Request request, OutputStream output) {
        try {
            String message = "404 Not Found";
            Response response = new Response.Builder()
                    .version("HTTP/1.1")
                    .statusCode(404)
                    .statusMessage("Not Found")
                    .body(message)
                    .contentType("text/plain")
                    .contentLength(String.valueOf(message.length()))
                    .build();

            output.write(response.getByteResponse());
            output.flush();
        } catch (Exception e) {
            logger.error("Error in notFoundHandler: {}", e.getMessage());
        }
    }

    private boolean encodeResponse(Request request, OutputStream output, Response response) {
        // Check if request contains 'Accept-Encoding' header and apply encoding type if
        // needed
        if (request.getHeaders().containsKey("Accept-Encoding")) {
            logger.info("Client supports encodings: {}", request.getHeaders().get("Accept-Encoding"));
            String[] encodings = request.getHeaders().get("Accept-Encoding").split(",");
            List<HttpEncoding> serverSupportedEncodingsFound = new ArrayList<>();

            for (String encoding : encodings) {
                if (HttpEncoding.isValidEncoding(encoding.trim())) {
                    serverSupportedEncodingsFound.add(HttpEncoding.fromString(encoding.trim()));
                }
            }

            AtomicInteger i = new AtomicInteger(0);
            return applyEncoding(serverSupportedEncodingsFound, i, output, request, response);
        }

        return false;
    }

    private boolean applyEncoding(
            List<HttpEncoding> serverSupportedEncodingsFound,
            AtomicInteger i,
            OutputStream output,
            Request request,
            Response response) {
        if (!serverSupportedEncodingsFound.isEmpty()) {
            HttpEncoding selectedEncoding = serverSupportedEncodingsFound.get(i.get());
            logger.info("Selected encoding: {}", selectedEncoding.getValue());
            try {
                return response.encodeBody(selectedEncoding, output);
            } catch (Exception e) {
                logger.error("Error applying encoding {}: {}", selectedEncoding.getValue(), e.getMessage());
                logger.warn("Trying next encoding if available...");
                if (i.incrementAndGet() < serverSupportedEncodingsFound.size()) {
                    return applyEncoding(serverSupportedEncodingsFound, i, output, request, response);
                } else {
                    logger.warn("No more encodings to try.");
                }
                return false;
            }
        }

        return false;
    }

    public void ensureConnectionHeader(Response response, OutputStream output) {
        boolean isKeepAlive = Container.getServerInstance().isKeepAlive();
        try {
            Optional<Header> existing = response.getHeaders().stream()
                    .filter(h -> h.getKey().equalsIgnoreCase("connection"))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setValue(isKeepAlive ? "keep-alive" : "close");
            } else {
                response.getHeaders().add(new Header("Connection", isKeepAlive ? "keep-alive" : "close"));
            }
        } catch (Exception e) {
            logger.error("Error ensuring Connection header: {}", e.getMessage());
        }
    }
}
