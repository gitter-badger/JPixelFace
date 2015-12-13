package net.rainbowcode.jpixelface.skin;

import net.rainbowcode.jpixelface.SkinManager;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;

public enum Mutate
{
    NONE("/skin/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            return bufferedImage;
        }
    },
    HELM("/helm/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            bufferedImage = SkinManager.getHelm(bufferedImage);
            bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, scale);
            return bufferedImage;
        }
    },
    BODY("/body/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            bufferedImage = SkinManager.getBody(bufferedImage, false);
            bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, 18 * scale, 32 * scale);
            return bufferedImage;
        }
    },
    BODY_NOLAYER("/body-nolayer/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            bufferedImage = SkinManager.getBody(bufferedImage, true);
            bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, 18 * scale, 32 * scale);
            return bufferedImage;
        }
    },
    TORSO("/torso/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            BufferedImage body = SkinManager.getBody(bufferedImage, false);
            body = Scalr.crop(body, 1, 0, 16, 20);
            bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, 18 * scale, 20 * scale);
            body.flush();
            return bufferedImage;
        }
    },
    TORSO_NOLAYER("/torso-nolayer/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            BufferedImage body = SkinManager.getBody(bufferedImage, true);
            body = Scalr.crop(body, 1, 0, 16, 20);
            bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, 18 * scale, 20 * scale);
            body.flush();
            return bufferedImage;
        }
    },
    BUST("/bust/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            BufferedImage body = SkinManager.getBody(bufferedImage, false);
            body = Scalr.crop(body, 1, 0, 16, 16);
            bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, scale);
            body.flush();
            return bufferedImage;
        }
    },
    BUST_NOLAYER("/bust-nolayer/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            BufferedImage body = SkinManager.getBody(bufferedImage, true);
            body = Scalr.crop(body, 1, 0, 16, 16);
            bufferedImage = Scalr.resize(body, Scalr.Method.SPEED, scale);
            body.flush();
            return bufferedImage;
        }
    },
    AVATAR("/avatar/") {
        @Override
        public BufferedImage act(BufferedImage bufferedImage, int scale) {
            bufferedImage = Scalr.crop(bufferedImage, 8, 8, 8, 8);
            bufferedImage = Scalr.resize(bufferedImage, Scalr.Method.SPEED, scale);
            return bufferedImage;
        }
    };

    private final String path;

    Mutate(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
    public abstract BufferedImage act(BufferedImage bufferedImage, int scale);
}
