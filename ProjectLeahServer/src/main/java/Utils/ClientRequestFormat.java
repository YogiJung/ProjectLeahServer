package Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ClientRequestFormat {
    JsonObject header = new JsonObject();
    String Protocol;
    String API_Name;
    String API_KEY;
    int order;
    int backPressureFlag = 1;

    int count;
    String data;

    public ClientRequestFormat(JsonElement API, String endpoint) {
        try {

            header.addProperty("endpoint", endpoint);

            if (this.header.get("endpoint").getAsString().equals("setting")) {
                this.API_Name = API.getAsJsonObject().get("API_Name").getAsString();
                this.API_KEY = API.getAsJsonObject().get("API_KEY").getAsString();
                this.Protocol = API.getAsJsonObject().get("Protocol").getAsString();
                this.order = API.getAsJsonObject().get("order").getAsInt();
            } else {
                this.API_Name = API.getAsJsonObject().get("API_Name").getAsString();
                this.API_KEY = API.getAsJsonObject().get("API_KEY").getAsString();
                this.Protocol = API.getAsJsonObject().get("Protocol").getAsString();
            }
        } catch(Exception e) {
            System.err.println("Client Request Error: " + e.getLocalizedMessage());
        }
    }
    public void buildInitialObject(int count, ByteBuf buf) {
        this.count = count;
        this.data = JsonParser.parseString(buf.toString(StandardCharsets.UTF_8)).getAsJsonObject().get("data").getAsString();
    }

    public JsonObject getHeader() {
        return header;
    }

    public int getOrder() {
        return order;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getAPI_Name() {
        return API_Name;
    }

    public String getProtocol() {
        return Protocol;
    }

    public String getData() {
        return data;
    }

    public int getCount() {
        return count;
    }

    public void setData(String data) {
        this.data = data;
    }
    public void setAPI_Name(String API_Name) {
        this.API_Name = API_Name;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }
    public void countUp() {
        this.count += 1;
    }

    public void setBackPressureFlag(int backPressureFlag) {
        this.backPressureFlag = backPressureFlag;
    }

    public int getBackPressureFlag() {
        return this.backPressureFlag;
    }
}
