package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcMessage;

public class Ping implements IrcMessage {
    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, IrcChat chat) {
        return chat.getType().equalsIgnoreCase("PING");
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, IrcChat chat) {
        ctx.writeAndFlush("PONG :" + chat.getMessage());
    }
}
