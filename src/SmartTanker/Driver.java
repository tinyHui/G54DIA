package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Action;
import uk.ac.nott.cs.g54dia.library.MoveAction;
import uk.ac.nott.cs.g54dia.library.Task;
import uk.ac.nott.cs.g54dia.library.Well;

import java.util.*;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
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

//    public int inverseDirection(int direction) {
//        int d;
//        switch (direction) {
//            case MoveAction.NORTHEAST:
//                d = MoveAction.SOUTHWEST;
//                break;
//            case MoveAction.SOUTHEAST:
//                d = MoveAction.NORTHWEST;
//                break;
//            case MoveAction.EAST:
//                d = MoveAction.WEST;
//                break;
//            case MoveAction.NORTHWEST:
//                d = MoveAction.SOUTHEAST;
//                break;
//            case MoveAction.SOUTHWEST:
//                d = MoveAction.NORTHEAST;
//                break;
//            case MoveAction.WEST:
//                d = MoveAction.EAST;
//                break;
//            case MoveAction.NORTH:
//                d = MoveAction.SOUTH;
//                break;
//            case MoveAction.SOUTH:
//                d = MoveAction.NORTH;
//                break;
//            default:
//                return -1;
//        }
//        return d;
//    }

    public Action driveTo(MemPoint target) {
        int direction = this.getDirection(target);
        this.current_point.moveTo(direction);
        return new MoveAction(direction);
    }

    public MemPoint getCurrentPoint() {
        return current_point;
    }

    public MemPoint getMidWell(MemPoint start, MemPoint target) {
        // found best well to go
        int min_distance = 101;
        int distance;
        MemPoint midpoint = null;
        for (Map.Entry<MemPoint, Well> pairs : this.map.well_list.entrySet()) {
            MemPoint w_p = pairs.getKey();
            distance = start.calcDistance(w_p) + w_p.calcDistance(target);
            if (distance < min_distance) {
                min_distance = distance;
                midpoint = w_p;
            }
        }
        return midpoint;
    }

    public void plan(Stack<TaskPair> plan_list, int water_level, long step_left) {
        HashMap<Task, MemPoint> task_list = this.ts.scanTaskList();
        plan_list.clear();

        for (Map.Entry<Task, MemPoint> pairs : task_list.entrySet()) {
            Task t = pairs.getKey();
            MemPoint p = pairs.getValue();

            plan_list.push(new TaskPair(p, t));

            if (t.getRequired() > water_level) {
                MemPoint w_p = getMidWell(this.current_point, p);
                plan_list.push(new TaskPair(w_p, null));
            }
        }
    }
}
