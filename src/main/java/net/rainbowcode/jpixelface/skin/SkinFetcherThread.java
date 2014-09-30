package net.rainbowcode.jpixelface.skin;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.rainbowcode.jpixelface.HttpUtil;
import net.rainbowcode.jpixelface.TimedConcurrentCache;
import net.rainbowcode.jpixelface.profile.Profile;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SkinFetcherThread extends Thread {
    public ConcurrentLinkedQueue<SkinFetchJob> queue = new ConcurrentLinkedQueue<>();
    private TimedConcurrentCache<UUID, byte[]> skinCache = new TimedConcurrentCache<>(86400000L);
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void run() {
        while (true) {
            while (queue.isEmpty()) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            SkinFetchJob pop = queue.poll();

            try {
                boolean found = false;
                byte[] skin;
                Profile profile = pop.getProfile();
                if (profile.getUuid() == null) {
                    skin = IOUtils.toByteArray(new FileInputStream(new File("char.png")));
                } else {
                    byte[] cache = skinCache.get(profile.getUuid());
                    if (cache == null) {
                        if (profile.getSkinUrl() != null){
                            skin = HttpUtil.getAsBytes(profile.getSkinUrl());
                        } else {
                            skin = IOUtils.toByteArray(new FileInputStream(new File("char.png")));
                        }
                    } else {
                        skin = cache;
                        found = true;
                    }

                }

                if (!found && profile.getUuid() != null) {
                    skinCache.put(profile.getUuid(), skin);
                }

                executor.execute(new SkinMutator(skin) {
                    @Override
                    public void run() {
                        try {
                            byte[] newSkin = getSkin();
                            if (!pop.getMutate().equals(Mutate.NONE)) {
                                InputStream in = new ByteArrayInputStream(newSkin);
                                BufferedImage bImageFromConvert = ImageIO.read(in);
                                in.close();

                                if (pop.getMutate().equals(Mutate.AVATAR)) {
                                    bImageFromConvert = Scalr.crop(bImageFromConvert, 8, 8, 8, 8);
                                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, pop.getSize());
                                } else if (pop.getMutate().equals(Mutate.HELM)) {
                                    bImageFromConvert = getHelm(bImageFromConvert);
                                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, pop.getSize());
                                } else if (pop.getMutate().equals(Mutate.BODY) || pop.getMutate().equals(Mutate.BODY_NOLAYER)) {
                                    bImageFromConvert = getBody(bImageFromConvert, pop.getMutate().equals(Mutate.BODY_NOLAYER));
                                    bImageFromConvert = Scalr.resize(bImageFromConvert, Scalr.Method.SPEED, 18 * pop.getSize(), 32 * pop.getSize());
                                } else if (pop.getMutate().equals(Mutate.TORSO) || pop.getMutate().equals(Mutate.TORSO_NOLAYER)) {
                                    BufferedImage body = getBody(bImageFromConvert, pop.getMutate().equals(Mutate.TORSO_NOLAYER));
                                    body = Scalr.crop(body, 1, 0, 17, 20);
                                    bImageFromConvert = Scalr.resize(body, Scalr.Method.SPEED, 18 * pop.getSize(), 20 * pop.getSize());
                                    body.flush();
                                } else if (pop.getMutate().equals(Mutate.BUST) || pop.getMutate().equals(Mutate.BUST_NOLAYER)) {
                                    BufferedImage body = getBody(bImageFromConvert, pop.getMutate().equals(Mutate.BUST_NOLAYER));
                                    body = Scalr.crop(body, 1, 0, 16, 16);
                                    bImageFromConvert = Scalr.resize(body, Scalr.Method.SPEED, pop.getSize());
                                    body.flush();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(bImageFromConvert, "png", baos);
                                baos.flush();
                                newSkin = baos.toByteArray();
                                baos.close();
                                bImageFromConvert.flush();
                            }

                            ByteBuf buf = pop.getCtx().alloc().directBuffer(newSkin.length + 1);
                            buf.writeBytes(newSkin);
                            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);
                            httpResponse.headers().set(CONTENT_TYPE, "image/png");
                            httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
                            pop.getCtx().writeAndFlush(httpResponse);
                        } catch (Exception e) {
                            HttpUtil.sendError(pop.getCtx(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                            e.printStackTrace();
                        }
                    }
                });


                if (!found) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public BufferedImage getHelm(BufferedImage bufferedImage) {
        BufferedImage combined = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        BufferedImage head = Scalr.crop(bufferedImage, 8, 8, 8, 8);
        BufferedImage overlay = Scalr.crop(bufferedImage, 40, 8, 8, 8);
        Graphics graphics = combined.getGraphics();
        graphics.drawImage(head, 0, 0, null);
        graphics.drawImage(overlay, 0, 0, null);
        graphics.dispose();
        return combined;
    }

    public BufferedImage getBody(BufferedImage bufferedImage, boolean naked) {
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
        if (bufferedImage.getHeight() == 64 && bufferedImage.getWidth() == 64) {
            BufferedImage rightArm2 = Scalr.crop(bufferedImage, 44, 36, 4, 12);
            BufferedImage leftArm2 = Scalr.crop(bufferedImage, 52, 52, 4, 12);
            BufferedImage chest2 = Scalr.crop(bufferedImage, 20, 36, 8, 12);
            BufferedImage rightLeg2 = Scalr.crop(bufferedImage, 4, 36, 4, 12);
            BufferedImage leftLeg2 = Scalr.crop(bufferedImage, 4, 52, 4, 12);
            leftArm = Scalr.crop(bufferedImage, 36, 52, 4, 12);
            leftLeg = Scalr.crop(bufferedImage, 20, 52, 4, 12);
            graphics.drawImage(leftArm, 1, 8, null);
            graphics.drawImage(leftLeg, 9, 20, null);

            if (!naked) {
                graphics.drawImage(rightArm2, 13, 8, null);
                graphics.drawImage(leftArm2, 1, 8, null);
                graphics.drawImage(chest2, 5, 8, null);
                graphics.drawImage(rightLeg2, 5, 20, null);
                graphics.drawImage(leftLeg2, 9, 20, null);
            }
        } else {
            leftArm = Scalr.rotate(rightArm, Scalr.Rotation.FLIP_HORZ);
            leftLeg = Scalr.rotate(rightLeg, Scalr.Rotation.FLIP_HORZ);
            graphics.drawImage(leftArm, 1, 8, null);
            graphics.drawImage(leftLeg, 9, 20, null);
        }


        graphics.dispose();
        return combined;
    }

}
