package xxx.moparisthebest.irclib.messages.handlers;

import io.netty.channel.Channel;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.messages.IrcMessageHandler;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Rejoin implements IrcMessageHandler {
    @Override
    public boolean shouldHandle(IrcMessage message) {
        if (message.getCommand().equalsIgnoreCase("474")) {
            return true;
        }
        UserProperties up = message.getServer().getUserProperties();
        String nick = message.getTargetParams();
        if (nick == null || nick.isEmpty()) {
            return false;
        }
        boolean ispart = message.getCommand().equalsIgnoreCase("PART");
        boolean iskick = message.getCommand().equalsIgnoreCase("KICK");
        return (ispart || iskick) && nick.equals(up.getNickname());
    }

    @Override
    public void handle(IrcMessage message) {
        final Channel channel = message.getServer().getChannel();
        final String target;
        if (message.getCommand().equalsIgnoreCase("474")) {
            target = message.getTargetParams();
        } else {
            target = message.getTarget();
        }
        message.getServer().getIrcClient().getGroup().schedule(new Runnable() {
            @Override
            public void run() {
                channel.writeAndFlush("JOIN " + target);
            }
        }, 10, TimeUnit.SECONDS);
    }
}
