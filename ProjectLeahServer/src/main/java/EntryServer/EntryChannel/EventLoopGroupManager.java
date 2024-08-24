package EntryServer.EntryChannel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventLoopGroupManager {

    static EventLoopGroup workerGroup = new NioEventLoopGroup();
    static EventLoopGroup bossGroup = new NioEventLoopGroup();
    static EventLoopGroup tcpWorkerGroup = new NioEventLoopGroup();
    static EventLoopGroup udpWorkerGroup = new NioEventLoopGroup();

    public void shutDownAllGraceFully() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        tcpWorkerGroup.shutdownGracefully();
        udpWorkerGroup.shutdownGracefully();
    }

    public static EventLoopGroup GetTcpWorkerGroup() {
        return tcpWorkerGroup;
    }
    public static EventLoopGroup GetUdpWorkerGroup() {
        return udpWorkerGroup;
    }
    public static EventLoopGroup GetBossGroup() {
        return bossGroup;
    }
    public static EventLoopGroup GetWorkerGroup() {
        return workerGroup;
    }
}
