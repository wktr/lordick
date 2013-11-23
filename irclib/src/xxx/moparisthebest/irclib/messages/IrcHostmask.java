package xxx.moparisthebest.irclib.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcHostmask {

    private final String nick, ident, host;

    private static Pattern HOSTMASK_PATTERN = Pattern.compile(":?([^!]+)(?:[!]([^@]+))?(?:[@](\\S+))?");

    public IrcHostmask(String prefix) {
        if (prefix == null) {
            nick = ident = host = null;
            return;
        }
        Matcher m = HOSTMASK_PATTERN.matcher(prefix);
        if (m.find()) {
            nick = m.group(1);
            ident = m.group(2);
            host = m.group(3);
        } else {
            nick = prefix;
            ident = host = null;
        }
    }

    public String getNick() {
        return nick;
    }

    public String getIdent() {
        return ident;
    }

    public String getHost() {
        return host;
    }
}
