package xxx.moparisthebest.irclib.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import xxx.moparisthebest.irclib.IrcClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.List;

public class IrcInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder STRING_DECODER = new StringDecoder();
    private static final StringEncoder STRING_ENCODER = new StringEncoder();

    private boolean ssl;
    private IrcClient client;

    public IrcInitializer(IrcClient client, boolean ssl) {
        this.client = client;
        this.ssl = ssl;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (ssl) {
            // todo: actually check certs
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, IrcSSLTrustManagerFactory.getTrustManagers(), null);
            SSLEngine engine = context.createSSLEngine();
            engine.setUseClientMode(true);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(8192));
        pipeline.addLast("stringDecoder", STRING_DECODER);

        pipeline.addLast("stringEncoder", STRING_ENCODER);
        pipeline.addLast("lineEncoder", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, String message, List<Object> out) throws Exception {
                client.OnSend(ctx.channel(), message);
                out.add(message + "\r\n");
            }
        });

        pipeline.addLast("handler", new IrcHandler());
    }
}