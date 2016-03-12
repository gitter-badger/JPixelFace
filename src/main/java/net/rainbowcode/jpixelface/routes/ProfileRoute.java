package net.rainbowcode.jpixelface.routes;

import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.profile.ProfileFuture;

import static spark.Spark.get;

public class ProfileRoute extends Route
{
    public ProfileRoute()
    {
        super("/profile/:id");
    }

    @Override
    public void init(HttpServer server)
    {
        get(getPath(), (request, response) -> {
            String id = request.params("id").replace(".json", "");
            ProfileFuture future = server.getProfile(id);

            while (!future.isDone())
            {
                Thread.sleep(1);
            }
            response.type("application/json");
            return future.get().toJson();
        });
    }
}
