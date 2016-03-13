package net.rainbowcode.jpixelface.profile;

import com.google.gson.*;
import net.rainbowcode.jpixelface.*;
import net.rainbowcode.jpixelface.exceptions.MojangException;
import net.rainbowcode.jpixelface.redis.RedisKey;
import net.rainbowcode.jpixelface.redis.RedisUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ProfileManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static int getTicket()
    {
        while (HttpServer.requestCounter.get() == 1)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return HttpServer.requestCounter.decrementAndGet();
    }

    private static void commitProfile(Profile profile)
    {
        RedisUtils.setAndExpire(
                RedisKey.PROFILE_UUID.buildKey(profile.getUuid().toString()),
                profile.toJson().toString(), 86400);
        RedisUtils.setAndExpire(
                RedisKey.UUID_NAME.buildKey(profile.getName().toLowerCase()),
                profile.getUuid().toString(), 86400);
    }

    public static Profile getProfileFromUUID(UUID uuid) throws MojangException, IOException
    {
        String key = RedisKey.PROFILE_UUID.buildKey(uuid.toString());
        if (RedisUtils.exists(key))
        {
            return new Profile(RedisUtils.getAsJson(key));
        }
        else
        {
            String url = "https://sessionserver.mojang.com/session/minecraft/profile/"
                    + uuid.toString().replaceAll("-", "");

            getTicket();

            HttpStringResponse response = HttpUtil.getAsString(url);

            String string = response.getResponse();
            if (response.getCode() != 200)
            {
                throw new MojangException(url, response.getCode());
            }
            else
            {
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(string).getAsJsonObject();
                JsonArray properties = object.getAsJsonArray("properties");
                String name = object.getAsJsonPrimitive("name")
                        .getAsString();
                JsonObject textures = properties.get(0).getAsJsonObject();
                String value = textures.get("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(
                        value), "UTF-8");
                JsonObject parse = parser.parse(decoded).getAsJsonObject();
                JsonObject texturesOb = parse.getAsJsonObject("textures");
                final String[] skinUrl = {null};
                final String[] capeUrl = {null};
                if (texturesOb != null)
                {
                    resolve(() -> texturesOb.getAsJsonObject("SKIN").getAsJsonPrimitive("url").getAsString()).ifPresent(x -> skinUrl[0] = x);
                    resolve(() -> texturesOb.getAsJsonObject("CAPE").getAsJsonPrimitive("url").getAsString()).ifPresent(x -> capeUrl[0] = x);
                }
                Profile profile = new Profile(name, uuid, skinUrl[0], capeUrl[0]);
                commitProfile(profile);
                return profile;

            }

        }
    }

    private static UUID uuidFromName(String name) throws MojangException, IOException, JsonParseException
    {
        name = name.toLowerCase();
        String key = RedisKey.UUID_NAME.buildKey(name);
        if (RedisUtils.exists(key))
        {
            return UUID.fromString(RedisUtils.getAsString(key));
        }
        else
        {
            getTicket();
            String path = "https://api.mojang.com/users/profiles/minecraft/" + name;
            HttpStringResponse response = HttpUtil.getAsString(path);
            String string = response.getResponse();
            JsonParser parser = new JsonParser();
            if (response.getCode() != 200)
            {
                throw new MojangException(path, response.getCode());
            }
            else
            {
                JsonElement parse = parser.parse(string);
                JsonObject object = parse.getAsJsonObject();
                UUID uuid = UUID.fromString(StringUtil.addDashes(object
                        .getAsJsonPrimitive("id").getAsString()));
                RedisUtils.setAndExpire(key, uuid.toString(), 86400);
                return uuid;
            }
        }
    }

    public static Profile getProfileFromName(String name) throws MojangException, IOException, JsonParseException
    {
        name = name.toLowerCase();
        return getProfileFromUUID(uuidFromName(name));
    }

    /**
     * Resolves optional objects, dealing with nulls.
     * <p>
     * Nicked from https://github.com/winterbe/java8-tutorial
     * MIT: https://github.com/winterbe/java8-tutorial/blob/master/LICENSE
     *
     * @param resolver
     * @param <T>
     * @return
     */
    public static <T> Optional<T> resolve(Supplier<T> resolver)
    {
        try
        {
            T result = resolver.get();
            return Optional.ofNullable(result);
        }
        catch (NullPointerException e)
        {
            return Optional.empty();
        }
    }
}
