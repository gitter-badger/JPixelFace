package net.rainbowcode.jpixelface.uuid;

public class UUIDFetchJob {
    private final String name;
    private final UUIDFetchRunnable runnable;

    public UUIDFetchJob(String name, UUIDFetchRunnable runnable) {
        this.name = name;
        this.runnable = runnable;
    }

    public UUIDFetchRunnable getRunnable() {
        return runnable;
    }

    public String getName() {
        return name;
    }
}
