package net.rainbowcode.jpixelface.uuid;

import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.profile.Profile;
import net.rainbowcode.jpixelface.profile.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class ProfileFetcherThread extends Thread
{
    public ConcurrentLinkedQueue<ProfileFetchJob> queue = new ConcurrentLinkedQueue<>();
    private long lastIncrement = System.currentTimeMillis();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public void run()
    {
        while (true)
        {
            long timeSinceIncrement = System.currentTimeMillis() - lastIncrement;
            if (timeSinceIncrement >= 1000L)
            {
                if (HttpServer.requestCounter.get() == 600)
                {
                    lastIncrement = System.currentTimeMillis(); // We set this to make sure we don't increment too early, better late than get banned.
                }
                else
                {
                    HttpServer.requestCounter.incrementAndGet();
                    lastIncrement = System.currentTimeMillis();
                    LOGGER.info("Counter: {}", HttpServer.requestCounter.get());
                }
            }
            if (queue.isEmpty())
            {
                try
                {
                    sleep(10);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {

                ProfileFetchJob pop = queue.poll();

                try
                {
                    boolean found = false;
                    if (pop.getName() != null)
                    {
                        found = ProfileManager.nameExistsInCache(pop.getName());
                    }
                    else if (pop.getUuid() != null)
                    {
                        found = ProfileManager.uuidExistsInCache(pop.getUuid());
                    }

                    executor.execute(() -> {
                        ProfileFetchRunnable runnable = pop.getRunnable();
                        Profile p;
                        if (pop.getName() != null)
                        {
                            p = ProfileManager.getProfileFromName(pop.getName());
                        }
                        else if (pop.getUuid() != null)
                        {
                            p = ProfileManager.getProfileFromUUID(pop.getUuid());
                        }
                        else
                        {
                            LOGGER.warn("A profile request with both name and uuid being null was passed to the fetcher thread!");
                            p = new Profile(null, null, null, null);
                        }
                        runnable.setProfile(p);
                        runnable.run();
                    });

                    if (!found)
                    {
                        sleep(10);
                    }


                }
                catch (Exception e)
                {
                    HttpUtil.sendError(pop.getRunnable().getCtx(), INTERNAL_SERVER_ERROR);
                    e.printStackTrace();
                }
            }

        }
    }

}
