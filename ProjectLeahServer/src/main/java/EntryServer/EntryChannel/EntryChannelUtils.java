package EntryServer.EntryChannel;

import EntryServer.EntryChannel.Client.ClientSetUp;
import EntryServer.MainEntryPointServer;
import QueueManagers.SyncProcessQueueManager;
import QueueManagers.SyncProcessQueueObject;
import Utils.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static Utils.FormatUtils.*;

public class EntryChannelUtils {
    ChannelFuture cf_udp = null;
    ChannelFuture cf_tcp = null;
    SyncProcessQueueManager spqm;
    LatchWrapper lw;
    SyncProcessQueueObject<ByteBuf> initialQueue;

    public EntryChannelUtils(SyncProcessQueueManager spqm, LatchWrapper lw, SyncProcessQueueObject<ByteBuf> initialQueue) {
        this.spqm = spqm;
        this.lw = lw;
        this.initialQueue = initialQueue;
    }

    public JsonElement[] settingEntryPoint(RequestFormat request, ClientSetUp clientSetUp, int settingFlag) throws Exception {
        int personalServerSetUpFlag = request.getPersonalServerSetUpFlag();

        int TCP_PORT = request.getTCP_PORT();
        int UDP_PORT = request.getUDP_PORT();
        System.out.println("TCP PORT: " + TCP_PORT);
        System.out.println("UDP_PORT: " + UDP_PORT);
        JsonArray APIList= request.getAPIList();

        List<Integer> flag_result = ServerSetUp(APIList);
        int flag = flag_result.get(0);
        if (personalServerSetUpFlag != 1) {
            if (TCP_PORT != -1 && (flag == 1 || flag == 2)) {
                if (settingFlag == 0) {
                    MainEntryPointServer.wakeTCPServer(TCP_PORT);
                } else {
                    MainEntryPointServer.changeTCPPort(TCP_PORT);
                }
            }
            else if (UDP_PORT != -1 && (flag == -2 || flag == 2)) {
                if (settingFlag == 0) {
                    MainEntryPointServer.wakeUDPServer(UDP_PORT);
                } else {
                    MainEntryPointServer.changeUDPPort(UDP_PORT);
                }
            } else {
                throw new Exception("Error: PORT is not valid or Protocl not matched");
            }
        }

        if (flag == -2) {
            cf_udp = clientSetUp.udpClientSetUp("127.0.0.1", request.getUDP_PORT());
            cf_udp.sync();
        } else if (flag == 1) {
            cf_tcp = clientSetUp.tcpClientSetUp("127.0.0.1", request.getTCP_PORT());
            cf_tcp.sync();
        } else if (flag == 2) {
            cf_udp = clientSetUp.udpClientSetUp("127.0.0.1", flag);
            cf_tcp = clientSetUp.tcpClientSetUp("127.0.0.1", flag);

        } else {
            throw new Exception("Error: flag is not matched");
        }

        return sortAPIInOrder(flag_result.get(1), APIList);
    }

    public List<Integer> ServerSetUp(JsonArray APIListInOrder) throws Exception{
        int tcp_flag = -1;
        int udp_flag = 1;
        int length = 0;
        for (JsonElement API : APIListInOrder) {
            length++;
            String Protocol = API.getAsJsonObject().get("Protocol").getAsString();
            if (Protocol.equals("DataGram")) {
                udp_flag = 2;
            }

            if (Protocol.equals("DataStream")) {
                tcp_flag = 1;
            }
        }
        List<Integer> flag_result = new ArrayList<>(List.of(udp_flag * tcp_flag, length));

        return flag_result;
        // -1(not), -2(only_u), 1(only_t), 2(both)
    }

    public JsonElement[] sortAPIInOrder(int length, JsonArray APIList) throws Exception{
        JsonElement[] APIListInOrder = new JsonElement[length];
        for (JsonElement API : APIList) {
            int index = API.getAsJsonObject().get("order").getAsInt();
            APIListInOrder[index - 1] = API;
        }
        return APIListInOrder;
    }

