package EntryServer;

import EntryServer.EntryChannel.EntryPointInitializer;
import EntryServer.EntryChannel.EventLoopGroupManager;
import ExampleTCPServer.MainExampleTCPServer;
import ExampleUDPServer.MainExampleUDPServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class MainEntryPointServer {

    static MainExampleUDPServer udpServer = new MainExampleUDPServer();
    static MainExampleTCPServer tcpServer = new MainExampleTCPServer();
    public static void main(String[] args) throws  Exception {
        if (args.length != 2) {
            System.err.println("Usage: <Protocol> <PORT>");
            System.err.println("Protocol: Datagram | DataStream");
            return;
        }

        String PROTOCOL = args[0];
        int PORT = Integer.parseInt(args[1]);

        if (!(PROTOCOL.equals("DataGram") || PROTOCOL.equals("DataStream"))) {
            System.err.println("Error: Protocol Name is not properly set");
            return;
        }

        EventLoopGroupManager eventLoopGroupManager = new EventLoopGroupManager();

        try {
            if (PROTOCOL.equals("DataGram")) {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroupManager.GetWorkerGroup())
                        .channel(NioDatagramChannel.class)
                        .handler(new EntryPointInitializer());
                System.out.println("DataGram Server Successfully Started");
                ChannelFuture cf = b.bind(PORT).sync();
                cf.channel().closeFuture().sync();
            } else {
                ServerBootstrap sb = new ServerBootstrap();
                sb.group(eventLoopGroupManager.GetWorkerGroup(), eventLoopGroupManager.GetBossGroup())
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new EntryPointInitializer())
                        .handler(new LoggingHandler(LogLevel.DEBUG));
                System.out.println("DataStream Server Successfully Started");
                ChannelFuture cf = sb.bind(PORT).sync();
                cf.channel().closeFuture().sync();
            }
        } finally {
           eventLoopGroupManager.shutDownAllGraceFully();;
        }
    }

    public static void wakeTCPServer(int PORT) throws Exception {
        new Thread(() -> {
            try {
                tcpServer.run(PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void changeTCPPort(int PORT) throws Exception {
        tcpServer.stop();
        wakeTCPServer(PORT);
    }
    public static void wakeUDPServer(int PORT) throws Exception {
        new Thread(() -> {
            try {
                udpServer.run(PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void changeUDPPort(int PORT) throws Exception {
        udpServer.stop();
        wakeUDPServer(PORT);
    }
}

