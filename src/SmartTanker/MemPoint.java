package SmartTanker;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemPoint implements Cloneable {
    volatile int x, y, abs_x, abs_y;

    MemPoint(int x, int y) {
        this.x = x;
        this.y = y;
        this.abs_x = Math.abs(x);
        this.abs_y = Math.abs(y);
    }

    public boolean equals(Object o) {
        MemPoint p = (MemPoint)o;
        if (p==null) return false;
        return (p.x == this.x) && (p.y == this.y);
    }

    public Object clone() {
        return new MemPoint(x,y);
    }

    public int calcDistance(MemPoint p2) {
        if (p2 == null) {
            return 0;
        }
        int dx = Math.abs(this.x - p2.x);
        int dy = Math.abs(this.y - p2.y);
        // coordinate start from 0
        return dx > dy ? dx : dy;
    }
}
