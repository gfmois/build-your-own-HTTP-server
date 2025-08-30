package infrastructure.api;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domain.model.Controller;
import domain.model.Request;
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

    public void perform(Request request, OutputStreamWriter output) {
        Route route = resolve(request, output);
        try {
            Class<?> controllerClass = Class.forName(route.getClassName().replace("class ", ""));
            Object controllerInstance = Container.getOrCreate(controllerClass);
            controllerClass.getMethod(route.getHandler(), Request.class, OutputStreamWriter.class)
                    .invoke(controllerInstance, request, output);
            output.flush();
        } catch (Exception e) {
            logger.error("Error performing route action: {} - {}", e.getMessage(), e.getCause());
        }
    }

    public Route resolve(Request request, OutputStreamWriter output) {
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

    public void notFoundHandler(Request request, OutputStreamWriter output) {
        try {
            String message = "404 Not Found";
            output.write("HTTP/1.1 404 Not Found\r\n");
            output.write("Content-Type: text/plain\r\n");
            output.write("Content-Length: " + message.length() + "\r\n");
            output.write("\r\n"); // End of headers
            output.write(message);
            output.flush();
        } catch (Exception e) {
            logger.error("Error in notFoundHandler: {}", e.getMessage());
        }
    }
}
