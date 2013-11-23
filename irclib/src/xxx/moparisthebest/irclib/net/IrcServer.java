package xxx.moparisthebest.irclib.net;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import xxx.moparisthebest.irclib.IrcClient;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

public class IrcServer {

    private static final AttributeKey<UserProperties> USERPROPS_ATTR = AttributeKey.valueOf("UserProperties.attr");
    private static final AttributeKey<NetworkProperties> NETPROPS_ATTR = AttributeKey.valueOf("NetworkProperties.attr");
    private static final AttributeKey<IrcClient> CLIENT_ATTR = AttributeKey.valueOf("IrcClient.attr");

    public static void setProperties(Channel channel, UserProperties up, NetworkProperties np, IrcClient client) {
        channel.attr(USERPROPS_ATTR).set(up);
        channel.attr(NETPROPS_ATTR).set(np);
        channel.attr(CLIENT_ATTR).set(client);
    }

    private final Channel channel;

    public IrcServer(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public IrcClient getIrcClient() {
        return channel.attr(CLIENT_ATTR).get();
    }

    public NetworkProperties getNetworkProperties() {
        return channel.attr(NETPROPS_ATTR).get();
    }

    public UserProperties getUserProperties() {
        return channel.attr(USERPROPS_ATTR).get();
    }

    @Override
    public String toString() {
        return "[" + channel.remoteAddress() + "]";
    }
}
