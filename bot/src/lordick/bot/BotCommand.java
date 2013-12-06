package lordick.bot;

import lordick.Lordick;
import xxx.moparisthebest.irclib.messages.IrcMessage;

public abstract class BotCommand {

    public void handleCommand(Lordick client, String command, IrcMessage message) {
        // empty
    }

    public void unhandledCommand(Lordick client, String command, IrcMessage message) {
        // empty
    }

    public String getHelp() {
        return null;
    }

    public String[] getCommandList() {
        return null;
    }

    public String getCommand() {
        return null;
    }

    public void onMessage(Lordick client, IrcMessage message) {
        // empty
    }
}
