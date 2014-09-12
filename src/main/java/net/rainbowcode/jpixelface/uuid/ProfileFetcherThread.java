package net.rainbowcode.jpixelface.uuid;

import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.profile.Profile;
import net.rainbowcode.jpixelface.profile.ProfileManager;

import java.util.concurrent.ConcurrentLinkedQueue;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class ProfileFetcherThread extends Thread {
    public ConcurrentLinkedQueue<ProfileFetchJob> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while (true) {
            while (queue.isEmpty()) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ProfileFetchJob pop = queue.poll();
            try {
                boolean found = false;
                if (pop.getName() != null) {
                    found = ProfileManager.nameExistsInCache(pop.getName());
                } else if (pop.getUuid() != null) {
                    found = ProfileManager.uuidExistsInCache(pop.getUuid());
                }

                ProfileFetchRunnable runnable = pop.getRunnable();
                Profile p;
                if (pop.getName() != null) {
                    p = ProfileManager.getProfileFromName(pop.getName());
                } else if (pop.getUuid() != null){
                    p = ProfileManager.getProfileFromUUID(pop.getUuid());
                } else {
                    HttpServer.LOGGER.warn("A profile request with both name and uuid being null was passed to the fetcher thread!");
                    p = new Profile(null, null, null, null);
                }
                runnable.setProfile(p);
                runnable.run();


                if (!found) {
                    sleep(1000);
                }
            } catch (Exception e) {
                HttpUtil.sendError(pop.getRunnable().getCtx(), INTERNAL_SERVER_ERROR);
                e.printStackTrace();
            }

        }
    }

}
