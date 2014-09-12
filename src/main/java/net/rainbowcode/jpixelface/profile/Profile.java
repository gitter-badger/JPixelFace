package net.rainbowcode.jpixelface.profile;

import com.google.gson.JsonObject;

import java.util.UUID;

public class Profile {
    final private String name;
    final private UUID uuid;
    final private String skinUrl;
    final private String capeUrl;

    public Profile(String name, UUID uuid, String skinUrl, String capeUrl) {
        this.name = name;
        this.uuid = uuid;
        this.skinUrl = skinUrl;
        this.capeUrl = capeUrl;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSkinUrl() {
        return skinUrl;
    }

    public String getCapeUrl() {
        return capeUrl;
    }

    public String getMojangUUID() {
        return uuid.toString().replaceAll("-", "");
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", skinUrl='" + skinUrl + '\'' +
                ", capeUrl='" + capeUrl + '\'' +
                '}';
    }

    public JsonObject toJson(){
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("uuid", uuid.toString());
        object.addProperty("mojangUuid", getMojangUUID());
        object.addProperty("skinUrl", skinUrl);
        object.addProperty("capeUrl", capeUrl);
        return object;
    }
}
