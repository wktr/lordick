package lordick.util;

public class Duration {

    private static void dostuff(StringBuilder sb, long n, String s) {
        if (n > 0) {
            sb.append(n);
            sb.append(' ');
            sb.append(s);
            if (n > 1) {
                sb.append('s');
            }
            sb.append(' ');
        }
    }

    public static String getReadableDuration(long duration_ms) {
        long diffInSeconds = duration_ms / 1000;
        long seconds = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long minutes = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hours = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = diffInSeconds / 24;

        StringBuilder result = new StringBuilder();
        dostuff(result, days, "day");
        dostuff(result, hours, "hour");
        dostuff(result, minutes, "minute");
        dostuff(result, seconds, "second");
        return result.toString().trim();
    }

    public static String getReadableDuration(long from_ms, long to_ms) {
        return getReadableDuration(to_ms - from_ms);
    }

}
