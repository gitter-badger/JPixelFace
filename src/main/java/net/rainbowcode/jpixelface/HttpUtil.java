package net.rainbowcode.jpixelface;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static HttpStringResponse get(String url) throws IOException {
        URL obj = new URL(url);
        LOGGER.info("Sending 'GET' request to URL : {}", url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "jPixelFace dev");
        int responseCode = con.getResponseCode();
        LOGGER.info("Request to url finished: {} - Response code: {}", url, responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        if (responseCode == 200) {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        in.close();

        return new HttpStringResponse(response.toString(), responseCode);
    }

    public static byte[] getAsBytes(String url) throws IOException {
        URL obj = new URL(url);
        LOGGER.info("Sending 'GET' request to URL : {}", url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "jPixelFace dev");

        int responseCode = con.getResponseCode();
        LOGGER.info("Request to url finished: {} - Response code: {}", url, responseCode);


        return IOUtils.toByteArray(con.getInputStream());
    }

    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
