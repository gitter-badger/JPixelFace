package net.rainbowcode.jpixelface;

import com.sk89q.squirrelid.util.UUIDs;
import net.rainbowcode.jpixelface.profile.ProfileManager;
import net.rainbowcode.jpixelface.skin.Mutate;
import spark.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static spark.Spark.*;

public final class HttpServer
{

    static final int PORT = 8000;
    public static AtomicInteger requestCounter = new AtomicInteger(600);
    public static Thread tickCounter = new Thread()
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                if (HttpServer.requestCounter.get() < 600)
                {
                    System.out.println("Requests available to mojang = " + HttpServer.requestCounter.incrementAndGet());
                }
            }
        }
    };

    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{2,16}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}[0-9a-f]{4}[1-5][0-9a-f]{3}[89ab][0-9a-f]{3}[0-9a-f]{12}$");
    private static final Pattern REAL_UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
    public static SkinManager skinManager = new SkinManager();

    public static void main(String[] args) throws Exception
    {
        tickCounter.start();
        staticFileLocation("/public");
        port(PORT);

        before((request1, response1) -> {
            response1.header("X-host", System.getenv("HOSTNAME"));
            String key = "cache:" + request1.uri();
            response1.header("Cache-Control", "public, max-age=86400");
            ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("GMT"));
            response1.header("Date", now.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")));
            String oldAge = now.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz"));
            if (RedisUtils.exists(key))
            {
                oldAge = RedisUtils.getAsString(key);
                String modified = request1.headers("If-Modified-Since");
                if (modified != null && modified.equals(oldAge))
                {
                    halt(304);
                }
            }
            else
            {
                RedisUtils.setAndExpire(key, oldAge, 86400);
            }
            response1.header("Last-Modified", oldAge);
        });

        for (Mutate mutate : Mutate.values())
        {
            get(mutate.getPath() + ":id", (request, response) -> {
                String id = request.params("id").replace(".png", "");
                int size = 64;

                if (NAME.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromName(id), size, mutate));
                    return null;
                }
                else if (UUID_PATTERN.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromUUID(UUID.fromString(UUIDs.addDashes(id))), size, mutate));
                    return null;
                }
                else if (REAL_UUID_PATTERN.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromUUID(UUID.fromString(id)), size, mutate));
                    return null;
                }


                halt(403, "Not acceptable input");
                return "Not acceptable input";
            });

            get(mutate.getPath() + ":id/:size", (request, response) -> {
                String id = request.params("id").replace(".png", "");
                int size = -1;

                try
                {
                    size = Integer.parseInt(request.params("size").replace(".png", ""));
                }
                catch (NumberFormatException e)
                {
                    halt(403, "Not acceptable input: Size input is not a number");
                    return "Not acceptable input: Size input is not a number";
                }
                
                int MIN_SCALE = 8;
                int MAX_SCALE = 512;

                if (mutate.equals(Mutate.BODY) || mutate.equals(Mutate.BODY_NOLAYER) || mutate.equals(Mutate.TORSO) || mutate.equals(Mutate.TORSO_NOLAYER))
                {
                    MIN_SCALE = 1;
                    MAX_SCALE = 128;
                }

                if (size > MAX_SCALE || size < MIN_SCALE)
                {
                    halt(403, "Not acceptable input: Scale out of bounds (" + MIN_SCALE + " - " + MAX_SCALE + ")");
                    return "Not acceptable input: Scale out of bounds (" + MIN_SCALE + " - " + MAX_SCALE + ")";
                }

                if (NAME.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromName(id), size, mutate));
                    return null;
                }
                else if (UUID_PATTERN.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromUUID(UUID.fromString(UUIDs.addDashes(id))), size, mutate));
                    return null;
                }
                else if (REAL_UUID_PATTERN.matcher(id).find())
                {
                    sendPng(response, skinManager.getMutated(ProfileManager.getProfileFromUUID(UUID.fromString(id)), size, mutate));
                    return null;
                }

                halt(403, "Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)");
                return "Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)";
            });
        }


        get("/profile/:id", (request, response) -> {
            String id = request.params("id").replace(".json", "");
            response.type("application/json");

            if (NAME.matcher(id).find())
            {
                return ProfileManager.getProfileFromName(id).toJson();
            }
            else if (UUID_PATTERN.matcher(id).find())
            {
                return ProfileManager.getProfileFromUUID(UUID.fromString(UUIDs.addDashes(id))).toJson();
            }
            else if (REAL_UUID_PATTERN.matcher(id).find())
            {
                return ProfileManager.getProfileFromUUID(UUID.fromString(id)).toJson();
            }

            halt(403, "Not acceptable input");
            return "Not acceptable input";
        });
    }

    private static void sendPng(Response response, byte[] bytes) throws IOException
    {
        response.type("image/png");
        response.raw().getOutputStream().write(bytes);
        halt(200);
    }
}