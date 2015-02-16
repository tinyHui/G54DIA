package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.MoveAction;
import uk.ac.nott.cs.g54dia.library.Point;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemPoint implements Cloneable {
    volatile int x, y;

    MemPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point toPoint(MemMap map) {
        Point p = map.getCell(this).getPoint();
        if (p == null) {
            throw new ValueException("Unrecognised Direction");
        } else {
            return p;
        }
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
        int dx = Math.abs(this.x - this.x);
        int dy = Math.abs(p2.y - p2.y);
        return dx > dy ? dx : dy;
    }
}
