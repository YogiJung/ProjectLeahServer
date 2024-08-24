package EntryServer.EntryChannel.Handlers.ServerHandler;

import EntryServer.EntryChannel.Client.ClientSetUp;
import EntryServer.EntryChannel.EntryChannelUtils;
import QueueManagers.SyncProcessQueueManager;
import QueueManagers.SyncProcessQueueObject;
import Utils.*;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import static Utils.FormatUtils.byteBufToRequest;
import static Utils.FormatUtils.responseToByteBuf;


public class EntryPointInboundHandler extends ChannelInboundHandlerAdapter {

    //Json Object : {
//               header: {"endpoint" : String(setting, communicate) , }
//               personalServerSetUpFlag: 1(on), 0(off),
//               APIList: [{}, {}, {}],
//               TCP_PORT: int ==> not = -1,
//               UDP_PORT: int,}
//              {API_Name: ChatGPT,API_KEY: String, Protocol: DataGram or DataStream, order: int }
//              {API_KEY: String, Protocol: DataGram or DataStream }
//               {API_KEY: String, Protocol: DataGram or DataStream }
//
//    Data : {header: {"endpoint": communicate, data: {}}

    JsonElement[] sortAPIList = null;
    LatchWrapper lw = new LatchWrapper();
    SyncProcessQueueManager spqm = new SyncProcessQueueManager();
    ClientSetUp clientSetUp = new ClientSetUp(lw);
    SyncProcessQueueObject<ByteBuf> initialQueue = spqm.findSyncQueue("InitialQueue");
    EntryChannelUtils utils = new EntryChannelUtils(spqm, lw, initialQueue);
    int settingFlag = 0;
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (BackPressure.getBackPressureFlag() == 1) {
            ctx.channel().writeAndFlush(responseToByteBuf(new ResponseFormat("flowControl", 1)));
            spqm.clearInitialQueue();
        }

        RequestFormat request = byteBufToRequest((ByteBuf) msg);
        if (request == null) {
            return;
        }
        try {
            String endpoint = request.getHeader().get("endpoint").getAsString();
            if (endpoint.equals("setting")) {

                sortAPIList = utils.settingEntryPoint(request, clientSetUp, settingFlag);
                settingFlag = 1;
                utils.setAPIKey(sortAPIList);
                utils.queueSet(sortAPIList);

            } else if (endpoint.equals("communicate")) {

                initialQueue.enQueue((ByteBuf) msg, ctx);

                if (sortAPIList != null) {

                    utils.communicateClient(sortAPIList, 0, ctx);

                } else {
                    throw new Exception("Error: Setting is needed");
                }
            } else {
                throw new Exception("Error: Header not matched: { header: setting | communicate }");
            }

        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
