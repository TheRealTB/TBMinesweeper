package minesweeper;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MinesweeperGUI {
    private static int currentDifficulty = 2; // Default Medium

    public static void main(String[] args) {
        SoundGenerator.ensureSoundFilesExist();
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 850);
            frame.setLayout(new BorderLayout());

            Minesweeper game = new Minesweeper(currentDifficulty);
            Grid gridPanel = new Grid(game);

            // --- TOP CONTROL PANEL ---
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(new Color(200, 200, 200));
            controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

            Font headerFont = new Font("Arial", Font.BOLD, 15);

            // Flag Counter Label
            JLabel flagLabel = new JLabel("Flags: " + game.getNumMines());
            flagLabel.setFont(headerFont);
            gridPanel.setFlagLabel(flagLabel); 

            // Timer Label
            JLabel timerLabel = new JLabel("Time: 0");
            timerLabel.setFont(headerFont);
            gridPanel.setTimerLabel(timerLabel);

            // High Score Label
            JLabel highScoreLabel = new JLabel();
            highScoreLabel.setFont(headerFont);
            
            Runnable updateHighScoreDisplay = () -> {
                int bestTime = HighScoreManager.getHighScore(currentDifficulty);
                highScoreLabel.setText("Best: " + (bestTime == -1 ? "N/A" : bestTime + "s"));
            };
            updateHighScoreDisplay.run();
            gridPanel.setHighScoreUpdater(updateHighScoreDisplay);

            // Difficulty Label
            JLabel diffLabel = new JLabel("Difficulty:");
            diffLabel.setFont(headerFont);

            // Difficulty Dropdown
            String[] difficulties = {"Easy", "Medium", "Hard"};
            JComboBox<String> difficultyDropdown = new JComboBox<>(difficulties);
            difficultyDropdown.setSelectedIndex(currentDifficulty - 1); 
            difficultyDropdown.setFont(headerFont);

            // Center container to cleanly hot-swap grid panels when changing difficulty
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(gridPanel, BorderLayout.CENTER);

            // Handle Difficulty Change
            difficultyDropdown.addActionListener(e -> {
                int selectedIndex = difficultyDropdown.getSelectedIndex();
                int newDifficulty = selectedIndex + 1; 

                currentDifficulty = newDifficulty; 

                gridPanel.stopTimer();
                centerPanel.removeAll();
                
                Minesweeper newGame = new Minesweeper(currentDifficulty);
                Grid newGrid = new Grid(newGame);
                newGrid.setFlagLabel(flagLabel);
                newGrid.setTimerLabel(timerLabel);
                newGrid.setHighScoreUpdater(updateHighScoreDisplay);
                
                flagLabel.setText("Flags: " + newGame.getNumMines());
                timerLabel.setText("Time: 0");
                updateHighScoreDisplay.run();
                
                centerPanel.add(newGrid, BorderLayout.CENTER);
                centerPanel.revalidate();
                centerPanel.repaint();
                
                newGrid.requestFocusInWindow();
            });

            // Add components with clean vertical separators
            controlPanel.add(flagLabel);
            controlPanel.add(new JLabel(" | "));
            controlPanel.add(timerLabel);
            controlPanel.add(new JLabel(" | "));
            controlPanel.add(highScoreLabel);
            controlPanel.add(new JLabel(" | "));
            controlPanel.add(diffLabel);
            controlPanel.add(difficultyDropdown);

            // Add components to JFrame
            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(centerPanel, BorderLayout.CENTER);

            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
}

class Grid extends JPanel implements MouseListener {
    private Minesweeper game;
    private int diff; 
    boolean[][] revealed;
    boolean[][] flagged;  
    boolean gameOver = false;
    boolean gameWon = false; 
    boolean firstClick = true;
    
    private JLabel flagLabel; 
    private JLabel timerLabel;
    private Runnable highScoreUpdater;
    private Timer gameTimer;
    private int secondsElapsed = 0;

    // Tracking for middle-click hold / visual preview feedback
    private int pressedRow = -1;
    private int pressedCol = -1;
    private boolean isMiddlePressed = false;

    public Grid(Minesweeper game) {
        this.game = game;
        this.diff = game.getSize();
        this.revealed = new boolean[diff][diff];
        this.flagged = new boolean[diff][diff];

        gameTimer = new Timer(1000, e -> {
            secondsElapsed++;
            if (timerLabel != null) {
                timerLabel.setText("Time: " + secondsElapsed);
            }
        });

        this.addMouseListener(this);
        this.setFocusable(true);
    }

    public void setFlagLabel(JLabel label) {
        this.flagLabel = label;
    }

    public void setTimerLabel(JLabel label) {
        this.timerLabel = label;
    }

