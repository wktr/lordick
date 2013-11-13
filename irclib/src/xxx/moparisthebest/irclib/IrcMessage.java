package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;

public interface IrcMessage {

    public boolean shouldHandle(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message);

    public void handleMessage(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message);

}
