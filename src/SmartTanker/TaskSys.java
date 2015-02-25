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
    HashMap<Task, MemPoint> task_list = new HashMap<Task, MemPoint>();

    public Boolean scanTaskList() {
        Iterator it = this.task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t_r = (Task) pairs.getKey();
            if (t_r.isComplete()) {
                it.remove();
            }
        }
        return this.task_list.size() == 0;
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
}
