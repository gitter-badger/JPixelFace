package net.rainbowcode.jpixelface.profile;

import net.rainbowcode.jpixelface.exceptions.MojangException;

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
    private Exception exception = null;

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

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    public void await() throws Exception
    {
        while (!isDone())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (getException() != null)
        {
            if (getException() instanceof MojangException)
            {
                MojangException mojangException = (MojangException) getException();
                if (mojangException.getCode() == 204) // Handle people without profile
                {
                    return;
                }
            }
            throw getException();
        }
    }
}
