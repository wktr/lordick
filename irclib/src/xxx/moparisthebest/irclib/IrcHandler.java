package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import xxx.moparisthebest.irclib.messages.Ping;
import xxx.moparisthebest.irclib.properties.UserProperties;
import xxx.moparisthebest.util.ClassEnumerator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcHandler extends SimpleChannelInboundHandler<String> {

    // http://mybuddymichael.com/writings/a-regular-expression-for-irc-messages.html
    public static Pattern IRC_PATTERN = Pattern.compile("^(?:[:](?<prefix>\\S+) )?(?<type>\\S+)(?: (?!:)(?<destination>.+?))?(?: [:](?<message>.+))?$");

    Map<String, IrcMessage> messageHandlers = new HashMap<String, IrcMessage>();

    public IrcHandler() {
        for (Class c : ClassEnumerator.getClassesForPackage(Ping.class.getPackage())) {
            try {
                IrcMessage message = (IrcMessage) c.newInstance();
                messageHandlers.put(message.GetMessage(), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        System.out.println("<<< " + message);
        Matcher m = IRC_PATTERN.matcher(message);
        if (!m.matches()) {
            // todo: pritn error or something
            return;
        }
        if (messageHandlers.containsKey(m.group("type"))) {
            messageHandlers.get(m.group("type")).HandleMessage(ctx, message, m.group("prefix"), m.group("type"), m.group("destination"), m.group("message"));
        }
        ctx.channel().attr(IrcClient.CLIENT_ATTR).get().OnIrcMessage(ctx.channel(), message, m.group("prefix"), m.group("type"), m.group("destination"), m.group("message"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserProperties userprops = ctx.channel().attr(IrcClient.USERPROPS_ATTR).get();
        ctx.write("NICK :" + userprops.getNickname());
        ctx.writeAndFlush("USER " + userprops.getIdent() + " 0 * :" + userprops.getRealname());
    }
}
