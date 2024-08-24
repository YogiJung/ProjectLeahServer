package APICluster.TextToSpeechAPI;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import QueueManagers.SyncProcessQueueObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static APICluster.APIClusterManager.*;

public class PlayHTAPI {
    private final String API_URL = "https://play.ht/api/v2/tts";
    private final String USER_ID = "";
    private final String SECRET_KEY = "";
    private final String CLONE_VOICE_ID = "s3://voice-cloning-zero-shot/04c96b93-6532-4e87-b57d-fe94b36dca31/original/manifest.json";
    SyncProcessQueueObject<byte[]> playHTQueue;

    public PlayHTAPI(SyncProcessQueueObject<byte[]> playHTQueue) {
        this.playHTQueue = playHTQueue;
    }

    public HttpRequest makeRequest(String content) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("text", content);
            requestBody.addProperty("voice", CLONE_VOICE_ID);
            requestBody.addProperty("voice_engine", "PlayHT2.0");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", SECRET_KEY)
                    .header("X-User-Id", USER_ID)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendRequest(HttpRequest request) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String audioUrl = null;
            if (response.statusCode() == 200) {
                System.out.println(response.body());
                String event = response.body();
                int eventIndex = event.indexOf("event: completed");
                int dataIndex = event.indexOf("data: {", eventIndex);
                int endIndex = event.indexOf("}", dataIndex) + 1;

                if (dataIndex != -1 && endIndex != -1) {
                    String dataLine = event.substring(dataIndex + 6, endIndex);
                    System.out.println("dataLine: " + dataLine);
                    JsonObject jsonResponse = JsonParser.parseString(dataLine).getAsJsonObject();
                    if (jsonResponse.has("url")) {
                        audioUrl = jsonResponse.get("url").getAsString();
                    }
                }

                if (audioUrl != null) {
                    playHTQueue.putQueue(urlToByteArray(audioUrl));
                } else {
                    System.out.println("Error: Audio URL not found in response");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
