package assignments.Ex3;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import java.awt.Color;
import java.util.ArrayList;

/**
 * Ex3Algo - A Pac-Man Algorithm with "Survival Instincts":
 * 1. Safe Mode (Virtual Walls):
 * The algorithm treats dangerous ghosts not just as enemies, but as "walls".
 * It marks the ghost's position and its immediate neighbors as obstacles on a temporary map.
 * This ensures the Pac-Man never even attempts to calculate a path through a dangerous area.
 * * 2. Plan B (Risky Mode):
 * If the Pac-Man is trapped (i.e., the "Safe Mode" map shows no reachable targets because
 * ghosts are blocking all paths), the algorithm switches to "Real Map" mode.
 * In this mode, it ignores the virtual walls and tries to squeeze past the ghosts to survive,
 * rather than freezing in place.
 * * 3. Efficiency (BFS):
 * Instead of running a pathfinding algorithm for every single dot (which is slow),
 * we run the `allDistance` (BFS) function ONCE per move. This gives us the distance
 * to every point on the board in a single calculation.
 * * 4. Anti-Stuck Mechanism:
 * The algorithm remembers its last 10 positions. If it detects that it is moving
 * back and forth (stuck in a loop), it forces a few random valid moves to break free.
 */
public class Ex3Algo implements PacManAlgo {

    // Internal counter for the number of moves made
    private int _count;

    // History buffer to track the last few positions of the Pac-Man.
    // Used to detect if the pac-man is stuck in a loo.
    private ArrayList<String> _posHistory = new ArrayList<>();

    // Counter for "forced random moves".
    // When > 0, the pac-man moves randomly to escape a loop.
    private int _stuckMoves = 0;

    /**
     * Constructor: Initializes the algorithm state.
     */
    public Ex3Algo() {
        _count = 0;
    }

    /**
     * Returns a short description of the algorithm.
     * @return String description
     */
    @Override
    public String getInfo() {
        return "Algo: Safe Mode (Virtual Walls) + Plan B (Risky Mode) + Anti-Stuck";
    }

