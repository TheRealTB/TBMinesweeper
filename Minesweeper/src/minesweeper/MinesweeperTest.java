package minesweeper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
@SuppressWarnings("javadoc")
class MinesweeperTest {
    private Minesweeper testEz;
    private Minesweeper testMed;
    private Minesweeper testHard;
    private Minesweeper testDef;
    
    
    @BeforeEach
    void setUp() throws Exception {
        testEz = new Minesweeper(1);
        testMed = new Minesweeper(2);
        testHard = new Minesweeper(3);
        testDef = new Minesweeper(4);
    }

    @Test
    public void testToString() {
        System.out.println(testEz.toString());
        System.out.println(testMed.toString());
        System.out.println(testHard.toString());
        System.out.println(testDef.toString());
    }

}