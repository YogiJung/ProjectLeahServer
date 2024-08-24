package ExampleTCPServer.TCPChannel.Handlers;

import APICluster.APIClusterManager;
import Utils.BackPressure;
import Utils.ClientRequestFormat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.http.HttpRequest;
import java.util.Base64;

import static APICluster.APIClusterManager.playAudio2;
import static ExampleTCPServer.TCPChannel.TCPChannelUtils.combineJsonData;
import static Utils.FormatUtils.*;

public class TCPInboundHandler extends ChannelInboundHandlerAdapter {
        APIClusterManager acm = new APIClusterManager();
        ByteBuf cumulativeBuffer;

        int frameLength = 44100 * 20;
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        cumulativeBuffer = ctx.alloc().buffer();
    }
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!((ByteBuf) msg).isReadable()) {
            System.err.println("Non-readable tcp inbound Handler");
            return;
        }

            ClientRequestFormat clientRequest = byteBufToClientRequest((ByteBuf) msg);
            if (clientRequest.getAPI_Name().equals("GoogleSTT") && BackPressure.backPressureFlag == 1) {
                return;
            }

            if (clientRequest.getHeader().get("endpoint").getAsString().equals("setting")) {
                String API_Name = clientRequest.getAPI_Name();
                if (API_Name.equals("ChatGPT")) {
                    acm.chatGPTAPI.setAPIKey(clientRequest.getAPI_KEY());
                }
                if (API_Name.equals("GoogleSTT")) {
                    acm.googleCloudSTTAPI.setCredential_file_route(clientRequest.getAPI_KEY());
                }
                if (API_Name.equals("GoogleTTS")) {
                    acm.googleCloudTTSAPI.setCredential_file_route(clientRequest.getAPI_KEY());
                }
                if (API_Name.equals("PlayHT")) {

                }
                if (API_Name.equals("ReplicaStudio")) {
                    acm.replicaStudioAPI.setAccess_Token();
                }

                ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));

            } else {
                String API_Name = clientRequest.getAPI_Name();
                if (API_Name.equals("ChatGPT") && acm.chatGPTAPI != null) {
                    String content = clientRequest.getData();

                    if (content == null || content.isEmpty()) {
                        clientRequest.setBackPressureFlag(0);
                        ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                        return;

                    } else {
                        HttpRequest request = acm.chatGPTAPI.makeRequest(content);
                        acm.chatGPTAPI.sendRequest(request);
                        String data = acm.spqm.findSyncQueue("ChatGPTQueue").takeQueue().toString();
                        clientRequest.setData(data);
                        ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                    }
                }

                if (API_Name.equals("GoogleTTS") && acm.googleCloudTTSAPI != null) {
                    byte[] audioContents = acm.googleCloudTTSAPI.StringToVoice(clientRequest.getData());
                    String audioString = Base64.getEncoder().encodeToString(audioContents);
                    clientRequest.setData(audioString);
                    ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                    BackPressure.setBackPressureFlag(0);
                    cumulativeBuffer.clear();
                }

                if (API_Name.equals("PlayHT") && acm.playHTAPI != null) {
                    HttpRequest request = acm.playHTAPI.makeRequest(clientRequest.getData());
                    acm.playHTAPI.sendRequest(request);
                    byte[] audioByte = (byte[]) acm.spqm.findSyncQueue("playHTQueue").takeQueue();
                    playAudio2(audioByte);
                    ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                }

                if (API_Name.equals("ReplicaStudio") && acm.replicaStudioAPI != null) {
                    HttpRequest request = acm.replicaStudioAPI.makeRequest(clientRequest.getData());
                    acm.replicaStudioAPI.sendRequest(request);
                    byte[] audioByte = (byte[]) acm.spqm.findSyncQueue("replicaStudioQueue").takeQueue();
                    String audioString = Base64.getEncoder().encodeToString(audioByte);
                    clientRequest.setData(audioString);
                    ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                    BackPressure.setBackPressureFlag(0);
                    cumulativeBuffer.clear();
                }


                if (API_Name.equals("GoogleSTT") && acm.googleCloudTTSAPI != null) {
                    ByteBuf in = (ByteBuf) msg;
                    in.writeByte(0x03);
                    if (BackPressure.getBackPressureFlag() == 1) {
                        in.release();
                        in.clear();
                        return;
                    }
                    cumulativeBuffer.writeBytes(in);
                    in.release();

                    while(cumulativeBuffer.readableBytes() < frameLength) {
                        System.out.println(cumulativeBuffer);
                        return;
                    }

                    if (cumulativeBuffer.readableBytes() >= frameLength) {
                        BackPressure.setBackPressureFlag(1); //remove later

                        ByteBuf slicedBuf = cumulativeBuffer.readRetainedSlice(frameLength);

                        byte[] decodedBytes = combineJsonData(slicedBuf);

                        cumulativeBuffer.clear();

                        String data = acm.googleCloudSTTAPI.streamRecognize(decodedBytes);

                        clientRequest.setData(data);

                        ctx.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
                    }
                }
            }

    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
