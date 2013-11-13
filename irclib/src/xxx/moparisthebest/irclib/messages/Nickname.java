package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.Hostmask;

import java.util.Random;

public class Nickname implements IrcMessage {
    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, IrcChat chat) {
        return chat.getType().equalsIgnoreCase("NICK") || chat.getType().equalsIgnoreCase("433") || chat.getType().equalsIgnoreCase("396");
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, IrcChat chat) {
        UserProperties up = IrcClient.getUserProperties(ctx.channel());
        String nick = Hostmask.getNick(chat.getPrefix());
        if (chat.getType().equalsIgnoreCase("NICK")) { // if we change (or someone else) changes our nickname, keep track
            if (nick.equals(up.getNickname())) {
                up.setNickname(nick);
            }
        } else if (chat.getType().equalsIgnoreCase("433")) { // if our nick is in use
            ctx.writeAndFlush("NICK " + up.getAltnick() + new Random().nextInt(1000));
        } else { // update host
            up.setHost(chat.getDestination());
        }
    }
}
