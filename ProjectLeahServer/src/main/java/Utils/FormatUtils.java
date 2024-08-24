package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class FormatUtils {
    public static ByteBuf clientRequestToByteBuf(ClientRequestFormat clientRequest) {
        try {
            Gson gson = new Gson();
            byte delimiter = 0x03;
            String jsonData = removeNewLine(gson.toJson(clientRequest));
            ByteBuf buf = Unpooled.copiedBuffer(jsonData, StandardCharsets.UTF_8);
            buf.writeByte(delimiter);
            return buf;

        } catch (Exception e) {
            System.err.println("Error in ClientRequest to ByteBuf" + e.getLocalizedMessage());
            return null;
        }
    }

    public static ClientRequestFormat byteBufToClientRequest(ByteBuf buf) {
        try {

            Gson gson = new Gson();
            String bufString = buf.toString(StandardCharsets.UTF_8);
            return gson.fromJson(bufString, ClientRequestFormat.class);

        } catch (Exception e) {
            System.err.println("Error in ByteBuf To ClientRequest: " + e.getLocalizedMessage());
            return null;
        } finally {
            buf.release();
        }
    }
    public static ByteBuf responseToByteBuf(ResponseFormat response) {
        try {
            byte delimiter = 0x03;
            Gson gson = new Gson();
            String jsonData = gson.toJson(response);
            ByteBuf responseByteBuf = Unpooled.copiedBuffer(jsonData, StandardCharsets.UTF_8);
            responseByteBuf.writeByte(delimiter);
            return responseByteBuf;
        } catch (Exception e) {
            return null;
        }
    }
    public static RequestFormat byteBufToRequest(ByteBuf buf) {
        try {
            String jsonString = buf.toString(StandardCharsets.UTF_8);
            if (!jsonString.endsWith("}")) {
                return null;
            }

            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            RequestFormat request = new RequestFormat(jsonObject);
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            buf.release();
        }
    }
    public static String removeNewLine(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\n", "");
    }
}
