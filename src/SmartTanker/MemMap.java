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
        if (p.abs_x < MAX_RANGE && p.abs_y < MAX_RANGE) {
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
        if (p.abs_x < MAX_RANGE && p.abs_y < MAX_RANGE) {
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

    public MemPoint getNearestWell(MemPoint current) {
        int min_distance = 100;
        int distance;
        MemPoint well = null;
        Iterator it = this.well_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            distance = p_r.calcDistance(current);
            if (distance < min_distance) {
                min_distance = distance;
                well = p_r;
            }
        }
        return well;
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
        throw new ValueException("Can't find for a null point");
    }

    public MemPoint nearestToGo(MemPoint sp, MemPoint ep, MemPoint p1, MemPoint p2) {
        if (p1 == null && p2 == null) {
            return null;
        } else if (p1 == null) {
            return p2;
        } else if (p2 == null) {
            return p1;
        }

        int p1_distance_sum = p1.calcDistance(sp) + p1.calcDistance(ep);
        int p2_distance_sum = p2.calcDistance(sp) + p2.calcDistance(ep);

        return p1_distance_sum > p2_distance_sum ? p2 : p1;
    }

}
