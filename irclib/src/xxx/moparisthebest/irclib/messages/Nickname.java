package xxx.moparisthebest.irclib.messages;

import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.IrcMessageHandler;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.Random;

public class Nickname implements IrcMessageHandler {
    @Override
    public boolean shouldHandle(IrcMessage message) {
        return message.getType().equalsIgnoreCase("NICK") || message.getType().equalsIgnoreCase("433") || message.getType().equalsIgnoreCase("396");
    }

    @Override
    public void handle(IrcMessage message) {
        UserProperties up = IrcClient.getUserProperties(message.getChannel());
        String nick = message.getHostmask().getNick();
        if (message.getType().equalsIgnoreCase("NICK")) { // if we change (or someone else) changes our nickname, keep track
            if (nick.equals(up.getNickname())) {
                up.setNickname(nick);
            }
        } else if (message.getType().equalsIgnoreCase("433")) { // if our nick is in use
            nick = up.getAltnick() + new Random().nextInt(1000);
            message.getChannel().writeAndFlush("NICK " + nick);
            up.setNickname(nick);
        } else { // update host
            up.setHost(message.getDestination());
        }
    }
}
