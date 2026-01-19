package assignments.Ex3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the MyGame Logic.
 */
class MyGameTest {

    @Test
    void testIsValidMove() {
        MyGame.initLevel(1);
        int[][] map = MyGame.map;
        assertFalse(MyGame.isValid(0, 0, map));
        assertTrue(MyGame.isValid(1, 1, map));
        assertFalse(MyGame.isValid(-1, 5, map));
    }

    @Test
    void testBFSLogic() {
        MyGame.initLevel(1);
        int[][] map = MyGame.map;
        int[][] dist = MyGame.bfs(1, 1, map);

        int h = map.length;
        assertEquals(0, dist[h-1-1][1]);
        assertEquals(1, dist[h-1-1][2]);
    }

    @Test
    void testGhostHouseLogic() {
        MyGame.initLevel(1);
        int[][] map = MyGame.map;
        assertTrue(MyGame.isValid(11, 10, map));
    }
}