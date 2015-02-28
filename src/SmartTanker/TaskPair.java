package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Task;

/**
 * Created by JasonChen on 2/25/15.
 */
public class TaskPair implements Cloneable{
    public MemPoint p;
    public Task t;
    public int f;

    public TaskPair() {
    }

    public TaskPair(MemPoint p, Task t, int f) {
        this.p = (MemPoint) p.clone();
        this.t = t;
        this.f = f;
    }

    public boolean isNull() {
        return this.p == null && this.t == null;
    }

    public Object clone() {
        return new TaskPair(p,t, f);
    }
}
