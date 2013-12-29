package lordick.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Json {

    private static String json_string = "(?:,|\\{)?\"([^\"]*)\":";
    private static String json_value = "(\"[^\"]*\"|\\{[^}]*\\}|\\[[^\\]]*\\]|[^}\\],]*)";
    private static Pattern json_pattern = Pattern.compile(json_string + json_value);
    private static Pattern json_object = Pattern.compile("(\\{[^}]*\\}),?");

    public static Map<String, String> parse(String json) {
        Map<String, String> map = new HashMap<String, String>();
        parseObject(map, null, json);
        return map;
    }

    private static void parseObject(Map<String, String> map, String parent, String json) {
        Matcher json_matcher = json_pattern.matcher(json);
        while (json_matcher.find()) {
            String key = json_matcher.group(1);
            String obj = json_matcher.group(2);
            //System.out.printf("Matched: '%s' to '%s'\n", key, obj);
            if (obj.startsWith("{")) {
                parseObject(map, key, obj);
            } else if (obj.startsWith("[")) {
                String newparent;
                if (parent == null) {
                    newparent = key;
                } else {
                    newparent = parent + "." + key;
                }
                Matcher array_matcher = json_object.matcher(obj.substring(1, obj.length() - 1));
                int idx = 0;
                while (array_matcher.find()) {
                    String array_value = array_matcher.group(1);
                    parseObject(map, newparent + "." + idx, array_value);
                    idx++;
                }
            } else {
                if (obj.startsWith("\"")) {
                    obj = obj.substring(1, obj.length() - 1);
                }
                if (parent != null) {
                    key = parent + "." + key;
                }
                //System.out.printf(" '%s' = '%s'\n", key, obj);
                map.put(key, obj);
            }
        }
    }


}
