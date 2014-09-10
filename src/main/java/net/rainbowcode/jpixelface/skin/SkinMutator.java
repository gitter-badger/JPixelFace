package net.rainbowcode.jpixelface.skin;

public abstract class SkinMutator implements Runnable {
    private final byte[] skin;

    protected SkinMutator(byte[] skin) {
        this.skin = skin;
    }

    public byte[] getSkin() {
        return skin;
    }
}
