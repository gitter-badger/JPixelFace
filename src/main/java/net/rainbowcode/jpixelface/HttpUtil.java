package net.rainbowcode.jpixelface;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class HttpUtil
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final OkHttpClient client = new OkHttpClient();

    private static Response get(String url) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "jPixelFace dev")
                .get()
                .build();

        LOGGER.info("Sending 'GET' request to URL : {}", url);
        Response response = client.newCall(request).execute();

        int responseCode = response.code();
        LOGGER.info("Request to url finished: {} - Response code: {}", url, responseCode);
        return response;
    }

    public static HttpStringResponse getAsString(String url) throws IOException
    {
        Response response = get(url);
        return new HttpStringResponse(response.body().string(), response.code());
    }

    public static byte[] getAsBytes(String url) throws IOException
    {
        Response response = get(url);
        return response.body().bytes();
    }

}
