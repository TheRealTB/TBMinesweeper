package minesweeper;

import javax.sound.sampled.*;
import java.io.File;

public class SoundGenerator {

    public static void ensureSoundFilesExist() {
        createToneWavIfNotExists("click.wav", 800, 40);   // Short high blip for clicks
        createToneWavIfNotExists("flag.wav", 450, 70);    // Pitched down lower for flags!
        createNoiseWavIfNotExists("explosion.wav", 600);  // Noise burst for explosions
        createMelodyWavIfNotExists("win.wav");          // Triumphant chime for winning
    }

    private static void createToneWavIfNotExists(String filename, double freq, int durationMs) {
        File f = new File(filename);
        if (f.exists()) return;

        try {
            int sampleRate = 8000;
            int numSamples = (sampleRate * durationMs) / 1000;
            byte[] buf = new byte[numSamples * 2];
            for (int i = 0; i < numSamples; i++) {
                double angle = i / (double) sampleRate * freq * 2.0 * Math.PI;
                double envelope = 1.0 - ((double) i / numSamples); // Fade out
                short val = (short) (Math.sin(angle) * 32767 * envelope);
                buf[2 * i] = (byte) (val & 0xff);
                buf[2 * i + 1] = (byte) ((val >> 8) & 0xff);
            }
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (AudioInputStream ais = new AudioInputStream(
                    new java.io.ByteArrayInputStream(buf), format, numSamples)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createNoiseWavIfNotExists(String filename, int durationMs) {
        File f = new File(filename);
        if (f.exists()) return;

        try {
            int sampleRate = 8000;
            int numSamples = (sampleRate * durationMs) / 1000;
            byte[] buf = new byte[numSamples * 2];
            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < numSamples; i++) {
                double envelope = 1.0 - ((double) i / numSamples);
                short val = (short) ((rand.nextInt(65536) - 32768) * envelope);
                buf[2 * i] = (byte) (val & 0xff);
                buf[2 * i + 1] = (byte) ((val >> 8) & 0xff);
            }
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (AudioInputStream ais = new AudioInputStream(
                    new java.io.ByteArrayInputStream(buf), format, numSamples)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createMelodyWavIfNotExists(String filename) {
        File f = new File(filename);
        if (f.exists()) return;

        try {
            int sampleRate = 8000;
            int noteDurationMs = 120;
            double[] freqs = {523.25, 659.25, 783.99, 1046.50}; // C5, E5, G5, C6 arpeggio
            int numSamplesPerNote = (sampleRate * noteDurationMs) / 1000;
            int totalSamples = numSamplesPerNote * freqs.length;
            byte[] buf = new byte[totalSamples * 2];

            int sampleIndex = 0;
            for (double freq : freqs) {
                for (int i = 0; i < numSamplesPerNote; i++) {
                    double angle = i / (double) sampleRate * freq * 2.0 * Math.PI;
                    double envelope = 1.0 - ((double) i / numSamplesPerNote);
                    short val = (short) (Math.sin(angle) * 32767 * envelope);
                    buf[2 * sampleIndex] = (byte) (val & 0xff);
                    buf[2 * sampleIndex + 1] = (byte) ((val >> 8) & 0xff);
                    sampleIndex++;
                }
            }
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (AudioInputStream ais = new AudioInputStream(
                    new java.io.ByteArrayInputStream(buf), format, totalSamples)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}