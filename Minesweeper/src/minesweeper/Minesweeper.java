package minesweeper;

@SuppressWarnings("javadoc")
public class Minesweeper {
    private int[][] grid;
    private int numMines;
    private boolean isInitialized = false;
    
    public Minesweeper(int difficulty) {
        switch(difficulty) {
            case 1:
                grid = new int[10][10];
                numMines = 10;
                break;
            case 2:
                grid = new int[18][18];
                numMines = 40;
                break;
            case 3:
                grid = new int[24][24];
                numMines = 99;
                break;
            default:
                grid = new int[18][18];
                numMines = 40;
        }
    }
    
    public void initializeBoard(int safeRow, int safeCol) {
        if (!isInitialized) {
            placeMines(safeRow, safeCol);
            numSetup();
            isInitialized = true;
        }
    }

    private void placeMines(int safeRow, int safeCol) {
        int minesPlaced = 0;
        int rows = grid.length;
        int cols = grid[0].length;

        while (minesPlaced != numMines) {
            int randRow = (int)(Math.random() * rows);
            int randCol = (int)(Math.random() * cols);
            
            // 3x3 safe zone around first click
            boolean isInsideSafeZone = (Math.abs(randRow - safeRow) <= 1 && Math.abs(randCol - safeCol) <= 1);
            
            if (grid[randRow][randCol] != 9 && !isInsideSafeZone) {
                grid[randRow][randCol] = 9;
                minesPlaced++;
            }
        }
    }
    
    private void numSetup() {
        int rows = grid.length;
        int cols = grid[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] == 9) continue;

                int mineCount = 0;

                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;

                        int nr = row + dr;
                        int nc = col + dc;

                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                            if (grid[nr][nc] == 9) {
                                mineCount++;
                            }
                        }
                    }
                }
                grid[row][col] = mineCount;
            }
        }
    }
    
    public int[][] getGrid() {
        return grid;
    }
    
    public int getSize() {
        return grid.length;
    }

    public int getNumMines() {
        return numMines;
    }
    
    @Override
    public String toString() {
        String r = "";
        for (int[] row : grid) {
            for (int tile : row) {
                r += tile + "  ";
            }
            r += "\n\n";
        }
        return r;
    }
}