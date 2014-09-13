package net.rainbowcode.jpixelface;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.rainbowcode.jpixelface.skin.SkinFetcherThread;
import net.rainbowcode.jpixelface.uuid.ProfileFetcherThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HttpServer {

    static final int PORT = 8000;
    static final ProfileFetcherThread PROFILE_FETCHER_THREAD = new ProfileFetcherThread();
    static final SkinFetcherThread SKIN_FETCHER_THREAD = new SkinFetcherThread();
    public static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        PROFILE_FETCHER_THREAD.start();
        SKIN_FETCHER_THREAD.start();
        LOGGER.info("Starting server...");
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer());

            Channel ch = b.bind(PORT).sync().channel();

            LOGGER.info("Started on port: {}", PORT);

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}