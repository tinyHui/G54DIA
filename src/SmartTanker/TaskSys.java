package SmartTanker;

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

    public Task scanTaskList(MemPoint p) {
        Task t = null;
        int min_cost = 100;
        int score;
        int cost;
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            MemPoint t_p = (MemPoint) pairs.getValue();
            if (t_r.isComplete()) {
                it.remove();
            } else if (p.equals(t_p)) {
                // at this point
                t = t_r;
            } else {
                cost = t_p.calcDistance(p);
                score = t_r.getRequired();
                // if can get more score by spend cost
                if (cost - score < min_cost) {
                    min_cost = cost;
                    t = t_r;
                }
            }
        }
        return t;
    }

    public void appendTask(MemPoint p, MemMap map) {
        Station s = ((Station) map.getCell(p));
        if (s != null) {
            Task t = s.getTask();
            if (t != null) {
                Iterator it = this.task_list.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    Task t_r = (Task) pairs.getKey();
                    if (t_r == t) {
                        it.remove();
                    }
                }
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
        return null;
    }
}
