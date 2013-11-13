package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;

public interface IrcMessage {

    public boolean shouldHandle(ChannelHandlerContext ctx, IrcChat chat);

    public void handleMessage(ChannelHandlerContext ctx, IrcChat chat);

}
