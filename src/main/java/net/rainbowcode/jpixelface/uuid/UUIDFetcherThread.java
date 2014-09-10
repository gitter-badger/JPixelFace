package net.rainbowcode.jpixelface.uuid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.squirrelid.util.UUIDs;
import net.rainbowcode.jpixelface.HttpUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class UUIDFetcherThread extends Thread {
    public ConcurrentLinkedQueue<UUIDFetchJob> queue = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String, UUID> uuidCache = new ConcurrentHashMap<>();

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

            UUIDFetchJob pop = queue.poll();
            try {
                boolean found = false;
                UUID uuid;
                if (uuidCache.get(pop.getName().toLowerCase()) == null) {
                    String response = HttpUtil.get("https://api.mojang.com/users/profiles/minecraft/" + pop.getName().toLowerCase());
                    JsonParser parser = new JsonParser();
                    JsonElement parse = parser.parse(response);
                    if (parse != null) {
                        JsonObject object = parse.getAsJsonObject();
                        uuid = UUID.fromString(UUIDs.addDashes(object.getAsJsonPrimitive("id").getAsString()));
                        uuidCache.put(pop.getName().toLowerCase(), uuid);
                    } else {
                        uuid = null;
                    }
                } else {
                    uuid = uuidCache.get(pop.getName().toLowerCase());
                    found = true;
                }

                if (uuid != null) {
                    UUIDFetchRunnable runnable = pop.getRunnable();
                    runnable.setUuid(uuid);
                    runnable.run();
                } else {
                    HttpUtil.sendError(pop.getRunnable().getCtx(), NOT_FOUND);
                }

                if (!found) {
                    sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
