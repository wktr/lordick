package xxx.moparisthebest.irclib.messages.handlers;

import io.netty.channel.Channel;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.messages.IrcMessageHandler;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.concurrent.TimeUnit;

public class Rejoin implements IrcMessageHandler {
    @Override
    public boolean shouldHandle(IrcMessage message) {
        UserProperties up = message.getServer().getUserProperties();
        return (message.getCommand().equalsIgnoreCase("PART") && message.getHostmask().getNick().equalsIgnoreCase(up.getNickname()))
                || (message.getCommand().equalsIgnoreCase("KICK") && message.getTargetParams().equalsIgnoreCase(up.getNickname()))
                || message.getCommand().equalsIgnoreCase("474");
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
