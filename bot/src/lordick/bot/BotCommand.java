package lordick.bot;

import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;

public abstract class BotCommand {

    public boolean shouldHandleCommand(IrcClient client, IrcMessage message) {
        return false;
    }

    public void handleCommand(IrcClient client, IrcMessage message) {
        // empty
    }

    public void unhandledCommand(IrcClient client, IrcMessage message) {

    }

    public abstract String getHelp();

    public String[] getCommandList() {
        return null;
    }

    public String getCommand() {
        return null;
    }

    public boolean shouldHandleMessage(IrcClient client, IrcMessage message) {
        return false;
    }

    public void handleMessage(IrcClient client, IrcMessage message) {
        // empty
    }

    public void unhandledMessage(IrcClient client, IrcMessage message) {

    }

}
