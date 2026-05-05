package tn.esprit.projet.utils;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Offline Speech-to-Text using Vosk (no API key, no internet required).
 *
 * SETUP (one-time):
 * 1. Download the English model (~40MB):
 *    https://alphacephei.com/vosk/models  → "vosk-model-small-en-us-0.15"
 * 2. Extract it into your project folder so the path becomes:
 *    projet_java_desktop.../vosk-model-small-en-us-0.15/
 */
public class SpeechToTextService {

    // Path to the extracted Vosk model folder (relative to project root)
    private static final String MODEL_PATH = "vosk-model-small-en-us-0.15/vosk-model-small-en-us-0.15";

    /**
     * Records audio from the microphone for a specific duration (in seconds).
     */
    public static byte[] recordAudio(int seconds) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Microphone not supported on this system.");
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long endTime = System.currentTimeMillis() + (seconds * 1000L);

        System.out.println("🎙️ Recording for " + seconds + " seconds...");
        while (System.currentTimeMillis() < endTime) {
            int count = line.read(buffer, 0, buffer.length);
            if (count > 0) rawOut.write(buffer, 0, count);
        }
        line.stop();
        line.close();
        System.out.println("✅ Recording done.");

        return rawOut.toByteArray();
    }

    /**
     * Converts raw PCM audio bytes to text using Vosk (completely offline).
     */
    public static String convertAudioToText(byte[] audioData) throws Exception {
        File modelDir = new File(MODEL_PATH);
        if (!modelDir.exists()) {
            throw new Exception(
                "Vosk model not found at: " + modelDir.getAbsolutePath() + "\n" +
                "Download it from: https://alphacephei.com/vosk/models\n" +
                "→ Choose 'vosk-model-small-en-us-0.15' and extract it in your project folder."
            );
        }

        try (Model model = new Model(MODEL_PATH);
             Recognizer recognizer = new Recognizer(model, 16000)) {

            recognizer.acceptWaveForm(audioData, audioData.length);
            String result = recognizer.getFinalResult();

            // Vosk returns JSON like: {"text": "hello world"}
            // Extract the text value
            if (result != null && result.contains("\"text\"")) {
                int start = result.indexOf("\"text\"") + 9;
                int end = result.lastIndexOf("\"");
                if (start > 0 && end > start) {
                    return result.substring(start, end).trim();
                }
            }
            return "";
        }
    }
}
