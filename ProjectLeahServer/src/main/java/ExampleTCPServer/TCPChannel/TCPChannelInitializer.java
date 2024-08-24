package ExampleTCPServer.TCPChannel;

import EntryServer.EntryChannel.CustomDelimiterDecoder;
import ExampleTCPServer.TCPChannel.Handlers.TCPInboundHandler;
import ExampleTCPServer.TCPChannel.Handlers.TCPOutboundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.FixedLengthFrameDecoder;

public class TCPChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        ByteBuf delimiter = Unpooled.copiedBuffer(new byte[] {0x03});
        cp.addLast(new CustomDelimiterDecoder(44100 * 10, false, delimiter));
        cp.addLast(new TCPInboundHandler());
        cp.addLast(new TCPOutboundHandler());
    }
}
