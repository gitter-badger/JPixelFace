/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package net.rainbowcode.jpixelface;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.rainbowcode.jpixelface.skin.Mutate;
import net.rainbowcode.jpixelface.skin.SkinFetchJob;
import net.rainbowcode.jpixelface.uuid.UUIDFetchJob;
import net.rainbowcode.jpixelface.uuid.UUIDFetchRunnable;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class HttpServerHandler extends ChannelHandlerAdapter {
    private final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{2,16}$");

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println(request.getUri());
            for (Mutate mutate : Mutate.values()) {
                if (request.getUri().startsWith(mutate.getPath())) {
                    String[] split = request.getUri().split(mutate.getPath());
                    if (split.length == 2 || split.length == 3) {
                        String name = getName(split[1]);
                        if (NAME.matcher(name).find()) {
                            int scale = getScale(split[1], mutate);
                            if (scale != -1) {
                                sendSkin(ctx, name, mutate, scale);
                            } else {
                                HttpUtil.sendError(ctx, HttpResponseStatus.NOT_ACCEPTABLE);
                            }

                        } else {
                            HttpUtil.sendError(ctx, NOT_FOUND);
                        }
                    }
                    return;
                }
            }


            final String uri = request.getUri();
            try {

                File publicDir = new File("public");
                if (uri.equals("/")) {
                    sendFile(new File(publicDir, "index.html"), ctx);
                } else {
                    for (File file : scanDir(publicDir)) {
                        String path = file.getPath().replace("public", "");
                        path = path.replaceAll("\\\\", "/");
                        if (path.equals(uri)) {
                            sendFile(file, ctx);
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            HttpUtil.sendError(ctx, NOT_FOUND);
        }

    }

    private int getScale(String s, Mutate mutate) {
        int MIN_SCALE = 8;
        int DEFAULT_SCALE = 64;
        int MAX_SCALE = 512;

        if (mutate.equals(Mutate.BODY) || mutate.equals(Mutate.BODY_NOLAYER) || mutate.equals(Mutate.TORSO) || mutate.equals(Mutate.TORSO_NOLAYER)) {
            MIN_SCALE = 1;
            MAX_SCALE = 128;
        }

        try {
            String[] split = s.split("/");

            if (split.length == 2) {
                int number = Integer.parseInt(getName(split[1]));
                if (number >= MIN_SCALE && number <= MAX_SCALE) {
                    return number;
                } else {
                    return -1;
                }
            }
        } catch (NumberFormatException e) {
            return DEFAULT_SCALE;
        }

        return DEFAULT_SCALE;
    }

    public void sendFile(File file, ChannelHandlerContext ctx) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(IOUtils.toByteArray(new FileInputStream(file))));
        String mime = "text/plain";
        if (file.getName().endsWith(".js")) {
            mime = "application/javascript";
        } else if (file.getName().endsWith(".css")) {
            mime = "text/css";
        } else if (file.getName().endsWith(".html")) {
            mime = "text/html";
        }
        response.headers().set(CONTENT_TYPE, mime);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private ArrayList<File> scanDir(File base) {
        ArrayList<File> files = new ArrayList<>();
        for (String s : base.list()) {
            File file = new File(base, s);
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(scanDir(file));
            }
        }
        return files;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendSkin(ChannelHandlerContext ctx, String name, Mutate mutate, int size) {
        HttpServer.UUID_FETCHER_THREAD.queue.add(new UUIDFetchJob(name, new UUIDFetchRunnable(ctx) {
            @Override
            public void run() {
                HttpServer.SKIN_FETCHER_THREAD.queue.add(new SkinFetchJob(getUuid(), getCtx(), mutate, size));
            }
        }));
    }

    private String getName(String string) {
        String[] split = string.split("/");
        return split[0];
    }
}