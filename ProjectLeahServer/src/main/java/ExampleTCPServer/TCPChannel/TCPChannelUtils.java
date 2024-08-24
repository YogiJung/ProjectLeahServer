package ExampleTCPServer.TCPChannel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TCPChannelUtils {
    public static byte[] combineJsonData(ByteBuf rawBytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<String> jsonObjectList = new ArrayList<>();
        JsonObject header = null;
        ByteBuf delimiter = Unpooled.copiedBuffer(new byte[] {0x03});
        int startIndex = 0;
        int endIndex;
        while (rawBytes.isReadable()) {
            if (rawBytes.readByte() == delimiter.getByte(0)) {
                endIndex = rawBytes.readerIndex();
                int length = endIndex - startIndex - 1;

                if (length > 0) {
                    rawBytes.readerIndex(startIndex);
                    byte[] extractedData = new byte[length];
                    rawBytes.readBytes(extractedData);
                    String jsonString = new String(extractedData, StandardCharsets.UTF_8);
                    jsonObjectList.add(jsonString);
                }
                startIndex = rawBytes.readerIndex();
            }
        }

        for (String jsonString : jsonObjectList) {
            if (!jsonString.trim().endsWith("}")) {
                System.out.println("Not end with }");
                continue;
            }

            jsonString = removePaddingInBase64(jsonString);

            JsonObject json;
            try {
                json = JsonParser.parseString(jsonString).getAsJsonObject();
            } catch (Exception e) {
                continue;
            }

            if (header == null) {
                header = json.getAsJsonObject("header");
            }

            String encodedData = json.get("data").getAsString();
            try {
                byte[] dataBytes = Base64.getDecoder().decode(encodedData);
                outputStream.write(dataBytes);
            } catch (IllegalArgumentException | IOException e) {
                System.out.println("Error decoding Base64: " + e.getMessage());
            }
        }

        return outputStream.toByteArray();
    }
    public static String removePaddingInBase64(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        String base64Data = jsonObject.get("data").getAsString();

        base64Data = base64Data.replaceAll("=+$", "");

        jsonObject.addProperty("data", base64Data);

        return jsonObject.toString();
    }
}
