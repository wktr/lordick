package xxx.moparisthebest.irclib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.List;

public class IrcInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder STRING_DECODER = new StringDecoder();
    private static final StringEncoder STRING_ENCODER = new StringEncoder();

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(8192));
        pipeline.addLast("stringDecoder", STRING_DECODER);

        pipeline.addLast("stringEncoder", STRING_ENCODER);
        pipeline.addLast("lineEncoder", new MessageToMessageEncoder<String>() {
            @Override
            protected void encode(ChannelHandlerContext channelHandlerContext, String message, List<Object> out) throws Exception {
                System.out.println("> " + message);
                out.add(message + "\r\n");
            }
        });

        pipeline.addLast("handler", new IrcHandler());
    }
}