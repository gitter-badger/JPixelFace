package net.rainbowcode.jpixelface.profile;

import com.google.gson.*;
import com.sk89q.squirrelid.util.UUIDs;

import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.HttpStringResponse;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.RedisKey;
import net.rainbowcode.jpixelface.RedisUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class ProfileManager {

	private static int getTicket() {
		while (HttpServer.requestCounter.get() == 1) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return HttpServer.requestCounter.decrementAndGet();
	}

	private static void commitProfile(Profile profile) {
		RedisUtils.setAndExpire(
				RedisKey.PROFILE_UUID.buildKey(profile.getUuid().toString()),
				profile.toJson().toString(), 86400);
		RedisUtils.setAndExpire(
				RedisKey.UUID_NAME.buildKey(profile.getName().toLowerCase()),
				profile.getUuid().toString(), 86400);
	}

	public static Profile getProfileFromUUID(UUID uuid) {
		if (uuid == null) {
			return new Profile(null, null, null, null);
		}
		String key = RedisKey.PROFILE_UUID.buildKey(uuid.toString());
		if (RedisUtils.exists(key)) {
			return new Profile(RedisUtils.getAsJson(key));
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
					String skinUrl = null;
					String capeUrl = null;
					if (texturesOb != null) {
						JsonObject skinOb = texturesOb.getAsJsonObject("SKIN");
						if (skinOb != null) {
							JsonPrimitive primitive = skinOb
									.getAsJsonPrimitive("url");
							skinUrl = primitive.getAsString();
						}
						JsonObject capeOb = texturesOb.getAsJsonObject("CAPE");
						if (capeOb != null) {
							JsonPrimitive primitive = capeOb
									.getAsJsonPrimitive("url");
							capeUrl = primitive.getAsString();
						}
					}
					Profile profile = new Profile(name, uuid, skinUrl, capeUrl);
					commitProfile(profile);
					return profile;

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		HttpServer.LOGGER.error("Profile from uuid failed hard!");
		return new Profile(null, null, null, null);
	}

	private static UUID uuidFromName(String name) {
		name = name.toLowerCase();
		String key = RedisKey.UUID_NAME.buildKey(name);
		if (RedisUtils.exists(key)) {
			return UUID.fromString(RedisUtils.getAsString(key));
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
						UUID uuid = UUID.fromString(UUIDs.addDashes(object
								.getAsJsonPrimitive("id").getAsString()));
						RedisUtils.setAndExpire(key, uuid.toString(), 86400);
						return uuid;
					} else {
						return null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		HttpServer.LOGGER.warn("UUID To profile failed hard");
		return null;
	}

	public static Profile getProfileFromName(String name) {
		name = name.toLowerCase();
		Profile profile = getProfileFromUUID(uuidFromName(name));
		if (profile.getName() == null) {
			profile = new Profile(name, null, null, null);
		}
		return profile;
	}

	public static boolean nameExistsInCache(String name) {
		name = name.toLowerCase();
		return RedisUtils.exists(RedisKey.UUID_NAME.buildKey(name));
	}

	public static boolean uuidExistsInCache(UUID uuid) {
		return RedisUtils.exists(RedisKey.PROFILE_UUID.buildKey(uuid.toString()));
	}
}
