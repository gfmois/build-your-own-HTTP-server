package infrastructure.api;

import java.util.Arrays;

public class Route {
    private String path;
    private String method;
    private String handler;
    private String className;
    private String description;
    private String[] middlewares;

    public Route() {
    }

    public Route(String path, String method, String handler, String description, String[] middlewares,
            String className) {
        this.path = path;
        this.method = method;
        this.handler = handler;
        this.description = description;
        this.middlewares = middlewares;
        this.className = className;
    }

    public static class Builder {
        private String path;
        private String method;
        private String handler;
        private String description;
        private String[] middlewares;
        private String className;

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder setHandler(String handler) {
            this.handler = handler;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setMiddlewares(String[] middlewares) {
            this.middlewares = middlewares;
            return this;
        }

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public Route build() {
            return new Route(path, method, handler, description, middlewares, className);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getMiddlewares() {
        return middlewares;
    }

    public void setMiddlewares(String[] middlewares) {
        this.middlewares = middlewares;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "Route [path=" + path + ", method=" + method + ", handler=" + handler + ", className=" + className
                + ", description=" + description + ", middlewares=" + Arrays.toString(middlewares) + "]";
    }

}
