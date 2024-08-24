package ExampleUDPServer.UDPChannel;

import ExampleUDPServer.UDPChannel.Handlers.UDPInboundHandler;
import ExampleUDPServer.UDPChannel.Handlers.UDPOutboundHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class UDPChannelInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(new UDPInboundHandler());
        cp.addLast(new UDPOutboundHandler());
    }
}
