package Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

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

public class RequestFormat {
    JsonObject header;
    int personalServerSetUpFlag;
    JsonArray APIList;
    int TCP_PORT;
    int UDP_PORT;


    public RequestFormat(JsonObject jsonObject) {
        this.header = jsonObject.get("header").getAsJsonObject();
        if (header.get("endpoint").getAsString().equals("setting")) {
            this.personalServerSetUpFlag = jsonObject.get("personalServerSetUpFlag").getAsInt();
            this.APIList = jsonObject.getAsJsonArray("APIList");
            this.TCP_PORT = jsonObject.get("TCP_PORT").getAsInt();
            this.UDP_PORT = jsonObject.get("UDP_PORT").getAsInt();
        } else {

        }
    }

    public JsonObject getHeader() {
        return header;
    }

    public int getPersonalServerSetUpFlag() {
        return personalServerSetUpFlag;
    }

    public int getTCP_PORT() {
        return TCP_PORT;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public JsonArray getAPIList() {
        return APIList;
    }
}
