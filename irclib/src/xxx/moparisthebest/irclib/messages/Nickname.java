package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcMessage;

public class Nickname implements IrcMessage {
    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message) {
        return type.equalsIgnoreCase("NICK") || type.equalsIgnoreCase("433") || type.equalsIgnoreCase("396");
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, String raw, String prefix, String type, String destination, String message) {
        if (type.equalsIgnoreCase("NICK")) { // if we change (or someone else) changes our nickname, keep track

        } else if (type.equalsIgnoreCase("433")) { // if our nick

        } else {

        }
    }
}
