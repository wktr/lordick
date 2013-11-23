package xxx.moparisthebest.irclib;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import xxx.moparisthebest.irclib.messages.IrcMessage;
import xxx.moparisthebest.irclib.net.IrcInitializer;
import xxx.moparisthebest.irclib.net.IrcServer;
import xxx.moparisthebest.irclib.properties.NetworkProperties;
import xxx.moparisthebest.irclib.properties.UserProperties;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IrcClient {

    private EventLoopGroup group = new NioEventLoopGroup();
    private List<Channel> connections = new CopyOnWriteArrayList<Channel>();

    public EventLoopGroup getGroup() {
        return group;
    }

    public List<Channel> getConnections() {
        return connections;
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
                onDisconnect(new IrcServer(channel));
            }
        });
        Channel chan = cf.channel();
        IrcServer.setProperties(chan, up, np, this);
        connections.add(chan);
        return chan;
    }

    public void onConnect(IrcServer server) {
        System.out.println("onConnect(): " + server);
        server.getChannel().write("NICK " + server.getUserProperties().getNickname());
        server.getChannel().writeAndFlush("USER " + server.getUserProperties().getIdent() + " 0 * :" + server.getUserProperties().getRealname());
    }

    public void onSend(IrcServer server, String message) {
        System.out.println("onSend(): " + server + " - " + message);
    }

    public void onMessage(IrcMessage message) {
        System.out.println("onMessage(): " + message.getServer() + " - " + message.getRaw());
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
