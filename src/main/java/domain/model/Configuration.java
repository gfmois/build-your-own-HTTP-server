package domain.model;

public class Configuration {
    private final Integer port;

    public Configuration(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Configuration [port=" + port + "]";
    }

    public static class Builder {
        private Integer port;

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Configuration build() {
            return new Configuration(this.port);
        }
    }
}
