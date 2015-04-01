package net.rainbowcode.jpixelface;

import net.rainbowcode.jpixelface.profile.Profile;
import net.rainbowcode.jpixelface.skin.Mutate;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SkinManager
{
    private final Logger logger = LogManager.getLogger();
    private byte[] defaultSkin;

    public SkinManager()
    {
        try
        {
            defaultSkin = IOUtils.toByteArray(getClass().getResourceAsStream("/char.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            logger.fatal("Unable to load char.png! Shutting down!");
            System.exit(1);
        }
    }

    public byte[] getSkinFromProfile(Profile profile)
    {
        byte[] skin = null;
        try
        {
            if (profile.getUuid() == null)
            {
                skin = defaultSkin;
            }
            else
            {
                if (profile.getSkinUrl() != null)
                {
                    byte[] key = RedisKey.SKIN.buildByteKey(profile.getUuid().toString());
                    if (RedisUtils.exists(key))
                    {
                        skin = RedisUtils.getAsBytes(key);
                    }
                    else
                    {
                        skin = HttpUtil.getAsBytes(profile.getSkinUrl());
                        RedisUtils.setAndExpire(key, skin, 86400);
                    }

                }
                else
                {
                    skin = defaultSkin;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return skin;
    }

    public byte[] getMutated(Profile profile, int size, Mutate mutate)
    {
        byte[] newSkin;
        try
        {
            newSkin = getSkinFromProfile(profile);
            if (!mutate.equals(Mutate.NONE))
            {
                InputStream in = new ByteArrayInputStream(newSkin);
                BufferedImage bImageFromConvert = ImageIO.read(in);
                in.close();


                if (mutate.equals(Mutate.AVATAR))
                {
                    bImageFromConvert = Scalr.crop(bImageFromConvert, 8, 8, 8, 8);
                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, size);
                }
                else if (mutate.equals(Mutate.HELM))
                {
                    bImageFromConvert = getHelm(bImageFromConvert);
                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, size);
                }
                else if (mutate.equals(Mutate.BODY) || mutate.equals(Mutate.BODY_NOLAYER))
                {
                    bImageFromConvert = getBody(bImageFromConvert, mutate.equals(Mutate.BODY_NOLAYER));
                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, 18 * size, 32 * size);
                }
                else if (mutate.equals(Mutate.TORSO) || mutate.equals(Mutate.TORSO_NOLAYER))
                {
                    BufferedImage body = getBody(bImageFromConvert, mutate.equals(Mutate.TORSO_NOLAYER));
                    body = Scalr.crop(body, 1, 0, 17, 20);
                    bImageFromConvert = Scalr.resize(body, Scalr.Method.SPEED, 18 * size, 20 * size);
                    body.flush();
                }
                else if (mutate.equals(Mutate.BUST) || mutate.equals(Mutate.BUST_NOLAYER))
                {
                    BufferedImage body = getBody(bImageFromConvert, mutate.equals(Mutate.BUST_NOLAYER));
                    body = Scalr.crop(body, 1, 0, 16, 16);
                    bImageFromConvert = Scalr.resize(body, Scalr.Method.SPEED, size);
                    body.flush();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bImageFromConvert, "png", baos);
                baos.flush();
                newSkin = baos.toByteArray();
                baos.close();
                bImageFromConvert.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return newSkin;
    }


    public BufferedImage getHelm(BufferedImage bufferedImage)
    {
        BufferedImage combined = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        BufferedImage head = Scalr.crop(bufferedImage, 8, 8, 8, 8);
        BufferedImage overlay = Scalr.crop(bufferedImage, 40, 8, 8, 8);

        //Iterate over pixels in stuff and replace white and black to be transparent to match minecraft rendering.
        // This is only to support dumb users using MS paint.
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                int rgb = overlay.getRGB(x, y);
                if (rgb == -1)
                {
                    overlay.setRGB(x, y, Color.TRANSLUCENT);
                }
            }
        }

        Graphics graphics = combined.getGraphics();
        graphics.drawImage(head, 0, 0, null);
        graphics.drawImage(overlay, 0, 0, null);
        graphics.dispose();
        return combined;
    }

    public BufferedImage getBody(BufferedImage bufferedImage, boolean naked)
    {
        BufferedImage combined = new BufferedImage(18, 32, BufferedImage.TYPE_INT_ARGB);
        BufferedImage head = getHelm(bufferedImage);
        BufferedImage rightArm = Scalr.crop(bufferedImage, 44, 20, 4, 12);
        BufferedImage leftArm;
        BufferedImage chest = Scalr.crop(bufferedImage, 20, 20, 8, 12);
        BufferedImage rightLeg = Scalr.crop(bufferedImage, 4, 20, 4, 12);
        BufferedImage leftLeg;
        Graphics graphics = combined.getGraphics();
        graphics.drawImage(head, 5, 0, null);
        graphics.drawImage(rightArm, 13, 8, null);
        graphics.drawImage(chest, 5, 8, null);
        graphics.drawImage(rightLeg, 5, 20, null);
        if (bufferedImage.getHeight() == 64 && bufferedImage.getWidth() == 64)
        {
            BufferedImage rightArm2 = Scalr.crop(bufferedImage, 44, 36, 4, 12);
            BufferedImage leftArm2 = Scalr.crop(bufferedImage, 52, 52, 4, 12);
            BufferedImage chest2 = Scalr.crop(bufferedImage, 20, 36, 8, 12);
            BufferedImage rightLeg2 = Scalr.crop(bufferedImage, 4, 36, 4, 12);
            BufferedImage leftLeg2 = Scalr.crop(bufferedImage, 4, 52, 4, 12);
            leftArm = Scalr.crop(bufferedImage, 36, 52, 4, 12);
            leftLeg = Scalr.crop(bufferedImage, 20, 52, 4, 12);
            graphics.drawImage(leftArm, 1, 8, null);
            graphics.drawImage(leftLeg, 9, 20, null);

            if (!naked)
            {
                graphics.drawImage(rightArm2, 13, 8, null);
                graphics.drawImage(leftArm2, 1, 8, null);
                graphics.drawImage(chest2, 5, 8, null);
                graphics.drawImage(rightLeg2, 5, 20, null);
                graphics.drawImage(leftLeg2, 9, 20, null);
            }
        }
        else
        {
            leftArm = Scalr.rotate(rightArm, Scalr.Rotation.FLIP_HORZ);
            leftLeg = Scalr.rotate(rightLeg, Scalr.Rotation.FLIP_HORZ);
            graphics.drawImage(leftArm, 1, 8, null);
            graphics.drawImage(leftLeg, 9, 20, null);
        }


        graphics.dispose();
        return combined;
    }
}
