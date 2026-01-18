package assignments.Ex3;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class MyGame {
    private static int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,0,1},{1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,1},
            {1,1,1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,0,1,1,1},{1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,1,0,1,0,1,1,1,1,0,1,1,1,0,1},{1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,1,0,1},{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
            {1,1,0,1,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,0,1,1},{0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
            {1,1,0,1,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,1,0,1,1},{1,0,0,0,0,0,1,0,1,1,1,0,1,1,1,0,1,0,0,0,0,0,1},
            {1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,1,0,1,1,1,1,1},{1,0,0,0,0,0,1,1,1,1,0,1,0,1,1,1,1,0,0,0,0,0,1},
            {1,0,1,0,1,0,1,0,0,0,0,1,0,0,0,0,1,0,1,0,1,0,1},{1,0,1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1},
            {1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1},{1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private static int pacX=11, pacY=1, score=0, level=1, pills=0, eaten=0, tick=0;
    private static ArrayList<int[]> ghosts = new ArrayList<>();
    private static boolean auto = false;
    private static final int[] DX = {0, 0, 1, -1}, DY = {1, -1, 0, 0}; // מעלה, מטה, ימין, שמאל

    public static void main(String[] a) {
        int w = map[0].length, h = map.length;
        StdDraw.setCanvasSize(w*25, h*25); StdDraw.setXscale(0, w); StdDraw.setYscale(0, h);
        StdDraw.enableDoubleBuffering();

        while (!StdDraw.isKeyPressed(KeyEvent.VK_M) && !StdDraw.isKeyPressed(KeyEvent.VK_A)) {
            StdDraw.clear(Color.BLACK); StdDraw.setPenColor(Color.YELLOW);
            StdDraw.text(w/2.0, h/2.0+2, "PAC-MAN EX3"); StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(w/2.0, h/2.0, "Press 'M' (Manual) or 'A' (Auto)"); StdDraw.show(); StdDraw.pause(50);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_A)) auto = true;
        initLevel(1);

        while (true) {
            tick++;
            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) { auto=!auto; StdDraw.pause(200); }
            if (auto) moveAuto(); else moveManual();
            if (tick % 3 == 0) moveGhosts();

            for (int[] g : ghosts) if (pacX==g[0] && pacY==g[1]) {
                System.out.println("Hit! Restarting..."); StdDraw.pause(1000); initLevel(level);
            }
            if (eaten >= pills) {
                System.out.println("Level Up!"); StdDraw.pause(1000); initLevel(++level);
            }
            draw(w, h); StdDraw.pause(100);
        }
    }

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

    private static void moveGhosts() {
        for (int[] g : ghosts) {
            int gx = g[0], gy = g[1];
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

    private static int[] getBest(int[][] grid) {
        int[][] dist = bfs(pacX, pacY, grid);
        int minD = 9999, tx = -1, ty = -1, h = grid.length;
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

    private static int[] getEscape() {
        int maxD = -1; int[] best = null;
        for(int k=0; k<4; k++) {
            int nx = pacX+DX[k], ny = pacY+DY[k];
            if (isValid(nx, ny, map)) {
                int minG = 9999;
                for (int[] g : ghosts) minG = Math.min(minG, Math.abs(g[0]-nx)+Math.abs(g[1]-ny));
                if (minG > maxD) { maxD = minG; best = new int[]{nx, ny}; }
            }
        }
        return best;
    }

    private static void moveManual() {
        int nx = pacX, ny = pacY;
        if (StdDraw.isKeyPressed(KeyEvent.VK_UP)) ny++; if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) ny--;
        if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) nx++; if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) nx--;
        updatePos(nx, ny);
    }

    private static void updatePos(int x, int y) {
        if (isValid(x, y, map)) {
            pacX=x; pacY=y;
            if (map[(map.length-1)-y][x] == 0) { score++; eaten++; map[(map.length-1)-y][x]=2; }
        }
    }

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

    private static boolean isValid(int x, int y, int[][] g) {
        int h=g.length; return x>=0 && x<g[0].length && y>=0 && y<h && g[(h-1)-y][x]!=1;
    }

    private static void draw(int w, int h) {
        StdDraw.clear(Color.BLACK);
        for(int y=0; y<h; y++) for(int x=0; x<w; x++) {
            int val = map[(h-1)-y][x];
            if(val==1) { StdDraw.setPenColor(new Color(20,20,180)); StdDraw.filledRectangle(x+.5,y+.5,.5,.5); }
            else if(val==0) { StdDraw.setPenColor(Color.PINK); StdDraw.filledCircle(x+.5,y+.5,.15); }
        }
        StdDraw.setPenColor(Color.YELLOW); StdDraw.filledCircle(pacX+.5,pacY+.5,.4);
        StdDraw.setPenColor(Color.RED); for(int[] g:ghosts) StdDraw.filledRectangle(g[0]+.5,g[1]+.5,.35,.35);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(w/2.0, h-.5, "Lvl: "+level+"  Pts: "+score+"  "+(auto?"AUTO":"MANUAL"));
        StdDraw.show();
    }
}