    /**
     * The main method called by the game engine at every step.
     * Calculates the next move for the Pac-Man agent.
     * * @param game The game object containing the board, ghosts, and player status.
     * @return An integer representing the direction (UP, DOWN, LEFT, RIGHT).
     */
    @Override
    public int move(PacmanGame game) {
        _count++;
        int[][] board = game.getGame(0);
        int w = board.length;
        int h = board[0].length;

        // Get current Pac-Man position
        String currentPosStr = game.getPos(0).toString();
        Pixel2D pacmanPos = parsePos(currentPosStr);

        // Step 1: Anti-Stuck Mechanism
        // Update history buffer
        _posHistory.add(currentPosStr);
        if (_posHistory.size() > 10) {
            _posHistory.remove(0); // Keep only the last 10 moves to save memory
        }

        // Check how many times we visited the current spot recently
        int occurrences = 0;
        for (String s : _posHistory) {
            if (s.equals(currentPosStr)) occurrences++;
        }

        // If we visited the spot more than 3 times in the last 10 moves, we are probably stuck.
        if (occurrences > 3 && _stuckMoves == 0) {
            _stuckMoves = 4;
        }

        int wallColor = Game.getIntColor(Color.BLUE, 0)
        Map realMap = new Map(board);
        realMap.setCyclic(GameInfo.CYCLIC_MODE);

        // If we are in "Stuck Mode", execute a random valid move immediately.
        if (_stuckMoves > 0) {
            _stuckMoves--;
            return randomValidDir(pacmanPos, realMap, wallColor);
        }
        // Step 2:
        // We create a deep copy of the map called "safeMap".
        // We will "draw" walls on this map where ghosts are located.
        Map safeMap = new Map(board);
        safeMap.setCyclic(GameInfo.CYCLIC_MODE);

        GhostCL[] ghosts = game.getGhosts(0);

        // Mark dangerous ghosts as walls on the safeMap
        for (GhostCL g : ghosts) {
            // A ghost is dangerous if it is NOT eatable, or if its eatable time is running out.
            if (g.remainTimeAsEatable(0) < 15) {
                Pixel2D gPos = parsePos(g.getPos(0).toString());
                safeMap.setPixel(gPos, wallColor);
                // This prevents the Pac-Man from walking into a ghost.
                int[] dx = {0, 0, 1, -1};
                int[] dy = {1, -1, 0, 0};
                for(int k=0; k<4; k++) {
                    // Use helper to handle cyclic borders correctly
                    Pixel2D neighbor = getNextPos(gPos, k, w, h);
                    if(safeMap.isInside(neighbor)) {
                        safeMap.setPixel(neighbor, wallColor);
                    }
                }
            }
        }

        // Attempt to find the best target (Pink dot or Eatable ghost) on the SAFE map.
        // If this returns a target, it means there is a safe path.
        Pixel2D target = findBestTarget(pacmanPos, safeMap, game, wallColor);

        // Step 3:
        // If target is null, it means the Safe Map is fully blocked (we are trapped by ghosts).
        // In this case, we switch to the "Real Map" (ignoring virtual walls).
        // We take the risk to move closer to a target, hoping to slip by.
        if (target == null) {
            target = findBestTarget(pacmanPos, realMap, game, wallColor);
        }
        // Step 4: Move Calculation
        if (target != null) {
            // Try to calculate the shortest path using the Safe Map first
            Pixel2D[] path = safeMap.shortestPath(pacmanPos, target, wallColor);

            // If Safe Map fails, calculate path on Real Map
            if (path == null) {
                path = realMap.shortestPath(pacmanPos, target, wallColor);
            }
            // If a valid path is found, return the direction of the first step
            if (path != null && path.length > 1) {
                return move2Dir(pacmanPos, path[1], w, h);
            }
        }

        // move randomly (but don't hit a wall).
        return randomValidDir(pacmanPos, realMap, wallColor);
    }

