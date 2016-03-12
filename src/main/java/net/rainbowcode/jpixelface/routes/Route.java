package net.rainbowcode.jpixelface.routes;

import net.rainbowcode.jpixelface.HttpServer;

public abstract class Route
{
    private final String path;

    public Route(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public abstract void init(HttpServer server);
}
