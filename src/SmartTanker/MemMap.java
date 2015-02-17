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
    int MAX_FUEL = 100;
    Map<MemPoint, Station> station_list = new HashMap<MemPoint, Station>();
    Map<MemPoint, Well> well_list = new HashMap<MemPoint, Well>();

    public void appendStation(MemPoint p, Station s) {
        if (p.abs_x <= MAX_FUEL / 2 && p.abs_y <= MAX_FUEL / 2) {
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
        if (p.abs_x <= MAX_FUEL / 2 && p.abs_y <= MAX_FUEL / 2) {
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
            throw new ValueException("Neither station nor well found on that point");
        }
        throw new ValueException("Can't find for a null point");
    }

    public MemPoint nearerMidPoint(MemPoint sp, MemPoint ep, MemPoint p1, MemPoint p2) {
        if (p1 == null && p2 == null) {
            return null;
        } else if (p1 == null) {
            return p2;
        } else if (p2 == null) {
            return p1;
        }

        MemPoint mid_point = new MemPoint ((sp.x + ep.x) / 2, (sp.y + ep.y) / 2);
        int p1_to_mid = p1.calcDistance(mid_point);
        int p2_to_mid = p2.calcDistance(mid_point);
        return p1_to_mid > p2_to_mid ? p2 : p1;
    }

}