    /**
     * Finds the "Best Target" for the Pac-Man.
     * Uses BFS (allDistance) to scan the entire board efficiently.
     * * Logic:
     * 1. Pink/Green Dots: Checks distance. Closest reachable dot is the candidate.
     * 2. Eatable Ghosts: Checks distance. If an eatable ghost is closer than a dot, it becomes the target.
     * * @param pacmanPos Current position of Pac-Man.
     * @param map The map to search on (either Safe Map or Real Map).
     * @param game The game state.
     * @param wallColor The integer value representing a wall/obstacle.
     * @return The Pixel2D coordinate of the best target, or null if no target is reachable.
     */
    private Pixel2D findBestTarget(Pixel2D pacmanPos, Map map, PacmanGame game, int wallColor) {
        int[][] board = game.getGame(0);
        int w = board.length;
        int h = board[0].length;
        int pinkDot = Game.getIntColor(Color.PINK, 0);
        int greenDot = Game.getIntColor(Color.GREEN, 0);

        // Compute BFS distances to ALL cells on the map at once.
        Map2D distMap = map.allDistance(pacmanPos, wallColor);

        double minDistance = Double.MAX_VALUE;
        Pixel2D bestTarget = null;

        // Scan the board for dots
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = board[x][y];
                if (pixel == pinkDot || pixel == greenDot) {
                    int d = distMap.getPixel(x, y);
                    // d != -1 means the target is not blocked by walls or ghosts
                    if (d != -1 && d < minDistance) {
                        minDistance = d;
                        bestTarget = new Index2D(x, y);
                    }
                }
            }
        }

        // Scan for Eatable Ghosts
        GhostCL[] ghosts = game.getGhosts(0);
        for (GhostCL g : ghosts) {
            // We only chase a ghost if we have enough time to catch it.
            if (g.remainTimeAsEatable(0) > 10) {
                Pixel2D gPos = parsePos(g.getPos(0).toString());
                int d = distMap.getPixel(gPos.getX(), gPos.getY());

                // If the ghost is reachable and closer than the nearest dot Chase it
                if (d != -1 && d < minDistance) {
                    minDistance = d;
                    bestTarget = gPos;
                }
            }
        }
        return bestTarget;
    }

    // =============================================================
    // Helper Methods
    // =============================================================

    /**
     * Calculates the next position coordinates based on a numeric direction index (0-3).
     * Used mainly for checking neighbors in loops.
     * Handles Cyclic Mode (wrapping around edges).
     * * @param p Current pixel
     * @param indexDir 0=Right, 1=Left, 2=Up, 3=Down (arbitrary internal mapping)
     * @param w Map width
     * @param h Map height
     * @return New Pixel2D position
     */
    private Pixel2D getNextPos(Pixel2D p, int indexDir, int w, int h) {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        int x = p.getX() + dx[indexDir];
        int y = p.getY() + dy[indexDir];

        if (GameInfo.CYCLIC_MODE) {
            x = (x + w) % w;
            y = (y + h) % h;
        }
        return new Index2D(x, y);
    }

    /**
     * Calculates the next position based on Game.UP/DOWN/LEFT/RIGHT constants.
     * Use this when working with the game engine's direction constants.
     */
    private Pixel2D getNextPosByGameDir(Pixel2D p, int dir, int w, int h) {
        int x = p.getX();
        int y = p.getY();
        if (dir == Game.RIGHT) x++;
        if (dir == Game.LEFT) x--;
        if (dir == Game.UP) y++;
        if (dir == Game.DOWN) y--;

        // Handle wrapping for Cyclic maps
        if (GameInfo.CYCLIC_MODE) {
            x = (x + w) % w;
            y = (y + h) % h;
        }
        return new Index2D(x, y);
    }

    /**
     * Selects a random direction that is logically valid (does not hit a wall).
     * Used when the algorithm is confused or stuck.
     * * @param p Current position
     * @param map The map (to check for walls)
     * @param wallColor Color of walls
     * @return A valid Game direction int (UP, DOWN, LEFT, RIGHT)
     */
    private int randomValidDir(Pixel2D p, Map map, int wallColor) {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        ArrayList<Integer> validDirs = new ArrayList<>();

        // Test all 4 directions
        for (int dir : dirs) {
            Pixel2D next = getNextPosByGameDir(p, dir, map.getWidth(), map.getHeight());
            if (map.getPixel(next) != wallColor) {
                validDirs.add(dir);
            }
        }

        // If all directions are blocked, default to UP.
        if (validDirs.isEmpty()) return Game.UP;

        // Pick a random direction from the valid ones
        return validDirs.get((int) (Math.random() * validDirs.size()));
    }

    /**
     * Utility: Parses a string "x,y,z" into a Pixel2D object.
     * The game engine provides positions as Strings.
     */
    private Pixel2D parsePos(String s) {
        try {
            String[] a = s.split(",");
            return new Index2D(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
        } catch (Exception e) {
            return new Index2D(0, 0); // Default fallback
        }
    }

    /**
     * Converts a move from 'curr' to 'next' into a Game direction constant.
     * Crucially, this handles the logic for Cyclic World (Teleporting).
     * * Example: Moving from x=0 to x=19 (on width 20) is a LEFT move, not RIGHT.
     * * @param curr Current position
     * @param next Next position
     * @param w Board width
     * @param h Board height
     * @return Game direction constant
     */
    private int move2Dir(Pixel2D curr, Pixel2D next, int w, int h) {
        int dx = next.getX() - curr.getX();
        int dy = next.getY() - curr.getY();

        // Standard movement
        if (dx == 1) return Game.RIGHT;
        if (dx == -1) return Game.LEFT;
        if (dy == 1) return Game.UP;
        if (dy == -1) return Game.DOWN;

        // Cyclic wrapping
        if (dx < -1) return Game.RIGHT;
        if (dx > 1) return Game.LEFT;
        if (dy < -1) return Game.UP;
        if (dy > 1) return Game.DOWN;

        return Game.UP;
    }
}