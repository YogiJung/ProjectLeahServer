package EntryServer.EntryChannel.Handlers.ServerHandler;

import io.netty.channel.*;

public class EntryPointOutboundHandler extends ChannelOutboundHandlerAdapter {

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
