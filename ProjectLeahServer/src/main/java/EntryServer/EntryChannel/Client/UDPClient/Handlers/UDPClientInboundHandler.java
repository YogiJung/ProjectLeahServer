package EntryServer.EntryChannel.Client.UDPClient.Handlers;

import Utils.LatchWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class UDPClientInboundHandler extends ChannelInboundHandlerAdapter {
    LatchWrapper lw;

    public UDPClientInboundHandler(LatchWrapper lw) {
        this.lw = lw;
    }
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
