package net.rainbowcode.jpixelface;

public class HttpStringResponse {
    private final String response;
    private final int code;

    public HttpStringResponse(String response, int code) {
        this.response = response;
        this.code = code;
    }

    public String getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }
}
