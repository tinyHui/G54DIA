package SmartTanker;

import SmartTanker.AntColony.ACO;
import uk.ac.nott.cs.g54dia.library.Action;
import uk.ac.nott.cs.g54dia.library.MoveAction;
import uk.ac.nott.cs.g54dia.library.Task;

import java.util.*;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
    MemPoint current_point = new MemPoint(0, 0);
    MemMap map;
    TaskSys ts;
    int prev_task_list_size = 0;

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

    public boolean plan(Queue<TaskPair> plan_list, Status status) {
        boolean new_plan = false;
        HashMap<Task, MemPoint> task_list = this.ts.scanTaskList();
        if (this.prev_task_list_size < task_list.size()) {
            // more task append, re-plan
            ACO aco = new ACO(this.map, this.current_point, status);
            aco.start(plan_list, task_list);
            new_plan = true;
        }
        this.prev_task_list_size = task_list.size();
        return new_plan;
    }
}
