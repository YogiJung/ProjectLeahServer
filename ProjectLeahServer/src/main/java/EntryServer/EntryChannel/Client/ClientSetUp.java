package EntryServer.EntryChannel.Client;

import EntryServer.EntryChannel.Client.TCPClient.Handlers.TCPClientInboundHandler;
import EntryServer.EntryChannel.Client.UDPClient.Handlers.UDPClientInboundHandler;
import EntryServer.EntryChannel.CustomDelimiterDecoder;
import EntryServer.EntryChannel.EventLoopGroupManager;
import Utils.LatchWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientSetUp {
    LatchWrapper lw;
    public ClientSetUp(LatchWrapper lw) {
        this.lw = lw;
    }
    public ChannelFuture tcpClientSetUp(String tcp_host, int tcp_PORT) {
        try {

            Bootstrap b_tcp = new Bootstrap();
            b_tcp.group(EventLoopGroupManager.GetTcpWorkerGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline cp = channel.pipeline();
                            ByteBuf delimiter = Unpooled.copiedBuffer(new byte[] {0x03});
                            cp.addLast("Delimiter", new CustomDelimiterDecoder(44100 * 10, false, delimiter));
                            cp.addLast(new TCPClientInboundHandler(lw));
                        }
                    });
            
            return b_tcp.connect(tcp_host, tcp_PORT).sync();

        } catch(Exception e) {
            System.err.println("Error: " + e.getLocalizedMessage());
            return null;
        }
    }
    public ChannelFuture udpClientSetUp(String udp_host, int udp_PORT) {
        try {
            Bootstrap b_udp = new Bootstrap();
            b_udp.group(EventLoopGroupManager.GetUdpWorkerGroup())
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline cp = channel.pipeline();
                            cp.addLast(new UDPClientInboundHandler(lw));
                        }
                    });

            return b_udp.connect(udp_host, udp_PORT).sync();
        } catch(Exception e) {
            System.err.println("Error: " + e.getLocalizedMessage());
            return null;
        }
    }
}
