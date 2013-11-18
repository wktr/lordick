package xxx.moparisthebest.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hostmask {

    private static Pattern HOSTMASK_PATTERN = Pattern.compile(":?([^!]+)(?:[!]([^@]+))?(?:[@](\\S+))?");

    private static String get(String mask, int group) {
        Matcher m = HOSTMASK_PATTERN.matcher(mask);
        if (m.matches()) {
            return m.group(group);
        } else {
            return null;
        }
    }

    public static String getNick(String mask) {
        return get(mask, 1);
    }

    public static String getIdent(String mask) {
        return get(mask, 2);
    }

    public static String getHost(String mask) {
        return get(mask, 3);
    }

}
