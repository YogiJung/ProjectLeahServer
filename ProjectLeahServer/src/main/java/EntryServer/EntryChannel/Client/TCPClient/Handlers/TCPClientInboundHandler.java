package EntryServer.EntryChannel.Client.TCPClient.Handlers;

import Utils.ClientRequestFormat;
import Utils.LatchWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import static Utils.FormatUtils.byteBufToClientRequest;

public class TCPClientInboundHandler extends ChannelInboundHandlerAdapter {
    LatchWrapper lw;
    public TCPClientInboundHandler(LatchWrapper lw) {
        this.lw = lw;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ClientRequestFormat clientRequestFormat = byteBufToClientRequest((ByteBuf) msg);
        String endpoint = clientRequestFormat.getHeader().get("endpoint").getAsString();
        if (endpoint.equals("setting")) {
            if (lw != null && lw.getLatch().getCount() != 0) {
                lw.countDownLatch();
            }
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
