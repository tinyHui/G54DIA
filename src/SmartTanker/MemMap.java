package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Cell;
import uk.ac.nott.cs.g54dia.library.Station;
import uk.ac.nott.cs.g54dia.library.Task;
import uk.ac.nott.cs.g54dia.library.Well;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemMap {
    Map<MemPoint, Station> stations = new HashMap<MemPoint, Station>();
    Map<MemPoint, Well> wells = new HashMap<MemPoint, Well>();

    public void appendStation(MemPoint p, Station s) {
        Iterator it = stations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.x == p.x && p_r.y == p.y) {
                it.remove();
            }
        }
        this.stations.put(p, s);
    }

    public void appendWell(MemPoint p, Well w) {
        Iterator it = stations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.x == p.x && p_r.y == p.y) {
                it.remove();
            }
        }
        this.wells.put(p, w);
    }

    public MemPoint getNearestStation(MemPoint current) {
        int min_distance = 100;
        int distance;
        MemPoint station = null;
        Iterator it = stations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            Task t = ((Station) pairs.getValue()).getTask();
            if (t != null) {
                distance = calcDistance(p_r, current);
                if (distance < min_distance) {
                    min_distance = distance;
                    station = p_r;
                }
            }
        }
        return station;
    }

    public MemPoint getNearestWell(MemPoint current) {
        int min_distance = 100;
        int distance;
        MemPoint well = null;
        Iterator it = wells.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            distance = calcDistance(p_r, current);
            if (distance < min_distance) {
                min_distance = distance;
                well = p_r;
            }
        }
        return well;
    }

    public Cell getCell(MemPoint p) {
        Iterator it = stations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.x == p.x && p_r.y == p.y) {
                return (Cell) pairs.getValue();
            }
        }
        return null;
    }

    public int calcDistance(MemPoint p1, MemPoint p2) {
        int dx = Math.abs(p1.x - p2.x);
        int dy = Math.abs(p2.y - p2.y);
        return dx > dy ? dx : dy;
    }

}
