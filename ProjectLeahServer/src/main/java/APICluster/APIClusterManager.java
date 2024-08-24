package APICluster;

import APICluster.ChatAPI.ChatGPTAPI;
import APICluster.SpeechToTextAPI.GoogleCloudSTTAPI;
import APICluster.TextToSpeechAPI.GoogleCloudTTSAPI;
import APICluster.TextToSpeechAPI.PlayHTAPI;
import APICluster.TextToSpeechAPI.ReplicaStudioAPI;
import QueueManagers.SyncProcessQueueManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class APIClusterManager {
    public static List<String> APICluster = new ArrayList<>(Arrays.asList("ChatGPTAPI", "GoogleSTTAPI", "GoogleTTSAPI", "PlayHTAPI", "ReplicaStudioAPI"));
    public SyncProcessQueueManager spqm = new SyncProcessQueueManager();

    public ChatGPTAPI chatGPTAPI = new ChatGPTAPI(spqm.generateSyncQueue("ChatGPTQueue", 1));
    public GoogleCloudSTTAPI googleCloudSTTAPI = new GoogleCloudSTTAPI();
    public GoogleCloudTTSAPI googleCloudTTSAPI = new GoogleCloudTTSAPI();
    public PlayHTAPI playHTAPI = new PlayHTAPI(spqm.generateSyncQueue("playHTQueue", 0));
    public ReplicaStudioAPI replicaStudioAPI = new ReplicaStudioAPI(spqm.generateSyncQueue("replicaStudioQueue", 0));


    public static String chatGPTDataHandler(String data) {
        try {
            JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            String responseData = jsonObject.get("choices").getAsJsonArray().get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
            return responseData;
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return null;
        }
    }

    public static void playAudio(byte[] audioBytes, int sampleRate) throws LineUnavailableException, UnsupportedAudioFileException, IOException {

        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);

        sourceDataLine.open(audioFormat);
        sourceDataLine.start();


        sourceDataLine.write(audioBytes, 0, audioBytes.length);


        sourceDataLine.drain();
        sourceDataLine.close();
    }
    public static void playAudio2(byte[] audioBytes) throws Exception {
        try (InputStream is = new ByteArrayInputStream(audioBytes)) {
            AdvancedPlayer player = new AdvancedPlayer(is);
            player.play();
        }
    }

    public static byte[] urlToByteArray(String audioUrl) {
        System.out.println("Audio URL : " + audioUrl);
        try (InputStream in = new URL(audioUrl).openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] urlToByteArray2(String audioUrl) {
        try {
            URL url = new URL(audioUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                System.out.println(header.getKey() + ": " + header.getValue());
            }

            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                return byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
