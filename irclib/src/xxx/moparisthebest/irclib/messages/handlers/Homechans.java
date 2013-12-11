package xxx.moparisthebest.irclib.messages.handlers;

import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.messages.IrcMessageHandler;

@SuppressWarnings("unused")
public class Homechans implements IrcMessageHandler {

    @Override
    public boolean shouldHandle(IrcMessage message) {
        return message.getCommand().equalsIgnoreCase("001");
    }

    @Override
    public void handle(IrcMessage message) {
        for (String s : message.getServer().getUserProperties().getHomeChannels()) {
            message.getServer().getChannel().write("JOIN " + s);
        }
        message.getServer().getChannel().flush();
    }
}
