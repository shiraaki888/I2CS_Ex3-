package assignments.Ex3;
import java.util.LinkedList;
import java.util.Queue;
/**
 * This class represents a 2D map as a "screen" or a raster matrix or maze over integers.
 * @author boaz.benmoshe
 *
 */
public class Map implements Map2D {
	private int[][] _map;
	private boolean _cyclicFlag = true;
	
	/**
	 * Constructs a w*h 2D raster map with an init value v.
	 * @param w
	 * @param h
	 * @param v
	 */
	public Map(int w, int h, int v) {init(w,h, v);}
	/**
	 * Constructs a square map (size*size).
	 * @param size
	 */
	public Map(int size) {this(size,size, 0);}
	
	/**
	 * Constructs a map from a given 2D array.
	 * @param data
	 */
	public Map(int[][] data) {
		init(data);
	}
    @Override
    public void init(int w, int h, int v) {
        _map = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                _map[x][y] = v;
            }
        }
    }
    @Override
    public void init(int[][] arr) {
        if (arr == null || arr.length == 0) throw new RuntimeException("Invalid array");
        _map = new int[arr.length][arr[0].length];
        for (int x = 0; x < arr.length; x++) {
            for (int y = 0; y < arr[0].length; y++) {
                _map[x][y] = arr[x][y];
            }
        }
    }
    @Override
    public int[][] getMap() {
        if (_map == null) return null;
        int[][] ans = new int[_map.length][_map[0].length];
        for (int x = 0; x < _map.length; x++) {
            for (int y = 0; y < _map[0].length; y++) {
                ans[x][y] = _map[x][y];
            }
        }
        return ans;
    }
    @Override
    public int getWidth() {
        return _map.length;
    }

    @Override
    public int getHeight() {
        return _map[0].length;
    }

    @Override
    public int getPixel(int x, int y) {
        if (isInside(x, y)) return _map[x][y];
        return -1; // Or throw exception
    }

    @Override
    public int getPixel(Pixel2D p) {
        return this.getPixel(p.getX(), p.getY());
    }

    @Override
    public void setPixel(int x, int y, int v) {
        if (isInside(x, y)) _map[x][y] = v;
    }

    @Override
    public void setPixel(Pixel2D p, int v) {
        setPixel(p.getX(), p.getY(), v);
    }

    @Override
	/** 
	 * Fills this map with the new color (new_v) starting from p.
	 * https://en.wikipedia.org/wiki/Flood_fill
	 */
	public int fill(Pixel2D xy, int new_v) {
		int ans=0;
        if (!isInside(xy)) return 0;
        int targetVal = getPixel(xy);
        if (targetVal == new_v) return 0;

        Queue<Pixel2D> q = new LinkedList<>();
        q.add(xy);
        setPixel(xy, new_v);
        ans++;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while(!q.isEmpty()) {
            Pixel2D curr = q.poll();
            for(int i=0; i<4; i++) {
                int nx = curr.getX() + dx[i];
                int ny = curr.getY() + dy[i];

                if(isCyclic()) {
                    nx = (nx + getWidth()) % getWidth();
                    ny = (ny + getHeight()) % getHeight();
                }

                if(isInside(nx, ny) && getPixel(nx, ny) == targetVal) {
                    setPixel(nx, ny, new_v);
                    q.add(new Index2D(nx, ny));
                    ans++;
                }
            }
        }
        return ans;
    }

	@Override
	/**
	 * BFS like shortest the computation based on iterative raster implementation of BFS, see:
	 * https://en.wikipedia.org/wiki/Breadth-first_search
	 */
    public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor) {
        Pixel2D[] ans = null;
        if (!isInside(p1) || !isInside(p2) || getPixel(p1) == obsColor || getPixel(p2) == obsColor) {
            return null;
        }
        if (p1.equals(p2)) return new Pixel2D[]{p1};

        int w = getWidth();
        int h = getHeight();
        int[][] dist = new int[w][h];
        Pixel2D[][] parents = new Pixel2D[w][h];

        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) dist[i][j] = -1;
        }

        Queue<Pixel2D> q = new LinkedList<>();
        q.add(p1);
        dist[p1.getX()][p1.getY()] = 0;

        boolean found = false;
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while(!q.isEmpty()) {
            Pixel2D curr = q.poll();
            if(curr.equals(p2)) {
                found = true;
                break;
            }

            for(int i=0; i<4; i++) {
                int nx = curr.getX() + dx[i];
                int ny = curr.getY() + dy[i];

                if(isCyclic()) {
                    nx = (nx + w) % w;
                    ny = (ny + h) % h;
                }

                if(isInside(nx, ny) && getPixel(nx, ny) != obsColor && dist[nx][ny] == -1) {
                    dist[nx][ny] = dist[curr.getX()][curr.getY()] + 1;
                    parents[nx][ny] = curr;
                    q.add(new Index2D(nx, ny));
                }
            }
        }

        if(found) {
            int pathLen = dist[p2.getX()][p2.getY()];
            ans = new Pixel2D[pathLen + 1];
            Pixel2D curr = p2;
            for(int i = pathLen; i >= 0; i--) {
                ans[i] = curr;
                if(i > 0) curr = parents[curr.getX()][curr.getY()];
            }
        }
        return ans;
    }


    private boolean isInside(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    @Override
    public boolean isInside(Pixel2D p) {
        return isInside(p.getX(), p.getY());
    }

    @Override
    public boolean isCyclic() {
        return _cyclicFlag;
    }

    @Override
    public void setCyclic(boolean cy) {
        _cyclicFlag = cy;
    }

    @Override
    public Map2D allDistance(Pixel2D start, int obsColor) {
        Map2D ans = null;
        ans = new Map(getWidth(), getHeight(), -1);

        if (!isInside(start) || getPixel(start) == obsColor) return ans;

        Queue<Pixel2D> q = new LinkedList<>();
        q.add(start);
        ans.setPixel(start, 0);

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while(!q.isEmpty()) {
            Pixel2D curr = q.poll();
            int currentDist = ans.getPixel(curr);

            for(int i=0; i<4; i++) {
                int nx = curr.getX() + dx[i];
                int ny = curr.getY() + dy[i];

                if(isCyclic()) {
                    nx = (nx + getWidth()) % getWidth();
                    ny = (ny + getHeight()) % getHeight();
                }

                if(isInside(nx, ny) && getPixel(nx, ny) != obsColor && ans.getPixel(nx, ny) == -1) {
                    ans.setPixel(nx, ny, currentDist + 1);
                    q.add(new Index2D(nx, ny));
                }
            }
        }
        return ans;
    }
}
