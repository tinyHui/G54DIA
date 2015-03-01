package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.Cell;
import uk.ac.nott.cs.g54dia.library.Station;
import uk.ac.nott.cs.g54dia.library.Well;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemMap {
    int MAX_RANGE = 50;
    Map<MemPoint, Station> station_list = new HashMap<MemPoint, Station>();
    Map<MemPoint, Well> well_list = new HashMap<MemPoint, Well>();

    public void appendStation(MemPoint p, Station s) {
        // deliver water takes one time step, cost one fuel
        if (p.calcDistanceToFuel() <= MAX_RANGE) {
            Iterator it = station_list.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MemPoint p_r = (MemPoint) pairs.getKey();
                if (p_r.equals(p)) {
                    it.remove();
                }
            }
            this.station_list.put(p, s);
        }
    }

    public void appendWell(MemPoint p, Well w) {
        // fill water takes one time step, cost one fuel
        if (p.calcDistanceToFuel() <= MAX_RANGE) {
            Iterator it = this.station_list.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MemPoint p_r = (MemPoint) pairs.getKey();
                if (p_r.equals(p)) {
                    it.remove();
                }
            }
            this.well_list.put((MemPoint) p.clone(), w);
        }
    }

    public Cell getCell(MemPoint p) {
        if (p != null) {
            Iterator it_s = this.station_list.entrySet().iterator();
            while (it_s.hasNext()) {
                Map.Entry pairs = (Map.Entry) it_s.next();
                MemPoint p_r = (MemPoint) pairs.getKey();
                if (p_r.x == p.x && p_r.y == p.y) {
                    return (Cell) pairs.getValue();
                }
            }

            Iterator it_w = this.well_list.entrySet().iterator();
            while (it_w.hasNext()) {
                Map.Entry pairs = (Map.Entry) it_w.next();
                MemPoint p_r = (MemPoint) pairs.getKey();
                if (p_r.x == p.x && p_r.y == p.y) {
                    return (Cell) pairs.getValue();
                }
            }
            return null;
        }
        throw new ValueException("Can'current_task find for a null point");
    }

    public MemPoint getMidWell(MemPoint start, MemPoint target) {
        // found best well to go
        int min_distance = 101;
        int distance;
        MemPoint midpoint = null;
        for (Map.Entry<MemPoint, Well> pairs : this.well_list.entrySet()) {
            MemPoint w_p = pairs.getKey();
            distance = start.calcDistance(w_p) + w_p.calcDistance(target);
            if (distance < min_distance) {
                min_distance = distance;
                midpoint = w_p;
            }
        }
        return midpoint;
    }
}
