package domain.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Response {
    private static final Logger logger = LogManager.getLogger(Response.class);

    private String version = "HTTP/1.1";
    private int statusCode = 200;
    private String statusMessage = "OK";
    private String body = "";
    private final List<Header> headers = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "Response [version=" + version + ", statusCode=" + statusCode + ", statusMessage=" + statusMessage
                + ", body=" + body + ", headers=" + headers + "]";
    }

    public String getRawResponse() {
        StringBuilder rawResponse = new StringBuilder();
        rawResponse.append(version).append(" ").append(statusCode).append(" ").append(statusMessage).append("\r\n");

        if (headers != null && !headers.isEmpty()) {
            for (Header header : headers) {
                rawResponse.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
            }
        }

        // If headers does not contain Content-Length, add it
        boolean hasContentLength = headers.stream().anyMatch(h -> h.getKey().equalsIgnoreCase("Content-Length"));
        boolean hasBody = body != null && !body.isEmpty();
        if (!hasContentLength) {
            rawResponse.append("Content-Length: ").append(hasBody ? body.length() : 0).append("\r\n");
        }

        rawResponse.append("\r\n");
        if (body != null && !body.isEmpty()) {
            rawResponse.append(body);
        }

        return rawResponse.toString();
    }

    public static class Builder {
        private String version = "HTTP/1.1";
        private int statusCode;
        private String statusMessage;
        private String body = "";
        private final List<Header> headers = new ArrayList<>();

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder contentType(String contentType) {
            this.headers.add(new Header("Content-Type", contentType));
            return this;
        }

        public Builder contentLength(String contentLength) {
            this.headers.add(new Header("Content-Length", contentLength));
            return this;
        }

        public Builder addHeader(String key, String value) {
            this.headers.add(new Header(key, value));
            return this;
        }

        public Response build() {
            Response response = new Response();
            response.setVersion(this.version);
            response.setStatusCode(this.statusCode);
            response.setStatusMessage(this.statusMessage);
            response.setBody(this.body);
            response.getHeaders().addAll(this.headers);
            return response;
        }
    }

}
