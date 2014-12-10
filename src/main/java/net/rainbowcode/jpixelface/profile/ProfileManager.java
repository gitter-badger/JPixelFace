package net.rainbowcode.jpixelface.profile;

import com.google.gson.*;
import com.sk89q.squirrelid.util.UUIDs;

import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.HttpStringResponse;
import net.rainbowcode.jpixelface.HttpUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ProfileManager
{
    public static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    private static final String KEY_PROFILE_UUID = "profileuuid:";
    private static final String KEY_UUID_NAME = "nameuuid:";

    private static int getTicket(){
    	while (HttpServer.requestCounter.get() == 1)
        {
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

        return HttpServer.requestCounter.decrementAndGet();
    }
    
    private static JsonObject jsonObjectFromString(String string){
    	JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(string).getAsJsonObject();
        return object;
    }
    
    private static void commitProfile(Profile profile) {
        try (Jedis jedis = pool.getResource()) {
        	String key = KEY_PROFILE_UUID + profile.getUuid().toString();
        	jedis.set(key, profile.toJson().toString());
        	jedis.expire(key, 86400);
        	key = KEY_UUID_NAME + profile.getName().toLowerCase();
        	jedis.set(key, profile.getUuid().toString());
        	jedis.expire(key, 86400);
        }
    }
    
	public static Profile getProfileFromUUID(UUID uuid) {
		if (uuid == null) {
			return new Profile(null, null, null, null);
		}
		try (Jedis jedis = pool.getResource()) {
			String key = KEY_PROFILE_UUID + uuid.toString();
			if (jedis.exists(key)) {
				return new Profile(jsonObjectFromString(jedis.get(key)));
			} else {
				try {
					String url = "https://sessionserver.mojang.com/session/minecraft/profile/"
							+ uuid.toString().replaceAll("-", "");

					getTicket();

					HttpStringResponse response = HttpUtil.get(url);

					String string = response.getResponse();
					if (response.getCode() != 200) {
						return new Profile(null, uuid, null, null);
					} else {
						JsonParser parser = new JsonParser();
						JsonObject object = parser.parse(string)
								.getAsJsonObject();
						JsonArray properties = object
								.getAsJsonArray("properties");
						String name = object.getAsJsonPrimitive("name")
								.getAsString();
						JsonObject textures = properties.get(0)
								.getAsJsonObject();
						String value = textures.get("value").getAsString();
						String decoded = new String(Base64.getDecoder().decode(
								value), "UTF-8");
						JsonObject parse = parser.parse(decoded)
								.getAsJsonObject();
						JsonObject texturesOb = parse
								.getAsJsonObject("textures");
						String skinUrl = null;
						String capeUrl = null;
						if (texturesOb != null) {
							JsonObject skinOb = texturesOb
									.getAsJsonObject("SKIN");
							if (skinOb != null) {
								JsonPrimitive primitive = skinOb
										.getAsJsonPrimitive("url");
								skinUrl = primitive.getAsString();
							}
							JsonObject capeOb = texturesOb
									.getAsJsonObject("CAPE");
							if (capeOb != null) {
								JsonPrimitive primitive = capeOb
										.getAsJsonPrimitive("url");
								capeUrl = primitive.getAsString();
							}
						}
						Profile profile = new Profile(name, uuid, skinUrl,
								capeUrl);
						commitProfile(profile);
						return profile;

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		HttpServer.LOGGER.error("Profile from uuid failed hard!");
		return new Profile(null, null, null, null);
	}
	
	private static UUID uuidFromName(String name) {
        name = name.toLowerCase();
		try (Jedis jedis = pool.getResource()) {
			String key = KEY_UUID_NAME + name;
			if (jedis.exists(key)) {
				return UUID.fromString(jedis.get(key));
			} else {
				try {
					getTicket();

					HttpStringResponse response = HttpUtil
							.get("https://api.mojang.com/users/profiles/minecraft/"
									+ name);
					String string = response.getResponse();
					JsonParser parser = new JsonParser();
					if (response.getCode() != 200) {
						return null;
					} else {
						JsonElement parse = parser.parse(string);
						if (parse != null) {
							JsonObject object = parse.getAsJsonObject();
							UUID uuid =  UUID.fromString(UUIDs.addDashes(object
									.getAsJsonPrimitive("id").getAsString()));
							
							jedis.set(key, uuid.toString());
							jedis.expire(key, 86400);
							return uuid;
						} else {
							return null;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		HttpServer.LOGGER.warn("UUID To profile failed hard");
		return null;
	}

    public static Profile getProfileFromName(String name)
    {
        name = name.toLowerCase();
        Profile profile = getProfileFromUUID(uuidFromName(name));
        if (profile.getName() == null)
        {
            profile = new Profile(name, null, null, null);
        }
        return profile;
    }

    public static boolean nameExistsInCache(String name)
    {
        name = name.toLowerCase();
        try (Jedis jedis = pool.getResource()) {
			String key = KEY_UUID_NAME + name;
			return jedis.exists(key);
		}
    }

	public static boolean uuidExistsInCache(UUID uuid) {
		try (Jedis jedis = pool.getResource()) {
			String key = KEY_PROFILE_UUID + uuid.toString();
			return jedis.exists(key);
		}
	}
}
