package net.rainbowcode.jpixelface.profile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

public class Profile
{
    final private String name;
    final private UUID uuid;
    final private String skinUrl;
    final private String capeUrl;

    public Profile(String name, UUID uuid, String skinUrl, String capeUrl)
    {
        this.name = name;
        this.uuid = uuid;
        this.skinUrl = skinUrl;
        this.capeUrl = capeUrl;
    }

    public Profile(JsonObject object)
    {
        this.name = ifNotNullGetAsString(object, "name");
        this.uuid = ifNotNullGetAsString(object, "uuid") != null ? UUID.fromString(object.get("uuid").getAsString()) : null;
        this.skinUrl = ifNotNullGetAsString(object, "skinUrl");
        this.capeUrl = ifNotNullGetAsString(object, "capeUrl");
    }

    private String ifNotNullGetAsString(JsonObject object, String string)
    {
        JsonElement jsonElement = object.get(string);
        if (jsonElement != null && !jsonElement.isJsonNull())
        {
            return jsonElement.getAsString();
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        return name;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public String getSkinUrl()
    {
        return skinUrl;
    }

    public String getCapeUrl()
    {
        return capeUrl;
    }

    public String getMojangUUID()
    {
        return uuid.toString().replaceAll("-", "");
    }

    @Override
    public String toString()
    {
        return "Profile{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", skinUrl='" + skinUrl + '\'' +
                ", capeUrl='" + capeUrl + '\'' +
                '}';
    }

    public JsonObject toJson()
    {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("uuid", uuid != null ? uuid.toString() : null);
        object.addProperty("mojangUuid", uuid != null ? getMojangUUID() : null);
        object.addProperty("skinUrl", skinUrl);
        object.addProperty("capeUrl", capeUrl);
        return object;
    }
}
