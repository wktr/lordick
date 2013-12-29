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

    @Override
    public int hashCode() {
        return getNick().hashCode() + getIdent().hashCode() + getHost().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IrcHostmask)) {
            return false;
        } else if (super.equals(obj)) {
            return true;
        } else {
            return obj.toString().equals(this.toString());
        }
    }

    @Override
    public String toString() {
        return getNick() + "!" + getIdent() + "@" + getHost();
    }
}
