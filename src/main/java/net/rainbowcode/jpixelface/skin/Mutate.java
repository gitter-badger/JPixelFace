package net.rainbowcode.jpixelface.skin;

public enum Mutate
{
    NONE("/skin/"),
    HELM("/helm/"),
    BODY("/body/"),
    BODY_NOLAYER("/body-nolayer/"),
    TORSO("/torso/"),
    TORSO_NOLAYER("/torso-nolayer/"),
    BUST("/bust/"),
    BUST_NOLAYER("/bust-nolayer/"),
    AVATAR("/avatar/");

    private final String path;

    Mutate(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
