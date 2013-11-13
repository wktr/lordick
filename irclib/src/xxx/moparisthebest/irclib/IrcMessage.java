package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;

public interface IrcMessage {

    public String GetMessage();

    public void HandleMessage(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message);

}
