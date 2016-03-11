package net.rainbowcode.jpixelface;

import net.rainbowcode.jpixelface.profile.ProfileFuture;
import net.rainbowcode.jpixelface.profile.ProfileRequestThread;
import net.rainbowcode.jpixelface.skin.Mutate;
import net.rainbowcode.jpixelface.svg.SVGGenerator;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
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
    public static ProfileRequestThread requestThread = new ProfileRequestThread();

    public static void main(String[] args) throws Exception
    {
        tickCounter.start();
        requestThread.start();
        staticFileLocation("/public");
        port(PORT);

        before((request1, response1) -> {
            response1.header("X-host", System.getenv("HOSTNAME"));
            String key = "cache:" + request1.uri();
            response1.header("Cache-Control", "public, max-age=86400");
            ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.of("GMT"));
            String oldAge = now.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz"));
            boolean shouldHalt = false;
            if (RedisUtils.exists(key))
            {
                oldAge = RedisUtils.getAsString(key);
                String modified = request1.headers("If-Modified-Since");
                if (modified != null && modified.equals(oldAge))
                {
                    shouldHalt = true;
                }
            }
            else
            {
                RedisUtils.setAndExpire(key, oldAge, 86400);
            }
            response1.header("Last-Modified", oldAge);
            if (shouldHalt)
            {
                halt(304);
            }
        });

        for (Mutate mutate : Mutate.values())
        {
            get(mutate.getPath() + ":id", (request, response) -> {
                boolean svg = false;
                if (request.params("id").endsWith(".svg"))
                {
                    svg = true;
                }

                String id = request.params("id").replace(".png", "").replace(".svg", "");

                int size = svg ? 8 : 64;

                if (svg)
                {
                    ProfileFuture future = getProfile(id);
                    if (future == null)
                    {
                        halt(403, "Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)");
                        return "";
                    }
                    else
                    {
                        return handleSVG(response, future, mutate);
                    }
                }
                else
                {
                    Response httpServletResponse = handleImage(response, id, size, mutate);
                    if (httpServletResponse != null)
                    {
                        return httpServletResponse.raw();
                    }
                }

                halt(403, "Not acceptable input");
                return "Not acceptable input";
            });

            get(mutate.getPath() + ":id/:size", "image/png", (request, response) -> {
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

                int minScale = mutate.getMinScale();
                int maxScale = mutate.getMaxScale();

                if (size > maxScale || size < minScale)
                {
                    halt(403, "Not acceptable input: Scale out of bounds (" + minScale + " - " + maxScale + ")");
                    return "Not acceptable input: Scale out of bounds (" + minScale + " - " + maxScale + ")";
                }

                Response httpServletResponse = handleImage(response, id, size, mutate);

                if (httpServletResponse != null)
                {
                    return httpServletResponse.raw();
                }

                halt(403, "Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)");
                return "Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)";
            });
        }


        get("/profile/:id", "application/json", (request, response) -> {
            String id = request.params("id").replace(".json", "");
            ProfileFuture future = getProfile(id);

            if (future != null)
            {
                while (!future.isDone())
                {
                    Thread.sleep(1);
                }
                return future.get().toJson();
            }

            halt(403, "Not acceptable input");
            return "Not acceptable input";
        });
    }

    private static ProfileFuture getProfile(String id)
    {
        ProfileFuture future = null;

        if (NAME.matcher(id).find())
        {
            future = requestThread.getProfileByName(id);
        }
        else if (UUID_PATTERN.matcher(id).find())
        {
            future = requestThread.getProfileByMojangID(id);
        }
        else if (REAL_UUID_PATTERN.matcher(id).find())
        {
            future = requestThread.getProfileByUUID(id);
        }

        return future;
    }

    private static String handleSVG(Response response, ProfileFuture future, Mutate mutate) throws InterruptedException, ExecutionException
    {
        while (!future.isDone())
        {
            Thread.sleep(1);
        }
        response.type("image/svg+xml");
//        response.header("Content-Encoding", "gzip");
        return SVGGenerator.convert(skinManager.getBufferedMutated(future.get(), mutate.getSvgScale(), mutate));
    }

    private static Response handleImage(Response response, String id, int size, Mutate mutate) throws IOException, InterruptedException, ExecutionException
    {

        ProfileFuture future;
        if (NAME.matcher(id).find())
        {
            future = requestThread.getProfileByName(id);
        }
        else if (UUID_PATTERN.matcher(id).find())
        {
            future = requestThread.getProfileByMojangID(id);
        }
        else if (REAL_UUID_PATTERN.matcher(id).find())
        {
            future = requestThread.getProfileByUUID(id);
        }
        else
        {
            response.status(403);
            response.body("Not a username, uuid or Mojang id.");
            return response;
        }
        response.type("image/png");
        HttpServletResponse raw = response.raw();
        while (!future.isDone())
        {
            Thread.sleep(1);
        }
        raw.getOutputStream().write(skinManager.getMutated(future.get(), size, mutate));
        raw.getOutputStream().flush();
        raw.getOutputStream().close();
        return response;
    }
}