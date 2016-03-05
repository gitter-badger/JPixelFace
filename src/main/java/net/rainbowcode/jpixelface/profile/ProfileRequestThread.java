package net.rainbowcode.jpixelface.profile;

import net.rainbowcode.jpixelface.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class ProfileRequestThread extends Thread implements Runnable
{
    Queue<ProfileFuture> futureQueue = new LinkedList<>();
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
