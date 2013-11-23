package xxx.moparisthebest.irclib.messages.handlers;

import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.messages.IrcMessageHandler;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class Rejoin implements IrcMessageHandler {
    @Override
    public boolean shouldHandle(IrcMessage message) {
        UserProperties up = IrcClient.getUserProperties(message.getChannel());
        return (message.getType().equalsIgnoreCase("PART") && message.getHostmask().getNick().equalsIgnoreCase(up.getNickname()))
                || (message.getType().equalsIgnoreCase("KICK") && message.getDestParams().equalsIgnoreCase(up.getNickname()))
                || message.getType().equalsIgnoreCase("474");
    }

    @Override
    public void handle(final IrcMessage message) {
        new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                    if (message.getType().equalsIgnoreCase("474")) {
                        message.getChannel().writeAndFlush("JOIN " + message.getDestParams());
                    } else {
                        message.getChannel().writeAndFlush("JOIN " + message.getDestination());
                    }
                } catch (Exception ignored) {
                }
            }
        }.run();
    }
}
