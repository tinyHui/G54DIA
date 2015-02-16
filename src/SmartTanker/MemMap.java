package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
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
    Map<MemPoint, Station> station_list = new HashMap<MemPoint, Station>();
    Map<MemPoint, Well> well_list = new HashMap<MemPoint, Well>();

    public void appendStation(MemPoint p, Station s) {
        Iterator it = station_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.equals(p)) {
                it.remove();
            }
        }
        this.station_list.put(p, s);
    }

    public void appendWell(MemPoint p, Well w) {
        Iterator it = this.station_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.equals(p)) {
                it.remove();
            }
        }
        this.well_list.put((MemPoint) p.clone(), w);
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
        Iterator it_s = this.station_list.entrySet().iterator();
        while (it_s.hasNext()) {
            Map.Entry pairs = (Map.Entry)it_s.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.x == p.x && p_r.y == p.y) {
                return (Cell) pairs.getValue();
            }
        }

        Iterator it_w = this.well_list.entrySet().iterator();
        while (it_w.hasNext()) {
            Map.Entry pairs = (Map.Entry)it_w.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            if (p_r.x == p.x && p_r.y == p.y) {
                return (Cell) pairs.getValue();
            }
        }

        throw new ValueException("Neither station nor well found on that point");
    }

    public MemPoint nearerPoint(MemPoint p, MemPoint p1, MemPoint p2) {
        if (p1 == null) {
            return p2;
        } else if (p2 == null) {
            return p1;
        }
        int d1 = p.calcDistance(p1);
        int d2 = p.calcDistance(p2);
        return d1 > d2 ? p2 : p1;
    }

}
