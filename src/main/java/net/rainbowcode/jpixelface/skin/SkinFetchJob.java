package net.rainbowcode.jpixelface.skin;

import io.netty.channel.ChannelHandlerContext;
import net.rainbowcode.jpixelface.profile.Profile;

import java.util.UUID;

public class SkinFetchJob {
    private final Profile profile;
    private final ChannelHandlerContext ctx;
    private final Mutate mutate;
    private final int size;

    public SkinFetchJob(Profile profile, ChannelHandlerContext ctx, Mutate mutate, int size) {
        this.profile = profile;
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

    public Profile getProfile() {
        return profile;
    }

    public int getSize() {
        return size;
    }
}
