package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import xxx.moparisthebest.irclib.messages.Ping;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcHandler extends SimpleChannelInboundHandler<String> {

    // http://mybuddymichael.com/writings/a-regular-expression-for-irc-messages.html
    public static Pattern IRC_PATTERN = Pattern.compile("^(?:[:](?<prefix>\\S+) )?(?<type>\\S+)(?: (?!:)(?<destination>.+?))?(?: [:](?<message>.+))?$");

    private List<IrcMessage> messageHandlers = new CopyOnWriteArrayList<IrcMessage>();

    public IrcHandler() {
        for (Class c : ClassEnumerator.getClassesForPackage(Ping.class.getPackage())) {
            try {
                IrcMessage message = (IrcMessage) c.newInstance();
                messageHandlers.add(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        Matcher m = IRC_PATTERN.matcher(message);
        if (!m.matches()) {
            // todo: print error or something
            return;
        }
        IrcChat chat = new IrcChat(message, m.group("prefix"), m.group("type"), m.group("destination"), m.group("message"));
        for (IrcMessage ircMessage : messageHandlers) {
            if (ircMessage.shouldHandle(ctx, chat)) {
                ircMessage.handleMessage(ctx, chat);
            }
        }
        IrcClient.getIrcClient(ctx.channel()).OnIrcMessage(ctx.channel(), chat);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        IrcClient.getIrcClient(ctx.channel()).OnException(ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserProperties userprops = IrcClient.getUserProperties(ctx.channel());
        ctx.write("NICK " + userprops.getNickname());
        ctx.writeAndFlush("USER " + userprops.getIdent() + " 0 * :" + userprops.getRealname());
    }
}
