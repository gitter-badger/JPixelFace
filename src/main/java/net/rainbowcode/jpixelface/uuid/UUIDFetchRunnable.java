package net.rainbowcode.jpixelface.uuid;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

public abstract class UUIDFetchRunnable implements Runnable {
    private UUID uuid;
    private final ChannelHandlerContext ctx;

    protected UUIDFetchRunnable(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
