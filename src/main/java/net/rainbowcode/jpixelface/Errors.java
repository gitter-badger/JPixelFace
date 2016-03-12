package net.rainbowcode.jpixelface;

public enum Errors
{
    ID_NOT_VALID("Not acceptable input (Not a valid Minecraft name, Mojang UUID or real UUID)", 400),
    NUMBER_FORMAT_EXCEPTION("Not acceptable input: Size input is not a number", 400),
    SIZE_TOO_BIG_OR_TOO_SMALL("Not acceptable input: Scale out of bounds. For this route, it should be between %d and %d", 400);

    private final String text;
    private final int code;

    Errors(String text, int code)
    {
        this.text = text;
        this.code = code;
    }

    public String getText()
    {
        return text;
    }

    public int getCode()
    {
        return code;
    }
}
