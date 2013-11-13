package lordick;

import lordick.bot.BotCommand;
import lordick.bot.commands.Karma;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

public class Lordick extends IrcClient {

    public Lordick() {
        for (Class c : ClassEnumerator.getClassesForPackage(Karma.class.getPackage())) {
            try {
                BotCommand message = (BotCommand) c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        UserProperties up = new UserProperties("lordick", "lordick", "lordick", null, "#mopar");
        NetworkProperties np = new NetworkProperties("irc.moparisthebest.xxx", 6667, false);
        connect(up, np);
    }

}
