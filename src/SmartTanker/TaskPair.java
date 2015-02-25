package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Task;

/**
 * Created by JasonChen on 2/25/15.
 */
public class TaskPair {
    Task t;
    MemPoint t_p;
    MemPoint w_p;

    public TaskPair(Task t, MemPoint t_p, MemPoint w_p) {
        this.t = t;
        this.t_p = t_p;
        this.w_p = w_p;
    }

    public Task getTask() {
        return t;
    }

    public MemPoint getTaskPoint() {
        return t_p;
    }

    public MemPoint getWellPoint() {
        return w_p;
    }

    public void setTask(Task t) {
        this.t = t;
    }

    public void setTaskPoint(MemPoint t_p) {
        this.t_p = t_p;
    }

    public void setWellPoint(MemPoint w_p) {
        this.w_p = w_p;
    }
}
