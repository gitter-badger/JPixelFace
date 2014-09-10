package net.rainbowcode.jpixelface.skin;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

public class SkinFetchJob {
    private final UUID uuid;
    private final ChannelHandlerContext ctx;
    private final Mutate mutate;
    private final int size;

    public SkinFetchJob(UUID uuid, ChannelHandlerContext ctx, Mutate mutate) {
        this.uuid = uuid;
        this.ctx = ctx;
        this.mutate = mutate;
        size = 64;
    }

    public SkinFetchJob(UUID uuid, ChannelHandlerContext ctx, Mutate mutate, int size) {
        this.uuid = uuid;
        this.ctx = ctx;
        this.mutate = mutate;
        this.size = size;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public Mutate getMutate() {
        return mutate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getSize() {
        return size;
    }
}
