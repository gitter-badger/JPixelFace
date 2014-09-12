package net.rainbowcode.jpixelface.profile;

import com.google.gson.*;
import com.sk89q.squirrelid.util.UUIDs;
import net.rainbowcode.jpixelface.HttpStringResponse;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.TimedConcurrentCache;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class ProfileManager {
    private static final TimedConcurrentCache<UUID, Profile> profiles = new TimedConcurrentCache<>(86400000L);
    private static final TimedConcurrentCache<String, UUID> uuids = new TimedConcurrentCache<>(86400000L);

    public static Profile getProfileFromUUID(UUID uuid) {
        if (uuid == null) {
            return new Profile(null, null, null, null);
        }
        return profiles.computeIfAbsent(uuid, uuid1 -> {
            try {
                String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid1.toString().replaceAll("-", "");

                HttpStringResponse response = HttpUtil.get(url);

                String string = response.getResponse();
                if (response.getCode() != 200) {
                    return new Profile(null, uuid1, null, null);
                } else {
                    JsonParser parser = new JsonParser();
                    JsonObject object = parser.parse(string).getAsJsonObject();
                    JsonArray properties = object.getAsJsonArray("properties");
                    String name = object.getAsJsonPrimitive("name").getAsString();
                    JsonObject textures = properties.get(0).getAsJsonObject();
                    String value = textures.get("value").getAsString();
                    String decoded = new String(Base64.getDecoder().decode(value), "UTF-8");
                    JsonObject parse = parser.parse(decoded).getAsJsonObject();
                    JsonObject texturesOb = parse.getAsJsonObject("textures");
                    String skinUrl = null;
                    String capeUrl = null;
                    if (texturesOb != null) {
                        JsonObject skinOb = texturesOb.getAsJsonObject("SKIN");
                        if (skinOb != null) {
                            JsonPrimitive primitive = skinOb.getAsJsonPrimitive("url");
                            skinUrl = primitive.getAsString();
                        }
                        JsonObject capeOb = texturesOb.getAsJsonObject("CAPE");
                        if (capeOb != null) {
                            JsonPrimitive primitive = capeOb.getAsJsonPrimitive("url");
                            capeUrl = primitive.getAsString();
                        }
                    }
                    uuids.put(name.toLowerCase(), uuid);
                    return new Profile(name, uuid1, skinUrl, capeUrl);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Profile(null, uuid1, null, null);
        });
    }

    public static Profile getProfileFromName(String name) {
        name = name.toLowerCase();
        Profile profile = getProfileFromUUID(uuids.computeIfAbsent(name, s -> {
            try {
                HttpStringResponse response = HttpUtil.get("https://api.mojang.com/users/profiles/minecraft/" + s);
                String string = response.getResponse();
                JsonParser parser = new JsonParser();
                if (response.getCode() != 200) {
                    return null;
                } else {
                    JsonElement parse = parser.parse(string);
                    if (parse != null) {
                        JsonObject object = parse.getAsJsonObject();
                        return UUID.fromString(UUIDs.addDashes(object.getAsJsonPrimitive("id").getAsString()));
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }));
        if (profile.getName() == null) {
            return new Profile(name, null, null, null);
        }
        return profile;
    }

    public static boolean nameExistsInCache(String name) {
        name = name.toLowerCase();
        return uuids.contains(name);
    }

    public static boolean uuidExistsInCache(UUID uuid){
        return profiles.contains(uuid);
    }
}
