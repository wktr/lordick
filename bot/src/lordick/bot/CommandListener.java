package lordick.bot;

import lordick.Lordick;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public interface CommandListener {

    public void handleCommand(Lordick client, String command, IrcMessage message);

    public String getHelp();

    public String getCommands();

}
