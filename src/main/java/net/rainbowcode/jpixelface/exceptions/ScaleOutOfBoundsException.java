package net.rainbowcode.jpixelface.exceptions;

public class ScaleOutOfBoundsException extends Exception
{
    private final int minScale;
    private final int maxScale;

    public ScaleOutOfBoundsException(int minScale, int maxScale)
    {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    public int getMinScale()
    {
        return minScale;
    }

    public int getMaxScale()
    {
        return maxScale;
    }
}
