package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.Cell;
import uk.ac.nott.cs.g54dia.library.Station;
import uk.ac.nott.cs.g54dia.library.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JasonChen on 2/16/15.
 */
public class TaskSys {
    Map<Task, MemPoint> task_list = new HashMap<Task, MemPoint>();
    int MAX_FUEL = 100;

    public Task scanTaskList(MemPoint p) {
        Task t = null;
        int min_distance = 100;
        int distance;
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            MemPoint t_p = (MemPoint) pairs.getValue();
            if (t_r.isComplete()) {
                it.remove();
            } else {
                distance = t_p.calcDistance(p);
                if (distance < min_distance) {
                    min_distance = distance;
                    t = t_r;
                }
            }
        }
        return t;
    }

    public void appendTask(MemPoint p, MemMap map) {
        if (p.x < MAX_FUEL / 2 &&
                p.y < MAX_FUEL / 2) {
            Task t = ((Station) map.getCell(p)).getTask();
            if (t != null) {
                Iterator it = this.task_list.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    Task t_r = (Task) pairs.getKey();
                    if (t_r == t) {
                        it.remove();
                    }
                }
                MemPoint well_point = map.getNearestWell(p);
                this.task_list.put(t, (MemPoint) p.clone());
            }
        }
    }

    public MemPoint getPoint(Task t) {
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            if (t_r == t) {
                return (MemPoint) pairs.getValue();
            }
        }
        throw new ValueException("No task found on that point");
    }
}
