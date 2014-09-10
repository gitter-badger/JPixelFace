package net.rainbowcode.jpixelface.uuid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.squirrelid.util.UUIDs;
import net.rainbowcode.jpixelface.HttpStringResponse;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.TimedConcurrentCache;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class UUIDFetcherThread extends Thread {
    public ConcurrentLinkedQueue<UUIDFetchJob> queue = new ConcurrentLinkedQueue<>();
    private TimedConcurrentCache<String, UUID> uuidCache = new TimedConcurrentCache<>(86400000L);

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
                    HttpStringResponse response = HttpUtil.get("https://api.mojang.com/users/profiles/minecraft/" + pop.getName().toLowerCase());
                    String string = response.getResponse();
                    JsonParser parser = new JsonParser();
                    if (response.getCode() != 200) {
                        uuid = null;
                    } else {
                        JsonElement parse = parser.parse(string);
                        if (parse != null) {
                            JsonObject object = parse.getAsJsonObject();
                            uuid = UUID.fromString(UUIDs.addDashes(object.getAsJsonPrimitive("id").getAsString()));
                            uuidCache.put(pop.getName().toLowerCase(), uuid);
                        } else {
                            uuid = null;
                        }
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
            } catch (Exception e) {
                HttpUtil.sendError(pop.getRunnable().getCtx(), INTERNAL_SERVER_ERROR);
                e.printStackTrace();
            }

        }
    }

}
