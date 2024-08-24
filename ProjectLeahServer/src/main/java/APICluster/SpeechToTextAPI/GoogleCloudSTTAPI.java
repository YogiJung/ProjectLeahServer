package APICluster.SpeechToTextAPI;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class GoogleCloudSTTAPI {
    int samplingRate = 44100;
    String credential_file_route;

    public String streamRecognize(byte[] audioData) throws Exception {
        byte[] wavData = createWavHeader(audioData, samplingRate, 1, 16);

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credential_file_route));
        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();


        try (SpeechClient speechClient = SpeechClient.create(settings)) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-US") //en-US // ko-KR
                    .setSampleRateHertz(samplingRate)
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(wavData))
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);

            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                transcript.append(alternative.getTranscript());
            }
            return transcript.toString();
        } catch (Exception e) {
            System.out.println("Error processing audio data: " + e.getMessage());
            return null;
        }
    }

    public void setCredential_file_route(String credential_file_route) {
        this.credential_file_route = credential_file_route;
    }
    public static byte[] createWavHeader(byte[] audioData, int sampleRate, int numChannels, int bitsPerSample) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeString(baos, "RIFF");
            writeInt(baos, 36 + audioData.length);
            writeString(baos, "WAVE");

            writeString(baos, "fmt ");
            writeInt(baos, 16);
            writeShort(baos, (short) 1);
            writeShort(baos, (short) numChannels);
            writeInt(baos, sampleRate);
            writeInt(baos, sampleRate * numChannels * bitsPerSample / 8);
            writeShort(baos, (short) (numChannels * bitsPerSample / 8));
            writeShort(baos, (short) bitsPerSample);

            writeString(baos, "data");
            writeInt(baos, audioData.length);
            baos.write(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private static void writeInt(ByteArrayOutputStream baos, int value) throws IOException {
        baos.write((value >> 0) & 0xFF);
        baos.write((value >> 8) & 0xFF);
        baos.write((value >> 16) & 0xFF);
        baos.write((value >> 24) & 0xFF);
    }

    private static void writeShort(ByteArrayOutputStream baos, short value) throws IOException {
        baos.write((value >> 0) & 0xFF);
        baos.write((value >> 8) & 0xFF);
    }

    private static void writeString(ByteArrayOutputStream baos, String value) throws IOException {
        for (char c : value.toCharArray()) {
            baos.write((byte) (c & 0xFF));
        }
    }
}




