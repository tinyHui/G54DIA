package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Action;
import uk.ac.nott.cs.g54dia.library.MoveAction;
import uk.ac.nott.cs.g54dia.library.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
    final MemPoint FUEL_PUMP = new MemPoint(0, 0);

    MemPoint current_point = new MemPoint(0, 0);
    MemMap map;
    TaskSys ts;

    public Driver(MemMap map, TaskSys ts) {
        this.map = map;
        this.ts = ts;
    }

    public int getDirection(MemPoint target) {
        if (target == null) {
            return -1;
        }
        int dx = target.x - current_point.x;
        int dy = target.y - current_point.y;

        if (dx > 0 && dy > 0) {
            return MoveAction.NORTHEAST;
        } else if (dx > 0 && dy < 0) {
            return MoveAction.SOUTHEAST;
        } else if (dx > 0 && dy == 0) {
            return MoveAction.EAST;
        } else if (dx < 0 && dy > 0) {
            return MoveAction.NORTHWEST;
        } else if (dx < 0 && dy < 0) {
            return MoveAction.SOUTHWEST;
        } else if (dx < 0 && dy == 0) {
            return MoveAction.WEST;
        } else if (dx == 0 && dy > 0) {
            return MoveAction.NORTH;
        } else if (dx == 0 && dy < 0) {
            return MoveAction.SOUTH;
        } else {
            return -1;
        }
    }

    public Action driveTo(MemPoint target) {
        int direction = this.getDirection(target);
        this.current_point.moveTo(direction);
        return new MoveAction(direction);
    }

    public MemPoint getCurrentPoint() {
        return current_point;
    }

    public MemPoint getMidWell(MemPoint target) {
        // found best well to go
        int min_distance = 101;
        int distance;
        MemPoint midpoint = null;
        Iterator it = this.map.well_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            MemPoint p_r = (MemPoint) pairs.getKey();
            distance = this.current_point.calcDistance(p_r) + p_r.calcDistance(target);
            if (distance < min_distance) {
                min_distance = distance;
                midpoint = p_r;
            }
        }
        return midpoint;
    }

    public TaskPair getNextPoint(MemPoint p, int water_level, int fuel_level, long time_left) {
        long left_step_num = fuel_level < time_left ? fuel_level : time_left;
        TaskPair task_pair = new TaskPair(null, null, null);
        int score;
        int step_to_task;
        int score_max = 0;

        HashMap<Task, MemPoint> sim_task_list = (HashMap<Task, MemPoint>) this.ts.task_list.clone();
        Iterator it = sim_task_list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Task t = (Task) pairs.getKey();
            MemPoint tp = (MemPoint) pairs.getValue();
            MemPoint wp = null;

            // check how much water required by this task
            score = t.getRequired();

            if (score <= water_level) {
                // tanker water storage enough for task water requirement
                step_to_task = p.calcDistance(tp);
            } else {
                // not enough
                wp = getMidWell(tp);
                if (wp == null) {
                    continue;
                }
                step_to_task = p.calcDistance(wp) + wp.calcDistance(tp);
            }

            step_to_task += tp.calcDistance(FUEL_PUMP);
            score -= step_to_task;

            if (step_to_task > left_step_num) {
                // too far to go
                score = -1;
            }

            if (score > score_max) {
                score_max = score;
                task_pair.setTask(t);
                task_pair.setTaskPoint(tp);
                task_pair.setWellPoint(wp);
            }
        }

        return task_pair;
    }
}
