package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by JasonChen on 2/12/15.
 */

public class SmartTanker extends Tanker {
    final static int
            // action mode
            REFUEL = 0,
            LOAD_WATER = 1,
            DELIVER_WATER = 2,
            // Driving mode
            EXPLORE = 3,
            DRIVE_TO_PUMP = 4,
            DRIVE_TO_FACILITY = 5;

    int mode = EXPLORE;

    MemMap map = new MemMap();
    TaskSys ts = new TaskSys();
    Driver driver = new Driver(map, ts);

    Cell current_cell;
    MemPoint current_point = driver.getCurrentPoint();
    TaskPair current_task_pair = new TaskPair();
    Task current_task;

    MemPoint target_point = (MemPoint) MemPoint.FUEL_PUMP.clone();

    int explore_count = -1;
    MemPoint explore_target_point = (MemPoint) MemPoint.FUEL_PUMP.clone();
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

    Queue<TaskPair> plan_list = new LinkedList<TaskPair>();

    Status status = new Status();

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long time_step) {
        Action act;
        recordMap(view);
        updateState(view, time_step);

        this.mode = arbitrator();

        switch (this.mode) {
            case EXPLORE:
            case DRIVE_TO_FACILITY:
                act = this.driver.driveTo(this.target_point);
                break;
            case DRIVE_TO_PUMP:
                act = this.driver.driveTo(MemPoint.FUEL_PUMP);
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
        return act;
    }

    private void updateState(Cell[][] view, long time_step) {
        this.current_point = this.driver.getCurrentPoint();
        this.current_cell = this.getCurrentCell(view);
        status.water_level = this.getWaterLevel();
        status.fuel_level = this.getFuelLevel();
        status.completed_count = this.getCompletedCount();
        boolean new_plan = this.driver.plan(this.plan_list, this.status);
        if (new_plan) {
            // plan updated, abort current target
            this.current_task_pair = new TaskPair();
        }
    }

    private boolean enoughFuel() {
        int cost = this.current_point.calcDistance(this.target_point) +
                this.target_point.calcDistanceToFuel();

        return !(cost > status.fuel_level &&
                !(this.current_cell instanceof FuelPump));
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
            return this.plan_list.poll();
        }
        return null;
    }

    private int arbitrator() {
        int command = EXPLORE;

        // have plan list
        if (!this.plan_list.isEmpty() ||
                !this.current_task_pair.isNull()) {
            // no plan occupied, try to read a new one
            if (this.current_task_pair.isNull()) {
                this.current_task_pair = nextPlanPoint();
                System.out.println("\t\tRemain: " + this.plan_list.size());
                System.out.println("\t\t\ttarget: " + this.current_task_pair.p.x + ", " + this.current_task_pair.p.y);
            }

            // have plan occupied
            if (!this.current_task_pair.isNull()) {
                if (!this.current_point.equals(this.current_task_pair.p)) {
                    // not at task point
                    this.target_point = (MemPoint) this.current_task_pair.p.clone();
                    command = DRIVE_TO_FACILITY;
                } else {
                    // at plan point
                    if (this.current_task_pair.t == null) {
                        // no task
                        this.current_task_pair = new TaskPair();
                        if (this.current_cell instanceof FuelPump) {
                            System.out.println("\t\t\t" + "Refuel At: " + this.current_point.x + ", " + this.current_point.y);
                            return REFUEL;
                        } else if(this.current_cell instanceof Well) {
                            System.out.println("\t\t\t" + "Load At: " + this.current_point.x + ", " + this.current_point.y);
                            return LOAD_WATER;
                        }
                    } else {
                        System.out.println("\t\t\t" + "Deliver At: " + this.current_point.x + ", " + this.current_point.y);
                        this.current_task = this.current_task_pair.t;
                        this.current_task_pair = new TaskPair();
                        status.delivered_water += Math.min(this.current_task.getRequired(), status.water_level);
                        return DELIVER_WATER;
                    }
                }
            }
        }

        if (command == EXPLORE) {
            this.target_point = exploreWorld();
            if (!enoughFuel()) {
                command = DRIVE_TO_PUMP;
            }
            // at fuel pump, gas not max
            if (this.current_cell instanceof FuelPump &&
                    status.fuel_level < MAX_FUEL) {
                return REFUEL;
            } else
                // at water well, water not max
                if (this.current_cell instanceof Well &&
                        status.water_level < MAX_WATER) {
                    return LOAD_WATER;
                }
        }

        return command;
    }
}
