package net.rainbowcode.jpixelface.svg;

public class SVGRect
{
    private int x;
    private int y;
    private int width;
    private int height;
    private int r;
    private int g;
    private int b;

    /**
     * A Rectangle SVG element
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Element width
     * @param height Element height
     * @param r      Red value
     * @param g      Green value
     * @param b      Blue value
     */
    public SVGRect(int x, int y, int width, int height, int r, int g, int b)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString()
    {
        return "<rect x=\"" + x + "\" y=\"" + y + "\" width=\"" + width + "\" height=\"" + height + "\" style=\"fill:rgb(" + r + ", " + g + ", " + b + ")\" />";
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