    public void setHighScoreUpdater(Runnable updater) {
        this.highScoreUpdater = updater;
    }

    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void updateFlagCounter() {
        if (flagLabel == null) return;
        int flagsPlaced = 0;
        for (int r = 0; r < diff; r++) {
            for (int c = 0; c < diff; c++) {
                if (flagged[r][c]) flagsPlaced++;
            }
        }
        int remainingFlags = game.getNumMines() - flagsPlaced;
        flagLabel.setText("Flags: " + remainingFlags);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        this.setBackground(new Color(0, 0, 0));

        Color grassLight = new Color(0xA2D149);
        Color grassDark  = new Color(0xAAD751);
        Color revealedBg = new Color(0xE5C298); 
        Color chordHighlight = new Color(0xDCDCDC); // Soft off-white preview highlight

        int cellWidth = getWidth() / diff;
        int cellHeight = getHeight() / diff;

        int[][] tileNumbers = game.getGrid(); 

        for (int row = 0; row < diff; row++) {
            for (int col = 0; col < diff; col++) {
                int x = col * cellWidth;
                int y = row * cellHeight;

                if (!revealed[row][col]) {
                    boolean isHighlighted = false;
                    if (isMiddlePressed && pressedRow >= 0 && pressedCol >= 0) {
                        if (Math.abs(row - pressedRow) <= 1 && Math.abs(col - pressedCol) <= 1) {
                            if (!flagged[row][col]) { 
                                isHighlighted = true;
                            }
                        }
                    }

                    if (isHighlighted) {
                        g2d.setColor(chordHighlight); 
                    } else {
                        g2d.setColor(((row + col) % 2 == 0) ? grassLight : grassDark);
                    }
                    g2d.fillRect(x, y, cellWidth, cellHeight);

                    if (flagged[row][col] || (gameWon && tileNumbers[row][col] == 9)) {
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(x + cellWidth / 3, y + cellHeight * 3 / 4, x + cellWidth / 3, y + cellHeight / 4);

                        g2d.setColor(Color.RED);
                        int[] xPoints = {x + cellWidth / 3, x + cellWidth * 3 / 4, x + cellWidth / 3};
                        int[] yPoints = {y + cellHeight / 4, y + cellHeight * 3 / 8, y + cellHeight / 2};
                        g2d.fillPolygon(xPoints, yPoints, 3);
                    }

                } else {
                    if (gameOver && tileNumbers[row][col] == 9) {
                        g2d.setColor(new Color(255, 100, 100)); 
                    } else if (gameWon) {
                        g2d.setColor(new Color(150, 255, 150)); 
                    } else {
                        g2d.setColor(revealedBg);
                    }
                    g2d.fillRect(x, y, cellWidth, cellHeight);

                    int value = tileNumbers[row][col];

                    if (value == 9) {
                        g2d.setColor(Color.BLACK);
                        int bombSize = cellWidth / 2;
                        int bombX = x + (cellWidth - bombSize) / 2;
                        int bombY = y + (cellHeight - bombSize) / 2;
                        g2d.fillOval(bombX, bombY, bombSize, bombSize);

                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(x + cellWidth / 2, bombY, x + cellWidth * 3 / 4, y + cellHeight / 4);

                        g2d.setColor(Color.WHITE);
                        g2d.fillOval(bombX + bombSize / 4, bombY + bombSize / 4, bombSize / 5, bombSize / 5);

                    } else if (value > 0) {
                        g2d.setColor(getNumberColor(value));
                        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, cellWidth / 2)));
                        g2d.drawString(String.valueOf(value), x + (cellWidth / 3), y + (cellHeight * 2 / 3));
                    }
                }

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x, y, cellWidth, cellHeight);
            }
        }
    }

    private Color getNumberColor(int num) {
        switch (num) {
            case 1: return Color.BLUE;
            case 2: return new Color(0, 128, 0);          
            case 3: return Color.RED;                     
            case 4: return new Color(0, 0, 128);          
            case 5: return new Color(128, 0, 0);          
            case 6: return new Color(0, 128, 128);        
            case 7: return Color.BLACK;                   
            case 8: return Color.GRAY;                    
            default: return Color.DARK_GRAY;
        }
    }

    private void revealEmptyTiles(int r, int c) {
        int[][] tileNumbers = game.getGrid();

        if (r < 0 || r >= diff || c < 0 || c >= diff) return;
        if (revealed[r][c] || flagged[r][c]) return;

        revealed[r][c] = true;

        if (tileNumbers[r][c] == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr != 0 || dc != 0) {
                        revealEmptyTiles(r + dr, c + dc);
                    }
                }
            }
        }
    }

    private boolean checkWinCondition() {
        int revealedCount = 0;
        int totalCells = diff * diff;
        int totalMines = game.getNumMines();

        for (int r = 0; r < diff; r++) {
            for (int c = 0; c < diff; c++) {
                if (revealed[r][c]) {
                    revealedCount++;
                }
            }
        }
        return revealedCount == (totalCells - totalMines);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver || gameWon) return;

        int cellWidth = getWidth() / diff;
        int cellHeight = getHeight() / diff;

        int col = e.getX() / cellWidth;
        int row = e.getY() / cellHeight;

        if (row >= 0 && row < diff && col >= 0 && col < diff) {
            int[][] tileNumbers = game.getGrid();
            
            // Right Click: Place/Remove Flag
            if (SwingUtilities.isRightMouseButton(e)) {
                if (!revealed[row][col]) {
                    flagged[row][col] = !flagged[row][col]; 
                    SoundManager.playSound("flag.wav");
                    updateFlagCounter(); 
                }
            } 
            // Middle Click Hold: Preview Highlight
            else if (SwingUtilities.isMiddleMouseButton(e) || e.getButton() == MouseEvent.BUTTON2) {
                if (revealed[row][col] && tileNumbers[row][col] > 0 && tileNumbers[row][col] < 9) {
                    pressedRow = row;
                    pressedCol = col;
                    isMiddlePressed = true;
                    repaint(); 
                    return;
                }
            }
            // Left Click: Reveal Tile
            else if (SwingUtilities.isLeftMouseButton(e)) {
                if (!flagged[row][col]) {
                    if (firstClick) {
                        firstClick = false;
                        game.initializeBoard(row, col); 
                        secondsElapsed = 0;
                        gameTimer.start(); 
                    }

                    SoundManager.playSound("click.wav");

                    if (tileNumbers[row][col] == 9) {
                        gameOver = true; 
                        gameTimer.stop(); 
                        SoundManager.playSound("explosion.wav");
                        revealed[row][col] = true;

                        for (int i = 0; i < diff; i++) {
                            for (int j = 0; j < diff; j++) {
                                if (tileNumbers[i][j] == 9) {
                                    revealed[i][j] = true;
                                    flagged[i][j] = false;
                                }
                            }
                        }
                    } else {
                        revealEmptyTiles(row, col);
                        if (checkWinCondition()) {
                            gameWon = true;
                            gameTimer.stop(); 
                            SoundManager.playSound("win.wav");
                            HighScoreManager.saveHighScore(diff == 10 ? 1 : (diff == 18 ? 2 : 3), secondsElapsed);
                            if (highScoreUpdater != null) highScoreUpdater.run();
                        }
                    }
                }
            }
            
            repaint(); 
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver || gameWon) return;

        if (isMiddlePressed && (SwingUtilities.isMiddleMouseButton(e) || e.getButton() == MouseEvent.BUTTON2)) {
            isMiddlePressed = false;

            if (pressedRow >= 0 && pressedCol >= 0 && pressedRow < diff && pressedCol < diff) {
                int[][] tileNumbers = game.getGrid();

                int adjacentFlags = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = pressedRow + dr;
                        int nc = pressedCol + dc;
                        if (nr >= 0 && nr < diff && nc >= 0 && nc < diff) {
                            if (flagged[nr][nc]) {
                                adjacentFlags++;
                            }
                        }
                    }
                }

                if (adjacentFlags == tileNumbers[pressedRow][pressedCol]) {
                    boolean hitBomb = false;

                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int nr = pressedRow + dr;
                            int nc = pressedCol + dc;
                            
                            if (nr >= 0 && nr < diff && nc >= 0 && nc < diff) {
                                if (!revealed[nr][nc] && !flagged[nr][nc]) {
                                    if (tileNumbers[nr][nc] == 9) {
                                        revealed[nr][nc] = true;
                                        hitBomb = true;
                                    } else {
                                        revealEmptyTiles(nr, nc);
                                    }
                                }
                            }
                        }
                    }

                    if (hitBomb) {
                        gameOver = true;
                        gameTimer.stop(); 
                        SoundManager.playSound("explosion.wav");
                        for (int i = 0; i < diff; i++) {
                            for (int j = 0; j < diff; j++) {
                                if (tileNumbers[i][j] == 9) {
                                    revealed[i][j] = true;
                                    flagged[i][j] = false;
                                }
                            }
                        }
                    } else {
                        if (checkWinCondition()) {
                            gameWon = true;
                            gameTimer.stop(); 
                            SoundManager.playSound("win.wav");
                            HighScoreManager.saveHighScore(diff == 10 ? 1 : (diff == 18 ? 2 : 3), secondsElapsed);
                            if (highScoreUpdater != null) highScoreUpdater.run();
                        }
                    }
                }
            }

            pressedRow = -1;
            pressedCol = -1;
            repaint();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}