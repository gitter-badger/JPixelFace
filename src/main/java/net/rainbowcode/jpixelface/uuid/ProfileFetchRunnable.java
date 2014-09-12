package net.rainbowcode.jpixelface.uuid;

import io.netty.channel.ChannelHandlerContext;
import net.rainbowcode.jpixelface.profile.Profile;

import java.util.UUID;

public abstract class ProfileFetchRunnable implements Runnable {
    private Profile profile;

    private final ChannelHandlerContext ctx;

    protected ProfileFetchRunnable(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Profile getProfile() {
        return profile;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
