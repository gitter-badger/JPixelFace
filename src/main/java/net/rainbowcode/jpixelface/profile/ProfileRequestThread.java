package net.rainbowcode.jpixelface.profile;

import com.google.gson.JsonParseException;
import net.rainbowcode.jpixelface.StringUtil;
import net.rainbowcode.jpixelface.exceptions.MojangException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProfileRequestThread extends Thread implements Runnable
{
    Queue<ProfileFuture> futureQueue = new ConcurrentLinkedQueue<>();
    private final Logger log = LogManager.getLogger();

    @Override
    public void run()
    {
        while (true)
        {
            if (!futureQueue.isEmpty())
            {
                for (ProfileFuture future : futureQueue)
                {
                    log.info("Processing profile request: Type: " + future.getType().name() + ", id: " + future.getId());
                    try
                    {
                        if (future.getType().equals(ProfileType.NAME))
                        {
                            Profile profileFromName = ProfileManager.getProfileFromName(future.getId());
                            future.setProfile(profileFromName);
                            future.setDone(true);
                        }
                        else if (future.getType().equals(ProfileType.MOJANGUUID))
                        {
                            Profile profileFromUUID = ProfileManager.getProfileFromUUID(UUID.fromString(StringUtil.addDashes(future.getId())));
                            future.setProfile(profileFromUUID);
                            future.setDone(true);
                        }
                        else if (future.getType().equals(ProfileType.UUID))
                        {
                            Profile profileFromUUID = ProfileManager.getProfileFromUUID(UUID.fromString(future.getId()));
                            future.setProfile(profileFromUUID);
                            future.setDone(true);
                        }
                    }
                    catch (MojangException | IOException | JsonParseException ex)
                    {
                        log.info("Failed processing profile request: Type: " + future.getType().name() + ", id: " + future.getId() + ", exception class:" + ex.getClass());
                        future.setException(ex);
                        future.setDone(true);
                    }
                    log.info("Done processing profile request: Type: " + future.getType().name() + ", id: " + future.getId());
                }
                futureQueue.removeIf(ProfileFuture::isDone);
            }
            try
            {
                sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public ProfileFuture getProfileByName(String name)
    {
        ProfileFuture future = new ProfileFuture(ProfileType.NAME, name);
        futureQueue.add(future);
        return future;
    }

    public ProfileFuture getProfileByMojangID(String id)
    {
        ProfileFuture future = new ProfileFuture(ProfileType.MOJANGUUID, id);
        futureQueue.add(future);
        return future;
    }

    public ProfileFuture getProfileByUUID(String id)
    {
        ProfileFuture future = new ProfileFuture(ProfileType.UUID, id);
        futureQueue.add(future);
        return future;
    }
}
