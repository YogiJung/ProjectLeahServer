package APICluster.TextToSpeechAPI;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;

import static APICluster.APIClusterManager.playAudio;

public class GoogleCloudTTSAPI {
    String credential_file_route;
    private TextToSpeechClient textToSpeechClient;
    public byte[] StringToVoice(String message) {

        try {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(credential_file_route));

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            textToSpeechClient = TextToSpeechClient.create(settings);
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(message)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US") //en-US
                    .setName("en-US-Wavenet-A") //ko-KR-Wavenet-A
                    .setSsmlGender(SsmlVoiceGender.FEMALE)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16)
                    .setSampleRateHertz(20100)
                    .setVolumeGainDb(16.0)
                    .build();
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();
            playAudio(audioContents.toByteArray(), 20100);
            return audioContents.toByteArray();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }
    private byte[] amplifyPcmData(byte[] pcmData, float gain) {
        for (int i = 0; i < pcmData.length; i += 2) {

            short sample = (short) ((pcmData[i] & 0xFF) | (pcmData[i + 1] << 8));
            sample = (short) Math.min(Math.max(sample * gain, Short.MIN_VALUE), Short.MAX_VALUE);
            pcmData[i] = (byte) (sample & 0xFF);
            pcmData[i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return pcmData;
    }
    public void setCredential_file_route(String credentialFileRoute) {
        this.credential_file_route = credentialFileRoute;
    }
}
