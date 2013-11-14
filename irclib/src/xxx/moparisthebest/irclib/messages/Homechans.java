package xxx.moparisthebest.irclib.messages;

import io.netty.channel.ChannelHandlerContext;
import xxx.moparisthebest.irclib.IrcChat;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.IrcMessage;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class Homechans implements IrcMessage {

    @Override
    public boolean shouldHandle(ChannelHandlerContext ctx, IrcChat chat) {
        return chat.getType().equalsIgnoreCase("001");
    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, IrcChat chat) {
        UserProperties up = IrcClient.getUserProperties(ctx.channel());
        for(String s : up.getHomeChannels()) {
            ctx.write("JOIN " + s);
        }
        ctx.flush();
    }
}
