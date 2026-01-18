package assignments.Ex3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapTest {
    private static final int EMPTY = 0;
    private static final int WALL = -1;
    private static final int TARGET = 1;

    /**
     * Tests basic map creation, get/set pixel, and boundaries.
     */

    @Test
    void testBasics() {
        Map map = new Map(10, 10, EMPTY);

        // Check dimensions
        assertEquals(10, map.getWidth());
        assertEquals(10, map.getHeight());

        // Test Set/Get
        map.setPixel(5, 5, TARGET);
        assertEquals(TARGET, map.getPixel(5, 5));
        assertEquals(EMPTY, map.getPixel(0, 0));

        // Test isInside
        assertTrue(map.isInside(new Index2D(0, 0)));
        assertTrue(map.isInside(new Index2D(9, 9)));
        assertFalse(map.isInside(new Index2D(10, 10)));
        assertFalse(map.isInside(new Index2D(-1, 0)));
    }

    /**
     * Tests the Shortest Path (BFS) in a standard non-cyclic scenario.
     */
    @Test
    void testShortestPathNormal() {
        int[][] data = {
                {0, 0, 0, 0},
                {0, -1, -1, 0},
                {0, -1, 0, 0},
                {0, 0, 0, 0}
        };
        Map map = new Map(data);
        map.setCyclic(false);

        Pixel2D start = new Index2D(0, 0);
        Pixel2D end = new Index2D(2, 2);
        Pixel2D[] path = map.shortestPath(start, end, WALL);
        assertNotNull(path, "Path should not be null");

        assertEquals(start, path[0]);
        assertEquals(end, path[path.length - 1]);
        assertTrue(path.length >= 5);
    }

    /**
     * Tests the Shortest Path (BFS) specifically checking the Cyclic (Wrap-around) logic.
     * This ensures the Pacman can go through walls to the other side.
     */
    @Test
    void testShortestPathCyclic() {
        Map map = new Map(5, 5, EMPTY);
        map.setCyclic(true);

        Pixel2D start = new Index2D(0, 2);
        Pixel2D end = new Index2D(4, 2);
        Pixel2D[] path = map.shortestPath(start, end, WALL);

        assertNotNull(path);
        assertEquals(2, path.length);
        assertEquals(start, path[0]);
        assertEquals(end, path[1]);
    }

    /**
     * Tests that the path returns NULL if the target is blocked by walls.
     */
    @Test
    void testNoPath() {
        int[][] data = {
                {0, -1, 0},
                {-1, 0, -1},
                {0, -1, 0}
        };
        Map map = new Map(data);
        map.setCyclic(false);

        Pixel2D start = new Index2D(0, 0);
        Pixel2D end = new Index2D(1, 1);

        Pixel2D[] path = map.shortestPath(start, end, WALL);
        assertNull(path, "Path should be null");
    }

    /**
     * Tests Flood Fill algorithm.
     * Fills an area and ensures it doesn't leak through walls.
     */
    @Test
    void testFloodFill() {
        int[][] data = {
                {0, 0, 0, 0, 0},
                {0, -1, -1, -1, 0},
                {0, -1, 0, -1, 0},
                {0, -1, -1, -1, 0},
                {0, 0, 0, 0, 0}
        };
        Map map = new Map(data);
        Pixel2D center = new Index2D(2, 2);
        int filledCount = map.fill(center, 5);
        assertEquals(5, map.getPixel(2, 2));
        assertEquals(0, map.getPixel(0, 0));
        assertEquals(-1, map.getPixel(1, 1));
        assertEquals(1, filledCount);
    }

    /**
     * Tests the allDistance map generation.
     * Verifies that the map correctly calculates distances from a source.
     */
    @Test
    void testAllDistance() {
        Map map = new Map(3, 3, EMPTY);
        map.setCyclic(false);
        Pixel2D start = new Index2D(1, 1); // Center

        Map2D distMap = map.allDistance(start, WALL);
        assertEquals(0, distMap.getPixel(1, 1));
        assertEquals(1, distMap.getPixel(1, 0));
        assertEquals(1, distMap.getPixel(0, 1));
        assertEquals(1, distMap.getPixel(2, 1));
        assertEquals(1, distMap.getPixel(1, 2));
        assertEquals(2, distMap.getPixel(0, 0));
    }
}
