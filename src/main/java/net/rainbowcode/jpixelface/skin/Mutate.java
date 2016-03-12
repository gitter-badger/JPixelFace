package net.rainbowcode.jpixelface.skin;

import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public enum Mutate
{
    NONE("/skin/", 8, 512)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    return bufferedImage;
                }
            },
    HELM("/helm/", 8, 512)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    bufferedImage = SkinManager.getHelm(bufferedImage);
                    bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, scale);
                    return bufferedImage;
                }
            },
    BODY("/body/", 1, 128)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    bufferedImage = SkinManager.getBody(bufferedImage, false);
                    bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, 18 * scale, 32 * scale);
                    return bufferedImage;
                }
            },
    BODY_NOLAYER("/body-nolayer/", 1, 128)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    bufferedImage = SkinManager.getBody(bufferedImage, true);
                    bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, 18 * scale, 32 * scale);
                    return bufferedImage;
                }
            },
    TORSO("/torso/", 1, 128)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    BufferedImage body = SkinManager.getBody(bufferedImage, false);
                    body = Scalr.crop(body, 1, 0, 16, 20);
                    bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, 18 * scale, 20 * scale);
                    body.flush();
                    return bufferedImage;
                }
            },
    TORSO_NOLAYER("/torso-nolayer/", 1, 128)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    BufferedImage body = SkinManager.getBody(bufferedImage, true);
                    body = Scalr.crop(body, 1, 0, 16, 20);
                    bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, 18 * scale, 20 * scale);
                    body.flush();
                    return bufferedImage;
                }
            },
    BUST("/bust/", 8, 512, 16)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    BufferedImage body = SkinManager.getBody(bufferedImage, false);
                    body = Scalr.crop(body, 1, 0, 16, 16);
                    bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, scale);
                    body.flush();
                    return bufferedImage;
                }
            },
    BUST_NOLAYER("/bust-nolayer/", 8, 512, 16)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    BufferedImage body = SkinManager.getBody(bufferedImage, true);
                    body = Scalr.crop(body, 1, 0, 16, 16);
                    bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, scale);
                    body.flush();
                    return bufferedImage;
                }
            },
    AVATAR("/avatar/", 8, 512)
            {
                @Override
                public BufferedImage act(BufferedImage bufferedImage, int scale)
                {
                    bufferedImage = Scalr.crop(bufferedImage, 8, 8, 8, 8);
                    bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, scale);
                    return bufferedImage;
                }
            };

    private final String path;
    private final int minScale;
    private final int maxScale;
    private final int svgScale;

    Mutate(String path, int minScale, int maxScale, int svgScale)
    {
        this.path = path;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.svgScale = svgScale;
    }

    Mutate(String path, int minScale, int maxScale)
    {
        this.path = path;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.svgScale = minScale;
    }

    public String getPath()
    {
        return path;
    }

    public int getMinScale()
    {
        return minScale;
    }

    public int getMaxScale()
    {
        return maxScale;
    }

    public abstract BufferedImage act(BufferedImage bufferedImage, int scale);

    public int getSvgScale()
    {
        return svgScale;
    }
}
