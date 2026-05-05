import tn.esprit.projet.utils.SpeechToTextService;

/**
 * Quick test to verify speech-to-text recording works
 */
public class TEST_SPEECH_RECORDING {
    public static void main(String[] args) {
        System.out.println("🎤 Testing Speech-to-Text Recording...\n");
        
        try {
            System.out.println("Recording for 5 seconds... Speak now!");
            byte[] audioBytes = SpeechToTextService.recordAudio(5);
            
            if (audioBytes == null || audioBytes.length == 0) {
                System.err.println("❌ Failed to record audio");
                return;
            }
            
            System.out.println("✅ Recording complete. Audio size: " + audioBytes.length + " bytes");
            System.out.println("\nConverting to text...");
            
            String transcript = SpeechToTextService.convertAudioToText(audioBytes);
            
            if (transcript != null && !transcript.isEmpty()) {
                System.out.println("✅ SUCCESS! Transcribed text:");
                System.out.println("\"" + transcript + "\"");
            } else {
                System.err.println("❌ Failed to transcribe audio");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
