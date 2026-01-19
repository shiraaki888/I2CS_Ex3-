package assignments.Ex3;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * MyGame - A complete Pac-Man game implementation:
 * 1. Manual and Auto modes.
 * 2. Smart AI (Safe Mode + Escape Logic).
 * 3. Ghost intelligence with balanced difficulty.
 * 4. Multi-level system.
 */
public class MyGame {

    // The Game Map (1 = Wall, 0 = Dot/Path, 2 = Empty/House)
    private static int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,0,1},
            {1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,1},
            {1,1,1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,0,1,1,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,1,0,1,0,1,1,1,1,0,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,1,0,1},
            {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
            {1,1,0,1,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,0,1,1},
            {0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
            {1,1,0,1,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,1,0,1,1},
            {1,0,0,0,0,0,1,0,1,1,1,0,1,1,1,0,1,0,0,0,0,0,1},
            {1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,1,1,1,1,0,1,0,1,1,1,1,0,0,0,0,0,1},
            {1,0,1,0,1,0,1,0,0,0,0,1,0,0,0,0,1,0,1,0,1,0,1},
            {1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1},
            {1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1},
            {1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private static int pacX=11, pacY=1, score=0, level=1, pills=0, eaten=0, tick=0;
    private static ArrayList<int[]> ghosts = new ArrayList<>();
    private static boolean auto = false;
    private static final int[] DX = {0, 0, 1, -1}, DY = {1, -1, 0, 0};

    /**
     * Main Game Loop.
     * 1. Sets up the canvas.
     * 2. Shows the Start Screen.
     * 3. Runs the infinite game loop (Update Logic -> Draw -> Sleep).
     */
    public static void main(String[] a) {
        int w = map[0].length, h = map.length;
        StdDraw.setCanvasSize(w*25, h*25); StdDraw.setXscale(0, w); StdDraw.setYscale(0, h);
        StdDraw.enableDoubleBuffering();
        showStartScreen(w, h);
        if (StdDraw.isKeyPressed(KeyEvent.VK_A)) auto = true;
        initLevel(1);
        while (true) {
            tick++;
            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) { auto=!auto; StdDraw.pause(200); }
            if (auto) moveAuto(); else moveManual();

            if (tick % 3 == 0) moveGhosts();

            for (int[] g : ghosts) if (pacX==g[0] && pacY==g[1]) {
                System.out.println("Hit! Restarting level..."); StdDraw.pause(1000); initLevel(level);
            }
            if (eaten >= pills) {
                System.out.println("Level Up!"); StdDraw.pause(1000); initLevel(++level);
            }

            draw(w, h);
            StdDraw.pause(100);
        }
    }

    /**
     * Start Screen Display.
     * Shows instructions and waits for user to select mode (M or A).
     */
    private static void showStartScreen(int w, int h) {
        while (!StdDraw.isKeyPressed(KeyEvent.VK_M) && !StdDraw.isKeyPressed(KeyEvent.VK_A)) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 30));
            StdDraw.text(w/2.0, h/2.0+3, "PAC-MAN EX3");

            StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(w/2.0, h/2.0+1, "Press 'M' for MANUAL");

            StdDraw.setFont(new Font("Arial", Font.ITALIC, 16));
            StdDraw.setPenColor(Color.LIGHT_GRAY);
            StdDraw.text(w/2.0, h/2.0-0.5, "(Use Arrow Keys to Move)");

            StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(w/2.0, h/2.0-2.5, "Press 'A' for AUTO");

            StdDraw.show();
            StdDraw.pause(50);
        }
    }

    /**
     * Initializes a level.
     * Resets Pacman position, counts dots, clears ghosts, and spawns new ghosts based on level number.
     * Also opens the ghost house exit (roof).
     */
    private static void initLevel(int lvl) {
        pills=0; eaten=0; ghosts.clear(); map[10][11]=2;
        for(int y=0; y<map.length; y++) for(int x=0; x<map[0].length; x++) {
            if(map[y][x]!=1) {
                if((x>=9 && x<=13) && (y>=10 && y<=12)) map[y][x]=2;
                else { map[y][x]=0; pills++; }
            }
        }
        pacX=11; pacY=1;
        for(int i=0; i<Math.min(lvl,10); i++) ghosts.add(new int[]{11, 10});
    }

    /**
     * 1. If inside house -> Exit logic (Go center then up).
     * 2. Small chance (30%) to move randomly (Fairness factor).
     * 3. Otherwise -> Chase Pacman (shortest path logic).
     */
    private static void moveGhosts() {
        for (int[] g : ghosts) {
            int gx = g[0], gy = g[1];
            // Exit strategy: if in house, move to center and go up
            if (gy < 12 && gx >=9 && gx <=13) {
                if (gx < 11) g[0]++; else if (gx > 11) g[0]--; else g[1]++; continue;
            }
            int dx=0, dy=0;
            if (Math.random() < 0.3) {
                int r = (int)(Math.random()*4);
                if (isValid(gx+DX[r], gy+DY[r], map)) { g[0]+=DX[r]; g[1]+=DY[r]; continue; }
            }
            if (pacX > gx) dx=1; else if (pacX < gx) dx=-1;
            if (pacY > gy) dy=1; else if (pacY < gy) dy=-1;
            if (dx!=0 && isValid(gx+dx, gy, map)) g[0]+=dx; else if (dy!=0 && isValid(gx, gy+dy, map)) g[1]+=dy;
        }
    }

    /**
     * Auto Mode:
     * 1. Creates a "Safe Map" where ghosts are treated as walls.
     * 2. Tries to find the nearest dot on Safe Map using BFS.
     * 3. If trapped (Plan B), activates "Escape Mode" to run away from ghosts.
     */
    private static void moveAuto() {
        int h = map.length, w = map[0].length;
        int[][] safeMap = new int[h][w];
        for(int i=0; i<h; i++) safeMap[i] = map[i].clone();
        for (int[] g : ghosts) {
            int gy = (h-1)-g[1];
            if(gy>=0 && gy<h) safeMap[gy][g[0]] = 1;
            for(int k=0; k<4; k++) if(isValid(g[0]+DX[k], g[1]+DY[k], map)) safeMap[(h-1)-(g[1]+DY[k])][g[0]+DX[k]] = 1;
        }

        int[] move = getBest(safeMap);
        if (move == null) move = getEscape();
        if (move == null) move = getBest(map);
        if (move != null) updatePos(move[0], move[1]);
    }

    /**
     * Finds the nearest dot using BFS (Breadth-First Search).
     * @param grid The map to search on (Safe or Real).
     * @return Next step coordinates {x, y} or null if no path found.
     */
    private static int[] getBest(int[][] grid) {
        int[][] dist = bfs(pacX, pacY, grid);
        int minD = 9999, tx = -1, ty = -1, h = grid.length;
        // Find closest reachable dot
        for(int y=0; y<h; y++) for(int x=0; x<grid[0].length; x++)
            if(map[(h-1)-y][x]==0 && dist[(h-1)-y][x]!=-1 && dist[(h-1)-y][x]<minD) { minD=dist[(h-1)-y][x]; tx=x; ty=y; }

        if (tx != -1) for(int i=0; i<4; i++) {
            int nx=pacX+DX[i], ny=pacY+DY[i];
            if (isValid(nx, ny, grid)) {
                int d = bfs(nx, ny, grid)[(h-1)-ty][tx];
                if (d!=-1 && d<minD) return new int[]{nx, ny};
            }
        }
        return null;
    }

    /**
     * Escape Logic (Plan B).
     * Finds the move that maximizes the distance to the nearest ghost.
     */
    private static int[] getEscape() {
        int maxD = -1; int[] best = null;
        for(int k=0; k<4; k++) {
            int nx = pacX+DX[k], ny = pacY+DY[k];
            if (isValid(nx, ny, map)) {
                int minG = 9999;
                // Calculate distance to nearest ghost
                for (int[] g : ghosts) minG = Math.min(minG, Math.abs(g[0]-nx)+Math.abs(g[1]-ny));
                if (minG > maxD) { maxD = minG; best = new int[]{nx, ny}; }
            }
        }
        return best;
    }

    /**
     * Reads keyboard input for manual movement.
     */
    private static void moveManual() {
        int nx = pacX, ny = pacY;
        if (StdDraw.isKeyPressed(KeyEvent.VK_UP)) ny++; if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) ny--;
        if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) nx++; if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) nx--;
        updatePos(nx, ny);
    }

    /**
     * Updates Pacman position and handles Eating dots.
     */
    private static void updatePos(int x, int y) {
        if (isValid(x, y, map)) {
            pacX=x; pacY=y;
            if (map[(map.length-1)-y][x] == 0) { score++; eaten++; map[(map.length-1)-y][x]=2; }
        }
    }

    /**
     * a BFS algorithm to calculate distances from source (sx, sy) to all points.
     */
    private static int[][] bfs(int sx, int sy, int[][] g) {
        int h=g.length, w=g[0].length; int[][] d = new int[h][w];
        for(int[] r : d) Arrays.fill(r, -1);
        int syArr = (h-1)-sy; if(syArr<0||syArr>=h) return d;
        d[syArr][sx] = 0; Queue<int[]> q = new LinkedList<>(); q.add(new int[]{sx, sy});
        while(!q.isEmpty()) {
            int[] c = q.poll();
            for(int i=0; i<4; i++) {
                int nx=c[0]+DX[i], ny=c[1]+DY[i], nyArr=(h-1)-ny;
                if (isValid(nx, ny, g) && d[nyArr][nx]==-1) { d[nyArr][nx]=d[(h-1)-c[1]][c[0]]+1; q.add(new int[]{nx, ny}); }
            }
        }
        return d;
    }

    /**
     * Checks if a coordinate is within the board boundaries and is NOT a wall.
     */
    private static boolean isValid(int x, int y, int[][] g) {
        int h=g.length; return x>=0 && x<g[0].length && y>=0 && y<h && g[(h-1)-y][x]!=1;
    }

    /**
     * Renders the game: Map, Pacman, Ghosts, and Score.
     */
    private static void draw(int w, int h) {
        StdDraw.clear(Color.BLACK);
        // Draw Map
        for(int y=0; y<h; y++) for(int x=0; x<w; x++) {
            int val = map[(h-1)-y][x];
            if(val==1) { StdDraw.setPenColor(new Color(20,20,180)); StdDraw.filledRectangle(x+.5,y+.5,.5,.5); }
            else if(val==0) { StdDraw.setPenColor(Color.PINK); StdDraw.filledCircle(x+.5,y+.5,.15); }
        }
        // Draw Pacman
        StdDraw.setPenColor(Color.YELLOW); StdDraw.filledCircle(pacX+.5,pacY+.5,.4);
        // Draw Ghosts
        StdDraw.setPenColor(Color.RED); for(int[] g:ghosts) StdDraw.filledRectangle(g[0]+.5,g[1]+.5,.35,.35);
        // Draw GUI Text
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(w/2.0, h-.5, "Lvl: "+level+"  Pts: "+score+"  "+(auto?"AUTO":"MANUAL"));
        StdDraw.show();
    }
}