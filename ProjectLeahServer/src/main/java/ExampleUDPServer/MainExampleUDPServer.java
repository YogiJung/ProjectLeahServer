package ExampleUDPServer;

import ExampleUDPServer.UDPChannel.UDPChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class MainExampleUDPServer {
    ChannelFuture channelFuture = null;
    public void run(int PORT) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.channel(NioDatagramChannel.class)
                    .group(workerGroup)
                    .handler(new UDPChannelInitializer());

            ChannelFuture cf = b.bind(PORT).sync();
            channelFuture = cf;
            cf.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
    public void stop() throws Exception{
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }
}
