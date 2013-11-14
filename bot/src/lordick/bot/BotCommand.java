package lordick.bot;

import io.netty.channel.Channel;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

public abstract class BotCommand {

    public boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat) {
        return false;
    }

    public void handleCommand(IrcClient client, Channel channel, IrcChat chat) {
        // empty
    }

    public void unhandledCommand(IrcClient client, Channel channel, IrcChat chat) {

    }

    public abstract String getHelp();

    public String[] getCommandList() {
        return null;
    }

    public String getCommand() {
        return null;
    }

    public boolean shouldHandleMessage(IrcClient client, Channel channel, IrcChat chat) {
        return false;
    }

    public void handleMessage(IrcClient client, Channel channel, IrcChat chat) {
        // empty
    }

    public void unhandledMessage(IrcClient client, Channel channel, IrcChat chat) {

    }

}
