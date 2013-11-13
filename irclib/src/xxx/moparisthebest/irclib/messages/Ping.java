package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcMessage;

public class Ping implements IrcMessage {
    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message) {
        return type.equalsIgnoreCase("PING");
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message) {
        ctx.writeAndFlush("PONG :" + message);
    }
}
