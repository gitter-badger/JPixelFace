package net.rainbowcode.jpixelface.uuid;

import java.util.UUID;

public class ProfileFetchJob {
    private final String name;
    private final UUID uuid;
    private final ProfileFetchRunnable runnable;

    public ProfileFetchJob(String name, ProfileFetchRunnable runnable) {
        this.name = name;
        this.runnable = runnable;
        uuid = null;
    }

    public ProfileFetchJob(UUID uuid, ProfileFetchRunnable runnable) {
        this.name = null;
        this.runnable = runnable;
        this.uuid = uuid;
    }

    public ProfileFetchRunnable getRunnable() {
        return runnable;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
