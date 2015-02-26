package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

import java.util.Stack;

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
    MemPoint current_point = driver.getCurrentPoint();
    TaskPair current_task_pair;
    Task current_task;

    MemPoint target_point = (MemPoint) FUEL_PUMP.clone();

    int explore_count = -1;
    MemPoint explore_target_point = (MemPoint) FUEL_PUMP.clone();
    MemPoint[] explore_target_point_list = {new MemPoint(19, 25),
                                            new MemPoint(38, 0),
                                            new MemPoint(19, -25),
                                            new MemPoint(0, 0),
                                            new MemPoint(-19, 25),
                                            new MemPoint(-38, 0),
                                            new MemPoint(-19, -25),
                                            new MemPoint(0, 0),
                                            new MemPoint(-25, 19),
                                            new MemPoint(0, 38),
                                            new MemPoint(25, 19),
                                            new MemPoint(0, 0),
                                            new MemPoint(25, -19),
                                            new MemPoint(0, -38),
                                            new MemPoint(-25, -19),
                                            new MemPoint(0, 0)};

    Stack<TaskPair> plan_list = new Stack<TaskPair>();

    int water_level = -1;
    int fuel_level = -1;
    int completed_count = 0;
    int delivered_water = 0;
    long time_left = DURATION;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long time_step) {
        Action act;
        recordMap(view);
        updateState(view, time_step);

        this.mode = arbitrator();
        System.out.println(this.fuel_level + "\t" +
                this.mode + "\t" +
                this.current_point.x + "," + this.current_point.y + "\t" +
                this.target_point.x + "," + this.target_point.y + "\t" +
                (this.current_point.calcDistance(this.target_point) +
                        this.target_point.calcDistance(FUEL_PUMP)));

        switch (this.mode) {
            case EXPLORE:
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
                act = new DeliverWaterAction(this.current_task);
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
        this.completed_count = this.getCompletedCount();
        this.driver.plan(this.plan_list, this.water_level, Math.min(this.fuel_level, this.time_left));
    }

    private boolean enoughFuel(int mode) {
        int cost = this.current_point.calcDistance(this.target_point) +
                this.target_point.calcDistance(FUEL_PUMP);

        if (cost > this.fuel_level &&
                !(this.current_cell instanceof FuelPump)) {
            return false;
        }

        return true;
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

    private MemPoint exploreWorld() {
        if (current_point.equals(this.explore_target_point)) {
            this.explore_count++;
        }

        if (this.explore_count >= this.explore_target_point_list.length) {
            this.explore_count = 0;
        }

        this.explore_target_point = (MemPoint) this.explore_target_point_list[this.explore_count].clone();
        return (MemPoint) this.explore_target_point.clone();
    }

    private TaskPair nextPlanPoint() {
        if (!this.plan_list.isEmpty()) {
            return this.plan_list.pop();

        }
        return null;
    }

    private int arbitrator() {
        int command = EXPLORE;
        this.target_point = exploreWorld();

        // at fuel pump, gas not max
        if (this.current_cell instanceof FuelPump &&
                this.mode_prev != REFUEL &&
                this.fuel_level < MAX_FUEL) {
            return REFUEL;
        } else
        // at water well, water not max
        if (this.current_cell instanceof Well &&
                this.water_level < MAX_WATER &&
                this.mode_prev == EXPLORE) {
            return LOAD_WATER;
        }
        else
        // have plan list
        if (!this.plan_list.isEmpty()) {
            // no plan occupied, try to read a new one
            if (this.current_task_pair == null) {
                this.current_task_pair = nextPlanPoint();
            }

            // have plan occupied
            if (this.current_task_pair != null) {
                if (!this.current_point.equals(this.current_task_pair.p)) {
                    // not at task point
                    this.target_point = (MemPoint) this.current_task_pair.p.clone();
                    command = DRIVE_TO_FACILITY;
                } else {
                    // at plan point
                    if (this.current_task_pair.t == null) {
                        // no task, means this is a well
                        this.current_task_pair = null;
                        return LOAD_WATER;
                    } else {
                        this.current_task = this.current_task_pair.t;
                        this.current_task_pair =  null;
                        this.delivered_water += Math.min(this.current_task.getRequired(), this.water_level);
                        return DELIVER_WATER;
                    }
                }
            }
        }

        if (!enoughFuel(command)) {
            command = DRIVE_TO_PUMP;
        }

        return command;
    }
}
