package lordick.bot;

import lordick.Lordick;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public interface UnhandledCommandListener {

    public void unhandledCommand(Lordick client, String command, IrcMessage message);

}
