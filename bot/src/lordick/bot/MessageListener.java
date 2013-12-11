package lordick.bot;

import lordick.Lordick;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public interface MessageListener {

    public void onMessage(Lordick client, IrcMessage message);

}
