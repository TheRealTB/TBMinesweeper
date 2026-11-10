package minesweeper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class HighScoreManager {
    private static final String FILE_NAME = "minesweeper_scores.txt";

    // Loads the high score for a given difficulty (1 = Easy, 2 = Medium, 3 = Hard)
    public static int getHighScore(int difficulty) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return -1; // -1 represents N/A

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int diff = Integer.parseInt(parts[0].trim());
                    int time = Integer.parseInt(parts[1].trim());
                    if (diff == difficulty) {
                        return time;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore formatting errors and fallback to N/A
        }
        return -1;
    }

    // Saves a new time if it's better (lower) than the existing record
    public static void saveHighScore(int difficulty, int newTime) {
        int currentBest = getHighScore(difficulty);
        
        // If there's no previous score, or the new time is lower, update it
        if (currentBest == -1 || newTime < currentBest) {
            int easyScore = (difficulty == 1) ? newTime : getHighScore(1);
            int medScore  = (difficulty == 2) ? newTime : getHighScore(2);
            int hardScore = (difficulty == 3) ? newTime : getHighScore(3);

            try (FileWriter writer = new FileWriter(FILE_NAME, false)) {
                writer.write("1:" + (easyScore > 0 ? easyScore : -1) + "\n");
                writer.write("2:" + (medScore > 0 ? medScore : -1) + "\n");
                writer.write("3:" + (hardScore > 0 ? hardScore : -1) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}