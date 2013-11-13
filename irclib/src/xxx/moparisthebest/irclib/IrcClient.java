package xxx.moparisthebest.irclib;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrcClient {

    public static final AttributeKey<UserProperties> USERPROPS_ATTR = AttributeKey.valueOf("UserProperties.attr");
    public static final AttributeKey<NetworkProperties> NETPROPS_ATTR = AttributeKey.valueOf("NetworkProperties.attr");
    public static final AttributeKey<IrcClient> CLIENT_ATTR = AttributeKey.valueOf("IrcClient.attr");

    EventLoopGroup group = new NioEventLoopGroup();
    List<Channel> connections = new CopyOnWriteArrayList<Channel>();

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
        // todo: some constructor to ircinitialiser for ssl enabled in pipeline
        b.group(group).channel(NioSocketChannel.class).handler(new IrcInitializer());
        ChannelFuture cf = b.connect(np.getHost(), np.getPort());
        // todo: add something to the future to reconnect/whatever
        // todo: add something to the future to remove it from connection list and fire some OnDisconnect?
        Channel chan = cf.channel();
        chan.attr(USERPROPS_ATTR).set(up);
        chan.attr(NETPROPS_ATTR).set(np);
        chan.attr(CLIENT_ATTR).set(this);
        connections.add(chan);
        return chan;
    }

    public void OnIrcMessage(Channel channel, String raw, String prefix, String type, String destination, String message) {

    }

}
