package EntryServer.EntryChannel;

import EntryServer.EntryChannel.Handlers.ServerHandler.EntryPointOutboundHandler;
import EntryServer.EntryChannel.Handlers.ServerHandler.EntryPointInboundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class EntryPointInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        ByteBuf delimiter = Unpooled.copiedBuffer(new byte[] {0x03});
        cp.addLast(new CustomDelimiterDecoder(44100 * 10, false, delimiter));
        cp.addLast(new EntryPointInboundHandler());
        cp.addLast(new EntryPointOutboundHandler());
    }
}
