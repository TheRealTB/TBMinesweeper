package minesweeper;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundManager {

    // Helper method to play a sound effect safely
    public static void playSound(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (!soundFile.exists()) {
                return; // Fail silently if the audio file hasn't been added yet
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            // Prevent audio glitches from interrupting gameplay
        }
    }
}