package xxx.moparisthebest.irclib;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrcClient {

    private static final AttributeKey<UserProperties> USERPROPS_ATTR = AttributeKey.valueOf("UserProperties.attr");
    private static final AttributeKey<NetworkProperties> NETPROPS_ATTR = AttributeKey.valueOf("NetworkProperties.attr");
    private static final AttributeKey<IrcClient> CLIENT_ATTR = AttributeKey.valueOf("IrcClient.attr");

    public static IrcClient getIrcClient(Channel channel) {
        return channel.attr(CLIENT_ATTR).get();
    }

    public static NetworkProperties getNetworkProperties(Channel channel) {
        return channel.attr(NETPROPS_ATTR).get();
    }

    public static UserProperties getUserProperties(Channel channel) {
        return channel.attr(USERPROPS_ATTR).get();
    }

    private EventLoopGroup group = new NioEventLoopGroup();
    private List<Channel> connections = new CopyOnWriteArrayList<Channel>();

    public List<Channel> getConnections() {
        return connections;
    }

    public void disconnect(Channel ircConnection) {
        connections.remove(ircConnection);
        ircConnection.close();
    }

    public Channel connect(UserProperties up, NetworkProperties np) {
        System.out.printf("Connecting to %s:%d ... \r\n", np.getHost(), np.getPort());
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new IrcInitializer(this, np.isSSL()));
        ChannelFuture cf = b.connect(np.getHost(), np.getPort());
        cf.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                connections.remove(channel);
                OnDisconnect(channel);
            }
        });
        Channel chan = cf.channel();
        chan.attr(USERPROPS_ATTR).set(up);
        chan.attr(NETPROPS_ATTR).set(np);
        chan.attr(CLIENT_ATTR).set(this);
        connections.add(chan);
        return chan;
    }

    public void OnSend(Channel channel, String message) {
        System.out.println("OnSend(): " + channel + " - " + message);
    }

    public void OnIrcMessage(IrcMessage message) {
        System.out.println("OnIrcMessage(): " + message.getChannel() + " - " + message.getRaw());
    }

    public void OnDisconnect(Channel channel) {
        System.out.println("OnDisconnect(): " + channel);
    }

    public void OnException(Channel channel, Throwable cause) {
        System.out.println("OnException(): " + channel + " - " + cause.getMessage());
        cause.printStackTrace();
    }

}
