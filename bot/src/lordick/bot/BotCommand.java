package lordick.bot;

import io.netty.channel.Channel;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;

public abstract class BotCommand {

    public abstract boolean shouldHandleCommand(IrcClient client, Channel channel, IrcChat chat);

    public abstract void handleCommand(IrcClient client, Channel channel, IrcChat chat);

    public abstract String getHelp();

    public abstract String[] getCommandList();

    public boolean shouldHandleMessage(IrcClient client, Channel channel, IrcChat chat) {
        return false;
    }

    public void handleMessage(IrcClient client, Channel channel, IrcChat chat) {
        // empty
    }

}
