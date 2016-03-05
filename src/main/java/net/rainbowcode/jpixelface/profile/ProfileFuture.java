package net.rainbowcode.jpixelface.profile;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProfileFuture implements Future<Profile>
{
    private boolean done = false;
    private Profile profile = null;
    private final ProfileType type;
    private final String id;

    public ProfileFuture(ProfileType type, String id)
    {
        this.type = type;
        this.id = id;
    }

    public void setDone(boolean done)
    {
        this.done = done;
    }

    public void setProfile(Profile profile)
    {
        this.profile = profile;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public boolean isDone()
    {
        return done;
    }

    @Override
    public Profile get() throws InterruptedException, ExecutionException
    {
        return profile;
    }

    @Override
    public Profile get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return profile;
    }

    public ProfileType getType()
    {
        return type;
    }

    public String getId()
    {
        return id;
    }
}
