package xxx.moparisthebest.util;

public class StringUtil {

    public static String join(String delim, String... strings) {
        if (strings.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(strings[i]);
            }
        }
        return sb.toString();
    }

    public static String between(String str, String start, String end) {
        int s = str.indexOf(start) + start.length();
        int e = str.indexOf(end, s);
        return str.substring(s, e);
    }

}
