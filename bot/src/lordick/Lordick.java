package lordick;

import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class Lordick extends IrcClient {

    public void start() {
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", null, "#mopar");
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

}
