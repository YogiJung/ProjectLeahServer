package APICluster.ChatAPI;

import APICluster.ChatAPI.Memories.ChatMemories;
import QueueManagers.SyncProcessQueueObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.github.cdimascio.dotenv.Dotenv;

import static APICluster.APIClusterManager.chatGPTDataHandler;

public class ChatGPTAPI {
    Dotenv dotenv = Dotenv.load();
    SyncProcessQueueObject chatGPTQueue;
    ChatMemories chatMemories;
    String uri = dotenv.get("MONGO_URI");
    String database = "Memories";
    String collection = "systemSummaries";
    public ChatGPTAPI(SyncProcessQueueObject chatGPTQueue) {
        this.chatGPTQueue = chatGPTQueue;
        this.chatMemories = new ChatMemories(uri, database, collection);
    }

    String API_KEY;
    int flag = 0;
    public HttpRequest makeRequest(String content) {
        String API_URL = "https://api.openai.com/v1/completions";
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);
        } catch(Exception e) {
            e.printStackTrace();
        }

        JsonArray messages = chatMemories.makeChatMemories(content, flag);

        this.flag = 1;
        JsonObject payloadObj = new JsonObject();
        payloadObj.addProperty("model", "gpt-3.5-turbo");
        payloadObj.add("messages", messages);
        payloadObj.addProperty("temperature", 0.7);
        payloadObj.addProperty("max_tokens", 60);

        String payload = payloadObj.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        return request;
    }

    public void sendRequest(HttpRequest request) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::processStream);

        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }


    private void processStream(InputStream inputStream) {
        new Thread(() -> {
            try (inputStream) {
                int n;
                byte[] buffer = new byte[1024];
                while ((n = inputStream.read(buffer)) != -1) {
                    String responsePart = new String(buffer, 0, n);
                    System.out.println(responsePart);
                    String data = chatGPTDataHandler(responsePart);
                    chatMemories.addMemory(data);
                    chatGPTQueue.putQueue(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setAPIKey(String API_KEY) {
        this.API_KEY = API_KEY;
    }
}
