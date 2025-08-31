package enums;

public enum HttpEncoding {
    GZIP("gzip"),
    DEFLATE("deflate"),
    BR("br");

    private final String value;

    HttpEncoding(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidEncoding(String encoding) {
        for (HttpEncoding e : values()) {
            if (e.value.equalsIgnoreCase(encoding)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getAllEncodings() {
        HttpEncoding[] encodings = values();
        String[] result = new String[encodings.length];
        for (int i = 0; i < encodings.length; i++) {
            result[i] = encodings[i].value;
        }
        return result;
    }

    public static HttpEncoding fromString(String encoding) {
        for (HttpEncoding e : values()) {
            if (e.value.equalsIgnoreCase(encoding)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant for encoding: " + encoding);
    }
}
