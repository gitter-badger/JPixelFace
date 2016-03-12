package net.rainbowcode.jpixelface.svg;

import java.awt.image.BufferedImage;

public class SVGGenerator
{
    public static String convert(BufferedImage image)
    {
        SVGDocument document = new SVGDocument(image.getWidth(), image.getHeight());
        boolean[][] visited = new boolean[image.getWidth()][image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                if (!visited[x][y])
                {
                    //Thanks StackOverflow
                    int color = image.getRGB(x, y);

                    // Components will be in the range of 0..255:
                    int blue = color & 0xff;
                    int green = (color & 0xff00) >> 8;
                    int red = (color & 0xff0000) >> 16;
                    int alpha = (color & 0xff000000) >>> 24;

                    // Check nearby pixels to see if they are the same.
                    int width = 1;
                    int height = 1;
                    for (int x2 = x + 1; x2 < image.getWidth(); x2++)
                    {
                        for (int y2 = y; y2 < image.getHeight(); y2++)
                        {
                            if (image.getRGB(x2, y2) == color)
                            {
                                width = x2 - x + 1;
                                height = y2 - y + 1;
                                visited[x2][y2] = true;
                            }
                            else
                            {
                                break;
                            }
                        }

                        if (image.getRGB(x2, y) != color)
                        {
                            break;
                        }
                    }

                    if (alpha == 255)
                    {
                        document.addElement(new SVGRect(x, y, width, height, red, green, blue));
                    }
                    visited[x][y] = true;
                }
            }
        }

        return document.toString();
    }
}
