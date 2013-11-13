package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcMessage;

public class Ping implements IrcMessage {
    @Override
    public String GetMessage() {
        return "PING";
    }

    @Override
    public void HandleMessage(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message) {
        ctx.writeAndFlush("PONG :" + message);
    }
}
