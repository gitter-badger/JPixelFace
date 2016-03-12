package net.rainbowcode.jpixelface.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.exceptions.MojangException;
import net.rainbowcode.jpixelface.profile.Profile;
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

            future.await();
            response.type("application/json");
            if (future.getException() != null && future.getException() instanceof MojangException)
            {
                MojangException mojangException = (MojangException) future.getException();
                if (mojangException.getCode() == 204) // Handle people without profile
                {
                    JsonObject json = new Profile().toJson();
                    json.add("err", new JsonPrimitive("Profile not found"));
                    return json;
                }
            }
            return future.get().toJson();
        });
    }
}
