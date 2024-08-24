package ExampleTCPServer;

import ExampleTCPServer.TCPChannel.TCPChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class MainExampleTCPServer  {
    public ChannelFuture channelFuture = null;
    public void run(int PORT) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new TCPChannelInitializer())
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("TCP Server Started on PORT : " + PORT);
            ChannelFuture cf = sb.bind(PORT).sync();
            channelFuture = cf;
            cf.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    public void stop() throws Exception {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
    }
}
