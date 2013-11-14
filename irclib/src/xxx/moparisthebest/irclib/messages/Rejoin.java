package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.Hostmask;

public class Rejoin implements IrcMessage {
    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, IrcChat chat) {
        UserProperties up = IrcClient.getUserProperties(ctx.channel());
        return (chat.getType().equalsIgnoreCase("PART") && Hostmask.getNick(chat.getPrefix()).equalsIgnoreCase(up.getNickname()))
                || (chat.getType().equalsIgnoreCase("KICK") && chat.getDestParams().equalsIgnoreCase(up.getNickname()))
                || chat.getType().equalsIgnoreCase("474");
    }

    @Override
    public void handleMessage(final ChannelHandlerContext ctx, final IrcChat chat) {
        new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                    if (chat.getType().equalsIgnoreCase("474")) {
                        ctx.writeAndFlush("JOIN " + chat.getDestParams());
                    } else {
                        ctx.writeAndFlush("JOIN " + chat.getDestination());
                    }
                } catch (Exception ignored) {
                }
            }
        }.run();
    }
}
