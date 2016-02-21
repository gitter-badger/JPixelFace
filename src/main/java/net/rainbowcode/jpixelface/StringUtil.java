package net.rainbowcode.jpixelface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
    //Stolen from https://github.com/sk89q/SquirrelID
    private static final Pattern MOJANG_ID_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");
    public static String addDashes(String uuid) {
        uuid = uuid.replace("-", "");
        Matcher matcher = MOJANG_ID_PATTERN.matcher(uuid);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid UUID format");
        } else {
            return matcher.replaceAll("$1-$2-$3-$4-$5");
        }
    }
}
