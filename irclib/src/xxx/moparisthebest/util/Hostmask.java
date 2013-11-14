package xxx.moparisthebest.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hostmask {

    private static Pattern HOSTMASK_PATTERN = Pattern.compile(":?(?<nick>[^!]+)(?:[!](?<ident>[^@]+))?(?:[@](?<host>\\S+))?");

    private static String get(String mask, String group) {
        Matcher m = HOSTMASK_PATTERN.matcher(mask);
        if (m.matches()) {
            return m.group(group);
        } else {
            return null;
        }
    }

    public static String getNick(String mask) {
        return get(mask, "nick");
    }

    public static String getIdent(String mask) {
        return get(mask, "ident");
    }

    public static String getHost(String mask) {
        return get(mask, "host");
    }

}
