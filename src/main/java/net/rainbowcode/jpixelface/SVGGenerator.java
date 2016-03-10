package net.rainbowcode.jpixelface;

import java.awt.image.BufferedImage;

public class SVGGenerator
{
    public static String convert(BufferedImage image)
    {
        String retVal = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + image.getWidth() + "\" height=\"" + image.getHeight() + "\">";
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

                    int sameY = 1;

                    while (y + sameY < image.getHeight() && image.getRGB(x, y + sameY) == color)
                    {
                        sameY++;
                        visited[x][y + sameY - 1] = true;
                    }

                    int sameX = 1;

                    while (x + sameX < image.getWidth() && image.getRGB(x + sameX, y) == color)
                    {
                        sameX++;
                        visited[x + sameX - 1][y] = true;
                    }

                    retVal += "<rect x=\"" + x + "\" y=\"" + y + "\" width=\"" + sameX + "\" height=\"" + sameY + "\" style=\"fill:rgb(" + red + ", " + green + ", " + blue + ")\" />";
                }
            }
        }

        return retVal + "</svg>";
    }
}
