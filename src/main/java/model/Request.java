package model;

import java.util.HashMap;
import java.util.Map;

import enums.RequestMethod;

public class Request {
    private RequestMethod method;
    private String path;
    private String version;
    private Map<String, String> headers; 
    private String body;

    public Request() {
        this.headers = new HashMap<>();
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request[");
        sb.append("method=").append(method);
        sb.append(", path=").append(path);
        sb.append(", version=").append(version);
        sb.append(", headers=").append(headers);
        sb.append(", body=").append(body);
        sb.append("]");
        return sb.toString();
    }

}
