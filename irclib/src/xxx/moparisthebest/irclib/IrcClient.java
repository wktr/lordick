package xxx.moparisthebest.irclib;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.net.IrcInitializer;
import xxx.moparisthebest.irclib.net.IrcServer;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class IrcClient {

    public static String replaceColourCodes(String s) {
        return s.replaceAll("\u0002", "^B") // bold
                .replaceAll("\u000F", "^N") // normal
                .replaceAll("\u0011", "^F") // fixed
                .replaceAll("\u0012", "^R") // reverse
                .replaceAll("\u0016", "^N") // invers
                .replaceAll("\u001D", "^I") // italis
                .replaceAll("\u001F", "^U") // underline
                .replaceAll("\u0003", "^C") // colour 1
                .replaceAll("\u0004", "^K"); // colour 2
    }

    private EventLoopGroup group = new NioEventLoopGroup(2);
    ChannelGroup connections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public EventLoopGroup getGroup() {
        return group;
    }

    public ChannelGroup getConnections() {
        return connections;
    }

    public Channel connect(UserProperties up, NetworkProperties np) {
        System.out.printf("Connecting to %s:%d ... \r\n", np.getHost(), np.getPort());
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new IrcInitializer(this, np.isSSL()));
        ChannelFuture cf = b.connect(np.getHost(), np.getPort());
        Channel channel = cf.channel();
        IrcServer.setProperties(channel, up, np, this);
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                onDisconnect(new IrcServer(channelFuture.channel()));
            }
        });
        connections.add(channel);
        return channel;
    }

    public void onConnect(IrcServer server) {
        System.out.println("onConnect(): " + server);
    }

    public void onSend(IrcServer server, String message) {
        System.out.println("onSend(): " + server + " - " + replaceColourCodes(message));
    }

    public void onMessage(IrcMessage message) {
        System.out.println("onMessage(): " + message.getServer() + " - " + replaceColourCodes(message.getRaw()));
    }

    public void onDisconnect(IrcServer server) {
        System.out.println("onDisconnect(): " + server);
    }

    public void onException(IrcServer server, Throwable cause) {
        System.out.println("onException(): " + server + " - " + cause.getMessage());
        cause.printStackTrace();
    }

    public void onUnknown(IrcServer server, String message) {
        System.out.println("onUnknown(): " + server + " - " + message);
    }

}
