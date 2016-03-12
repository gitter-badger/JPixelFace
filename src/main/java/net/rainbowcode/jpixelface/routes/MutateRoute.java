package net.rainbowcode.jpixelface.routes;

import net.rainbowcode.jpixelface.Errors;
import net.rainbowcode.jpixelface.HttpServer;
import net.rainbowcode.jpixelface.exceptions.InvalidIdException;
import net.rainbowcode.jpixelface.exceptions.ScaleOutOfBoundsException;
import net.rainbowcode.jpixelface.profile.ProfileFuture;
import net.rainbowcode.jpixelface.skin.Mutate;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.halt;

public class MutateRoute extends Route
{

    private final Mutate mutate;

    public MutateRoute(Mutate mutate)
    {
        super(mutate.getPath());
        this.mutate = mutate;
    }

    @Override
    public void init(HttpServer server)
    {
        get(mutate.getPath() + ":id", (request, response) -> {
            boolean svg = false;
            if (request.params("id").endsWith(".svg"))
            {
                svg = true;
            }

            String id = request.params("id").replace(".png", "").replace(".svg", "");

            int size = 64;
            ProfileFuture future = server.getProfile(id);

            if (svg)
            {
                return server.handleSVG(response, future, mutate);
            }
            else
            {
                return server.handleImage(response, future, size, mutate).raw();
            }
        });

        get(mutate.getPath() + ":id/:size", "image/png", (request, response) -> {
            String id = request.params("id").replace(".png", "");
            int size = Integer.parseInt(request.params("size").replace(".png", ""));

            ProfileFuture future = server.getProfile(id);

            int minScale = mutate.getMinScale();
            int maxScale = mutate.getMaxScale();

            if (size > maxScale || size < minScale)
            {
                throw new ScaleOutOfBoundsException(minScale, maxScale);
            }

            Response httpServletResponse = server.handleImage(response, future, size, mutate);

            return httpServletResponse.raw();
        });
    }
}
