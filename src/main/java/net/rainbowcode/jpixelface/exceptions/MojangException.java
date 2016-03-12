package net.rainbowcode.jpixelface.exceptions;

public class MojangException extends Exception
{
    private final String path;
    private final int code;

    public MojangException(String path, int code)
    {
        this.path = path;
        this.code = code;
    }

    public String getPath()
    {
        return path;
    }

    public int getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return "MojangException{" +
                "path='" + path + '\'' +
                ", code=" + code +
                '}';
    }
}
