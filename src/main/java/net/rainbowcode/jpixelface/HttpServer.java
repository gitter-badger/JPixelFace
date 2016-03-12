package net.rainbowcode.jpixelface;

import net.rainbowcode.jpixelface.exceptions.InvalidIdException;
import net.rainbowcode.jpixelface.exceptions.ScaleOutOfBoundsException;
import net.rainbowcode.jpixelface.profile.ProfileFuture;
import net.rainbowcode.jpixelface.profile.ProfileRequestThread;
import net.rainbowcode.jpixelface.redis.RedisUtils;
import net.rainbowcode.jpixelface.routes.MutateRoute;
import net.rainbowcode.jpixelface.routes.ProfileRoute;
import net.rainbowcode.jpixelface.skin.Mutate;
import net.rainbowcode.jpixelface.skin.SkinManager;
import net.rainbowcode.jpixelface.svg.SVGGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public SkinManager skinManager = new SkinManager();
    public ProfileRequestThread requestThread = new ProfileRequestThread();
    private Logger log = LogManager.getLogger("HttpServer");

    public HttpServer()
    {
        log.info("Starting profile request thread.");
        requestThread.start();
    }

    public void init()
    {
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
            MutateRoute mutateRoute = new MutateRoute(mutate);
            mutateRoute.init(this);
            log.info("Initialised route for mutator: " + mutate.name());
        }

        new ProfileRoute().init(this);
        log.info("Initialised profile route");

        exception(NumberFormatException.class, (e, request, response) -> {
            response.status(Errors.NUMBER_FORMAT_EXCEPTION.getCode());
            response.body(Errors.NUMBER_FORMAT_EXCEPTION.getText());
        });

        exception(InvalidIdException.class, (e, request, response) -> {
            response.status(Errors.ID_NOT_VALID.getCode());
            response.body(Errors.ID_NOT_VALID.getText());
        });

        exception(ScaleOutOfBoundsException.class, (e, request, response) -> {
            ScaleOutOfBoundsException exception = (ScaleOutOfBoundsException) e;
            response.status(Errors.SIZE_TOO_BIG_OR_TOO_SMALL.getCode());
            response.body(String.format(Errors.SIZE_TOO_BIG_OR_TOO_SMALL.getText(), exception.getMinScale(), exception.getMaxScale()));
        });
    }

    public static void main(String[] args) throws Exception
    {
        tickCounter.start();
        HttpServer server = new HttpServer();
        server.init();
    }

    public ProfileFuture getProfile(String id) throws InvalidIdException
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

        if (future != null)
        {
            return future;
        }

        throw new InvalidIdException();
    }

    public String handleSVG(Response response, ProfileFuture future, Mutate mutate) throws InterruptedException, ExecutionException
    {
        while (!future.isDone())
        {
            Thread.sleep(1);
        }
        response.type("image/svg+xml");
        return SVGGenerator.convert(skinManager.getBufferedMutated(future.get(), mutate.getSvgScale(), mutate));
    }

    public Response handleImage(Response response, ProfileFuture profile, int size, Mutate mutate) throws IOException, InterruptedException, ExecutionException, InvalidIdException
    {

        if (profile == null)
        {
            throw new InvalidIdException();
        }

        response.type("image/png");
        HttpServletResponse raw = response.raw();

        while (!profile.isDone())
        {
            Thread.sleep(1);
        }

        raw.getOutputStream().write(skinManager.getMutated(profile.get(), size, mutate));
        raw.getOutputStream().flush();
        raw.getOutputStream().close();
        return response;
    }
}