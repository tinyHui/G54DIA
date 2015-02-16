package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

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
        DRIVE_EXPLORE = 3,
        DRIVE_TO_PUMP = 4,
        DRIVE_TO_FACILITY = 5,
        DRIVE_TO_HISTORY = 6;

    final MemPoint FUEL_PUMP = new MemPoint(0, 0);

    int mode = DRIVE_EXPLORE;
    boolean map_explored = false;

    boolean enough_fuel = false;

    MemMap map = new MemMap();
    Driver driver = new Driver();
    Cell current_cell;

    MemPoint current_point = driver.getCurrentPoint();
    MemPoint history_point;
    MemPoint target_point;
    MemPoint task_point;
    MemPoint nearest_well;

    TaskSys ts = new TaskSys();
    Task current_t;

    int direction = 1;
    int one_direction_count = 0;

    int water_level = 0;
    int fuel_level = 0;
    long time_left = DURATION;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {
        updateState(view, timestep);
        recordMap(view);

        this.mode = arbitrator();

        switch (this.mode) {
            case DRIVE_EXPLORE:
                exploreWorld();
            case DRIVE_TO_FACILITY:
            case DRIVE_TO_HISTORY:
                return this.driver.driveTo(this.target_point);
            case DRIVE_TO_PUMP:
                return this.driver.driveTo(this.FUEL_PUMP);
            case REFUEL:
                return new RefuelAction();
            case DELIVER_WATER:
                return new DeliverWaterAction(this.current_t);
            case LOAD_WATER:
                return new LoadWaterAction();
            default:
                throw new ValueException("Unrecognised mode");
        }
    }

    private void updateState(Cell[][] view, long timestep) {
        this.time_left = DURATION - timestep;

        this.target_point = null;
        this.current_point = this.driver.getCurrentPoint();
        this.current_cell = this.getCurrentCell(view);

        this.water_level = this.getWaterLevel();
        this.fuel_level = this.getFuelLevel();

        this.enough_fuel = checkFuel();

        this.current_t = this.ts.scanTaskList(this.current_point);
        if (this.current_t != null) {
            this.task_point = this.ts.getPoint(this.current_t);
            int direction_of_task = this.driver.getDirection(this.task_point);
            MemPoint nearest_well_task = this.map.getNearestWell(this.task_point);
            MemPoint nearest_well_current = this.map.getNearestWell(this.current_point);
            if ((this.driver.getDirection(nearest_well_task) == direction_of_task &&
                    this.driver.getDirection(nearest_well_current) == direction_of_task) ||
                    (this.driver.getDirection(nearest_well_task) != direction_of_task &&
                            this.driver.getDirection(nearest_well_current) != direction_of_task) ) {
                this.nearest_well = (MemPoint) this.map.nearerPoint(this.current_point, nearest_well_task, nearest_well_current).clone();
            } else if (this.driver.getDirection(nearest_well_task) == direction_of_task) {
                this.nearest_well = (MemPoint) nearest_well_task.clone();
            } else if (this.driver.getDirection(nearest_well_current) == direction_of_task) {
                this.nearest_well = (MemPoint) nearest_well_current.clone();
            }
        } else {
            this.task_point = null;
        }
    }

    private boolean checkFuel() {
        int away_fuel_x = Math.abs(this.current_point.x);
        int away_fuel_y = Math.abs(this.current_point.y);
        int distance = away_fuel_x > away_fuel_y ? away_fuel_x : away_fuel_y;
        // coordinate start from 0
        return this.fuel_level > distance + 1;
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
        move this routine, clock wise
        |\  /|
        | \/ |
        | /\ |
        |/  \|
         */
        this.one_direction_count++;
        if (this.one_direction_count >= VIEW_RANGE * 2) {
            this.one_direction_count = 1;
            this.direction++;
        }

        if (this.direction > 8) {
            this.map_explored = true;
            this.direction = 1;
        }

        switch (this.direction) {
            case 1:
            case 8:
                this.target_point = new MemPoint(this.current_point.x+1,
                        this.current_point.y+1);
                break;
            case 2:
            case 7:
                this.target_point = new MemPoint(this.current_point.x+1,
                        this.current_point.y-1);
                break;
            case 3:
            case 6:
                this.target_point = new MemPoint(this.current_point.x-1,
                        this.current_point.y-1);
                break;
            case 4:
            case 5:
                this.target_point = new MemPoint(this.current_point.x-1,
                        this.current_point.y+1);
                break;
            default:
                throw new ValueException("Unrecognised Direction");
        }
    }

    private int arbitrator() {
        // at history point and tanker not going back refuel,
        // set history point to null
        if (this.current_point.equals(this.history_point) &&
                mode != DRIVE_TO_PUMP) {
            this.history_point = null;
        }

        // at fuel pump, gas not max
        if (this.current_cell instanceof FuelPump &&
                this.fuel_level < MAX_FUEL / 2) {
            return REFUEL;
        } else
        // at water well, water not max
        if (this.current_cell instanceof Well &&
                this.water_level < MAX_WATER) {
            return LOAD_WATER;
        } else
//        // at station, no task occupied
//        if (this.current_cell instanceof Station) {
//
//        } else
        // at task cell and have water, give all water
        if (this.current_point.equals(this.task_point) &&
                this.water_level > 0) {
            return DELIVER_WATER;
        }

        // not enough fuel to go back and refill
        if (!this.enough_fuel &&
                !(this.current_cell instanceof FuelPump)) {
            // remain fuel can only go back fuel pump directly and current not at fuel pump
            if (history_point == null) {
                this.history_point = (MemPoint) this.current_point.clone();
            }
            return DRIVE_TO_PUMP;
        }

        if (this.current_t == null) {
            if (this.history_point != null &&
                    this.mode != DRIVE_TO_FACILITY) {
                this.target_point = (MemPoint) this.history_point.clone();
                return DRIVE_TO_HISTORY;
            } else
            // find nearest_well
            if (this.nearest_well != null &&
                    this.water_level < MAX_WATER) {
                if (this.history_point == null) {
                    this.history_point = (MemPoint) this.current_point.clone();
                }
                this.target_point = this.nearest_well;
                return DRIVE_TO_FACILITY;
            }
        } else {
            // enough water to finish task
            if (this.water_level >= this.current_t.getWaterDemand()) {
                if (this.current_point.equals(this.task_point)) {
                    // at task cell, finish it
                    this.task_point = null;
                    this.current_t = null;
                    return DELIVER_WATER;
                } else {
                    // not at task cell, go there
                    this.target_point = (MemPoint) this.task_point.clone();
                    return DRIVE_TO_FACILITY;
                }
            } else {
                // found nearest well
                if (this.nearest_well != null) {
                    // go refill water
                    this.target_point = this.nearest_well;
                    return DRIVE_TO_FACILITY;
                } else {
                    // continue search water
                    return DRIVE_EXPLORE;
                }
            }
        }
        return DRIVE_EXPLORE;
    }
}
