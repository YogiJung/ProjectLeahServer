package EntryServer.EntryChannel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CustomDelimiterDecoder extends ByteToMessageDecoder {
    int maxFrameLength;
    boolean stepDelimiter;
    ByteBuf delimiter;
    public CustomDelimiterDecoder(int maxFrameLength, boolean stepDelimiter, ByteBuf delimiter) {
        this.maxFrameLength = maxFrameLength;
        this.stepDelimiter = stepDelimiter;
        this.delimiter = delimiter.slice();
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int startIndex = in.readerIndex();
        int endIndex;

        while (in.isReadable()) {
            if (in.readByte() == delimiter.getByte(0)) {
                endIndex = in.readerIndex();
                //int length = endIndex - startIndex - 1; //delimiter excluded
                int length = endIndex - startIndex - 1;
                if (length > 0) {
                    ByteBuf frame = ctx.alloc().buffer(length);
                    in.getBytes(startIndex, frame, length);
                    frame.retain();
                    out.add(frame);
                }

                startIndex = endIndex;
            }
        }
        in.readerIndex(startIndex);
    }
}
