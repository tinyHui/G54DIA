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
    final static int
            EXPLORE_NORTH       =   0,
            EXPLORE_SOUTH       =   1,
            EXPLORE_EAST        =   2,
            EXPLORE_WEST        =   3,
            EXPLORE_NORTHEAST   =   4,
            EXPLORE_NORTHWEST   =   5,
            EXPLORE_SOUTHEAST   =   6,
            EXPLORE_SOUTHWEST   =   7;

    final MemPoint FUEL_PUMP = new MemPoint(0, 0);

    int mode = EXPLORE;
    int mode_prev = -1;

    boolean enough_fuel = false;

    MemMap map = new MemMap();
    Driver driver = new Driver();
    Cell current_cell;

    MemPoint current_point = driver.getCurrentPoint();
    MemPoint target_point = (MemPoint) FUEL_PUMP.clone();
    MemPoint explore_start_point = (MemPoint) FUEL_PUMP.clone();

    TaskSys ts = new TaskSys();
    Task current_t;

    int explore_direction = -1;
    int explore_direction_prev = explore_direction;
    int explore_one_direction_count = 0;

    int water_level = 0;
    int fuel_level = 0;
    long time_left = DURATION;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {
        recordMap(view);
        updateState(view, timestep);
        Action act;

        this.mode = arbitrator();
//        this.mode = EXPLORE;
        switch (this.mode) {
            case EXPLORE:
                exploreWorld();
                act = this.driver.driveTo(this.target_point);
                break;
            case DRIVE_TO_FACILITY:
                act = this.driver.driveTo(this.target_point);
                break;
            case DRIVE_TO_PUMP:
                act = this.driver.driveTo(this.FUEL_PUMP);
                break;
            case REFUEL:
                act = new RefuelAction();
                break;
            case DELIVER_WATER:
                act = new DeliverWaterAction(this.current_t);
                break;
            case LOAD_WATER:
                act = new LoadWaterAction();
                break;
            default:
                throw new ValueException("Unrecognised mode");
        }
        this.mode_prev = this.mode;
        return act;
    }

    private void updateState(Cell[][] view, long timestep) {
        this.time_left = DURATION - timestep;

        this.current_point = this.driver.getCurrentPoint();
        this.current_cell = this.getCurrentCell(view);

        this.water_level = this.getWaterLevel();
        this.fuel_level = this.getFuelLevel();

        this.enough_fuel = checkFuel();

        this.current_t = this.ts.scanTaskList(this.current_point);
    }

    private boolean checkFuel() {
        if (this.mode == DRIVE_TO_PUMP) {
            return false;
        }
        int cost;

        // add extra one fuel for deliver water or refill water
        cost = this.current_point.calcDistance(this.target_point) +
                this.target_point.calcDistance(FUEL_PUMP) + 1;

        return cost < this.fuel_level;
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
        /*
        move alone this routine,
        -------------
        start from current point,
        random choose the direction
        */
        Random generator = new Random();
        int explore_limit = MAX_FUEL / 2 - VIEW_RANGE - 1;

        // start a new explore
        if (!(this.mode_prev == LOAD_WATER ||
                this.mode_prev == EXPLORE) ||
                this.current_point.equals(this.explore_start_point) ||
                this.explore_one_direction_count == 0) {
            do {
                this.explore_direction = generator.nextInt(8);
            } while (this.explore_direction == this.explore_direction_prev);
            switch (this.explore_direction) {
                case EXPLORE_NORTH:
                case EXPLORE_SOUTH:
                    explore_limit -= this.current_point.y;
                    break;
                case EXPLORE_EAST:
                case EXPLORE_WEST:
                    explore_limit -= this.current_point.x;
                    break;
                case EXPLORE_NORTHEAST:
                case EXPLORE_NORTHWEST:
                case EXPLORE_SOUTHEAST:
                case EXPLORE_SOUTHWEST:
                    explore_limit = Math.min(explore_limit - this.current_point.x,
                            explore_limit - this.current_point.y);
                    break;
            }
            this.explore_one_direction_count = 0;
            this.explore_start_point = (MemPoint) this.current_point.clone();
        }

        this.explore_one_direction_count++;
        if (this.explore_one_direction_count >= explore_limit) {
            this.explore_one_direction_count = 0;
        }
        if (explore_limit < 0) {
            this.explore_direction = this.driver.inverseDirection(this.explore_direction);
        }

        this.target_point = (MemPoint) this.current_point.clone();
        this.target_point.moveTo(this.explore_direction);
        this.explore_direction_prev = this.driver.getDirection(this.explore_start_point);
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
        } else
        // at task cell and have water, give all water
        if (this.current_point.equals(this.ts.getPoint(this.current_t)) &&
                this.water_level > 0) {
            return DELIVER_WATER;
        }

        // not enough fuel to go back and refill
        if (!this.enough_fuel &&
                !(this.current_cell instanceof FuelPump)) {
            // remain fuel can only go back fuel pump directly and current not at fuel pump
            return DRIVE_TO_PUMP;
        }

        if (this.current_t != null) {
            MemPoint task_point = this.ts.getPoint(this.current_t);
            // enough water to finish task
            if (this.current_point.equals(task_point)) {
                // at task cell, finish it
                return DELIVER_WATER;
            }

            if (this.water_level >= this.current_t.getRequired()) {
                // not at task cell, go there
                this.target_point = (MemPoint) task_point.clone();
                return DRIVE_TO_FACILITY;
            } else {
                MemPoint nearest_well_task = this.map.getNearestWell(task_point);
                MemPoint nearest_well_current = this.map.getNearestWell(this.current_point);
                MemPoint nearest_well = this.map.nearestToGo(this.current_point, task_point,
                        nearest_well_task, nearest_well_current);

                // found nearest well
                if (nearest_well != null) {
                    // go refill water
                    this.target_point = nearest_well;
                    return DRIVE_TO_FACILITY;
                } else {
                    // continue search water
                    return EXPLORE;
                }
            }
        }

        return EXPLORE;
    }
}