    public void setAPIKey(JsonElement[] sortAPIList) throws InterruptedException {
        System.out.println("Set API Key");
        for (JsonElement API : sortAPIList) {
            lw.setLatch(1);

            ClientRequestFormat clientRequest = new ClientRequestFormat(API, "setting");

            if (clientRequest.getProtocol().equals("DataGram")) {
                cf_udp.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
            }

            if (clientRequest.getProtocol().equals("DataStream")) {
                cf_tcp.channel().writeAndFlush(clientRequestToByteBuf(clientRequest));
            }
            lw.awaitLatch();
        }

    }

    public void queueSet(JsonElement[] sortAPIList) {
        System.out.println("Setting Done");
        spqm.setDataBufferObjects(sortAPIList.length);
    }

    public void communicateClient(JsonElement[] sortAPIList, int count, ChannelHandlerContext ctx) {
        if (count >= sortAPIList.length) {
            return;
        }
        JsonElement API = sortAPIList[count];
        String Protocol = API.getAsJsonObject().get("Protocol").getAsString();
        ClientRequestFormat clientRequest = new ClientRequestFormat(API, "communicate");

        if (count == 0) {
            clientRequest.buildInitialObject(count, initialQueue.deQueue());
            if (clientRequest.getProtocol().equals("DataStream")) {
                cf_tcp.channel().writeAndFlush(clientRequestToByteBuf(clientRequest)).addListener(future -> {
                    if (future.isSuccess()) {
                        waitForClientResponse(cf_tcp.channel(), sortAPIList, ctx);
                    } else {
                        System.err.println(future.cause());
                    }
                });
            }
            else if (clientRequest.getProtocol().equals("DataGram")) {
                cf_udp.channel().writeAndFlush(clientRequestToByteBuf(clientRequest)).addListener(future -> {
                    if (future.isSuccess()) {
                        waitForClientResponse(cf_tcp.channel(), sortAPIList, ctx);
                    } else {
                        System.err.println(future.cause());
                    }
                });;
            }
        } else {
            if (Protocol.equals("DataStream")) {
                BackPressure.setBackPressureFlag(1);
                spqm.clearInitialQueue();
                Object msg = spqm.getDataBufferObjects()[count - 1].deQueue();
                cf_tcp.channel().writeAndFlush(msg).addListener(future -> {
                    if (future.isSuccess()) {
                        waitForClientResponse(cf_tcp.channel(), sortAPIList, ctx);

                    } else {
                        System.err.println(future.cause());
                    }
                });

            } else if (Protocol.equals("DataGram")) {
                cf_udp.channel().writeAndFlush(spqm.getDataBufferObjects()[count - 1].deQueue()).addListener(future -> {
                    if (future.isSuccess()) {
                        waitForClientResponse(cf_tcp.channel(), sortAPIList, ctx);
                    } else {
                        System.err.println(future.cause());
                    }
                });;
            }

        }
    }
    private void waitForClientResponse(Channel channel, JsonElement[] sortAPIList, ChannelHandlerContext serverCtx) {

        channel.pipeline().addAfter("Delimiter", UUID.randomUUID().toString() ,new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                ClientRequestFormat clientRequest = byteBufToClientRequest(msg);

                if (clientRequest.getBackPressureFlag() == 0) {

                    serverCtx.channel().writeAndFlush(responseToByteBuf(new ResponseFormat("flowControl", clientRequest.getBackPressureFlag())));

                } else {
                    clientRequest.countUp();
                    if (clientRequest.getCount() == sortAPIList.length) {
                        ResponseFormat response = new ResponseFormat("result", 0);
                        response.setData(clientRequest.getData());
                        serverCtx.channel().writeAndFlush(responseToByteBuf(response));
                    } else {
                        int count  = clientRequest.getCount();
                        clientRequest.setCount(count);
                        clientRequest.setAPI_KEY(sortAPIList[count].getAsJsonObject().get("API_KEY").getAsString());
                        clientRequest.setAPI_Name(sortAPIList[count].getAsJsonObject().get("API_Name").getAsString());
                        spqm.getDataBufferObjects()[count - 1].enQueue(clientRequestToByteBuf(clientRequest));
                        communicateClient(sortAPIList, count, serverCtx);
                        ctx.pipeline().remove(this);
                    }
                }
            }
        });
    }
}
