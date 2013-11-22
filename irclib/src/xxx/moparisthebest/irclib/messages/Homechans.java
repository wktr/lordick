package xxx.moparisthebest.irclib.messages;

import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.IrcMessageHandler;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class Homechans implements IrcMessageHandler {

    @Override
    public boolean shouldHandle(IrcMessage message) {
        return message.getType().equalsIgnoreCase("001");
    }

    @Override
    public void handle(IrcMessage message) {
        UserProperties up = IrcClient.getUserProperties(message.getChannel());
        for (String s : up.getHomeChannels()) {
            message.getChannel().write("JOIN " + s);
        }
        message.getChannel().flush();
    }
}
