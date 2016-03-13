package net.rainbowcode.jpixelface.svg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SVGDocument
{
    private final String header;
    private final String footer = "</svg>";

    private final ArrayList<SVGRect> elements = new ArrayList<>();

    public SVGDocument(int width, int height)
    {
        this.header = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">";
    }

    public void addElement(SVGRect rect)
    {
        elements.add(rect);
    }

    @Override
    public String toString()
    {
        String retval = header;
        retval += elements.stream()
                .map(SVGRect::toString)
                .collect(Collectors.joining());
        retval += footer;
        return retval;
    }
}
