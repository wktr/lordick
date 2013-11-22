package xxx.moparisthebest.irclib.messages;

import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.IrcMessageHandler;

public class Ping implements IrcMessageHandler {
    @Override
    public boolean shouldHandle(IrcMessage message) {
        return message.getType().equalsIgnoreCase("PING");
    }

    @Override
    public void handle(IrcMessage message) {
        message.getChannel().writeAndFlush("PONG :" + message.getMessage());
    }
}
