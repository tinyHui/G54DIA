package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

import java.util.Random;

/**
 * Created by JasonChen on 2/12/15.
 */

public class SmartTanker extends Tanker {
    final static int DURATION = 10 * 10000;
    final static int
            // action mode
            REFUEL = 0,
            LOAD_WATER = 1,
            DELIVER_WATER = 2,
            // Driving mode
            EXPLORE = 3,
            DRIVE_TO_PUMP = 4,
            DRIVE_TO_FACILITY = 5;

    final static MemPoint FUEL_PUMP = new MemPoint(0, 0);

    int mode = EXPLORE;
    int mode_prev = -1;

    MemMap map = new MemMap();
    TaskSys ts = new TaskSys();
    Driver driver = new Driver(map, ts);
    Cell current_cell;
    Task t;

    MemPoint current_point = driver.getCurrentPoint();
    MemPoint target_point = (MemPoint) FUEL_PUMP.clone();
    MemPoint explore_target_point = (MemPoint) FUEL_PUMP.clone();
    MemPoint[] explore_targets = {new MemPoint(19, 19),
                                  new MemPoint(38, 0),
                                  new MemPoint(19, -19),
                                  new MemPoint(0, 0),
                                  new MemPoint(-19, 19),
                                  new MemPoint(-38, 0),
                                  new MemPoint(-19, -19),
                                  new MemPoint(0, 0),
                                  new MemPoint(-24, 12),
                                  new MemPoint(0, 38),
                                  new MemPoint(24, 12),
                                  new MemPoint(0, 0),
                                  new MemPoint(24, -12),
                                  new MemPoint(0, -37),
                                  new MemPoint(-24, -12),
                                  new MemPoint(0, 0)};

    int explore_count = -1;
    int water_level = -1;
    int fuel_level = -1;
    Boolean task_list_empty = true;
    long time_left = DURATION;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long time_step) {
        Action act;
        recordMap(view);
        updateState(view, time_step);

        this.mode = arbitrator();
        checkFuel();
        System.out.println(this.fuel_level + "\t" + this.mode + "\t" + "(" + this.current_point.x + ", " + this.current_point.y + ")");

        switch (this.mode) {
            case EXPLORE:
                exploreWorld();
            case DRIVE_TO_FACILITY:
                act = this.driver.driveTo(this.target_point);
                break;
            case DRIVE_TO_PUMP:
                act = this.driver.driveTo(FUEL_PUMP);
                break;
            case REFUEL:
                act = new RefuelAction();
                break;
            case LOAD_WATER:
                act = new LoadWaterAction();
                break;
            case DELIVER_WATER:
                act = new DeliverWaterAction(this.t);
                break;
            default:
                throw new ValueException("Unrecognised mode");
        }
        this.mode_prev = this.mode;
        return act;
    }

    private void updateState(Cell[][] view, long time_step) {
        this.time_left = DURATION - time_step;

        this.current_point = this.driver.getCurrentPoint();
        this.current_cell = this.getCurrentCell(view);

        this.water_level = this.getWaterLevel();
        this.fuel_level = this.getFuelLevel();

        this.task_list_empty = this.ts.scanTaskList();
    }

    private void checkFuel() {
        int cost = this.current_point.calcDistance(this.target_point) +
                this.target_point.calcDistance(FUEL_PUMP) + 1;

        if (cost >= this.fuel_level &&
                !(this.current_cell instanceof FuelPump)) {
            this.mode = DRIVE_TO_PUMP;
        }
    }

    private void recordMap(Cell[][] view) {
        for (int y=-VIEW_RANGE; y < VIEW_RANGE; y++) {
            for (int x=-VIEW_RANGE; x < VIEW_RANGE; x++) {
                int real_x = this.current_point.x + x;
                int real_y = this.current_point.y - y;
                MemPoint point = new MemPoint(real_x, real_y);
                Cell cell = view[VIEW_RANGE + x][VIEW_RANGE + y];
                if (cell instanceof Station) {
                    this.map.appendStation(point, (Station) cell);
                    this.ts.appendTask(point, this.map);
                } else if (cell instanceof Well) {
                    this.map.appendWell(point, (Well) cell);
                }
            }
        }
    }

    private void exploreWorld() {
        if (current_point.equals(this.explore_target_point)) {
            this.explore_count++;
        }

        if (this.explore_count > 15) {
            this.explore_count = 0;
        }

        this.explore_target_point = this.explore_targets[this.explore_count];

        this.target_point = (MemPoint) this.explore_target_point.clone();
    }

    private int arbitrator() {
        // at fuel pump, gas not max
        if (this.current_cell instanceof FuelPump &&
                this.mode_prev != REFUEL &&
                this.fuel_level < MAX_FUEL) {
            return REFUEL;
        } else
        // at water well, water not max
        if (this.current_cell instanceof Well &&
                this.water_level < MAX_WATER) {
            return LOAD_WATER;
        }

        // task list not empty
        if (!this.task_list_empty) {
            TaskPair task_pair = this.driver.getNextPoint(this.current_point, this.water_level, this.fuel_level, this.time_left);
            this.t = task_pair.getTask();
            MemPoint task_point = task_pair.getTaskPoint();
            MemPoint well_point = task_pair.getWellPoint();

            // found best task to finish
            if (this.t != null && task_point != null) {
                if (well_point != null) {
                    // need well
                    this.target_point = well_point;
                    return DRIVE_TO_FACILITY;
                } else
                    // no need go well
                    if (!this.current_point.equals(task_point)) {
                        // not at task point
                        this.target_point = task_point;
                        return DRIVE_TO_FACILITY;
                    } else {
                        // at task cell
                        return DELIVER_WATER;
                    }
            }
        }

        return EXPLORE;
    }
}
