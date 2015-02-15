package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

/**
 * Created by JasonChen on 2/12/15.
 */

public class SmartTanker extends Tanker {
    int DURATION = 10 * 10000;
    int direction = 1;
    int explore_count = 1;
    int water_level = 0;
    int fuel_level = 0;
    long time_left = DURATION;

    Task t = null;
    Cell current_cell = null;
    Point current_point = null;
    Point nearest_station = null;
    Point nearest_well = null;
    Point history_point = null;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {
        this.time_left = DURATION - timestep;



        this.current_cell = this.getCurrentCell(view);

        this.water_level = this.getWaterLevel();
        this.fuel_level = this.getFuelLevel();
        
        scanView(view);

        // not enough fuel to go back and refill
        if (this.fuel_level < fuelPumpDistance() &&
                !(this.current_cell instanceof FuelPump) ) {
            // remain fuel can only go back fuel pump directly and current not at fuel pump
            this.history_point = this.current_point;
            return new MoveTowardsAction(FUEL_PUMP_LOCATION);
        }

        // at fuel pump
        if (this.current_cell instanceof FuelPump &&
                this.fuel_level < MAX_FUEL - 2) {
            // no gas, refuel
            return new RefuelAction();
        } else
        // if at water well, refill water
        if (this.current_cell instanceof Well &&
                this.water_level < MAX_WATER) {
            this.history_point = this.current_point;
            this.nearest_well = null;
            return new LoadWaterAction();
        } else
        // if at station and no task currently
        if (this.current_cell instanceof Station
                && this.t == null) {
            this.nearest_station = null;
            Station station = ((Station) this.current_cell);
            this.t = station.getTask();
        }

        if (this.t == null) {
            if (nearest_station != null) {
                // find nearest_station
                return new MoveTowardsAction(nearest_station);
            } else if (nearest_well != null &&
                    this.water_level < MAX_WATER) {
                // find nearest well and water level not at maximum
                return new MoveTowardsAction(nearest_well);
            } else if (this.history_point != null) {
                // if got history point, go back
                Point h = this.history_point;
                this.history_point = null;
                return new MoveTowardsAction(h);
            } else {
                // nothing find, keep explore
                return new MoveAction(exploreWorld());
            }
        } else if (this.t.isComplete()) {
            // clear current task, keep exploring
            this.t = null;
            return new MoveAction(this.exploreWorld());
        } else {
            // enough water to finish task
            if (this.water_level > this.t.getWaterDemand()) {
                if (this.current_point.equals(this.t.getStationPosition())) {
                    // at task cell, finish it
                    return new DeliverWaterAction(this.t);
                } else {
                    // not at task cell, go there
                    return new MoveTowardsAction(this.t.getStationPosition());
                }
            } else {
                // give all water then refill water
                if (this.water_level == 0) {
                    if (this.nearest_well != null) {
                        return new MoveTowardsAction(this.nearest_well);
                    } else {
                        return new MoveAction(this.exploreWorld());
                    }
                } else {
                    return new DeliverWaterAction(this.t);
                }
            }
        }
    }

    private void scanView(Cell[][] view) {
        // spiral find station with task and water well
        for (int y=-VIEW_RANGE; y < VIEW_RANGE; y++) {
            for (int x=-VIEW_RANGE; x < y; x++) {
                Cell cell = view[VIEW_RANGE + x][VIEW_RANGE + y];
                if (nearest_station == null &&
                        cell instanceof Station &&
                        ((Station) cell).getTask() != null) {
                    nearest_station = cell.getPoint();
                }
                if (cell instanceof Well &&
                        nearest_well == null) {
                    nearest_well = cell.getPoint();
                }
            }
        }
    }

    private int exploreWorld() {
        /*
        move this routine, clock wise
        |\  /|
        | \/ |
        | /\ |
        |/  \|
         */
        int d = 0;
        int explore_limit = 0;
        switch (this.direction) {
            case 1:
            case 8:
                explore_limit = (int)(VIEW_RANGE * Math.sqrt(2));
                d = MoveAction.NORTHEAST;
                break;
            case 2:
            case 3:
            case 6:
            case 7:
                explore_limit = VIEW_RANGE;
                d = MoveAction.SOUTH;
                break;
            case 4:
            case 5:
                explore_limit = (int)(VIEW_RANGE * Math.sqrt(2));
                d = MoveAction.NORTHWEST;
                break;
            default:
                throw new ValueException("Unrecognised Direction");
        }

        if (this.explore_count == explore_limit) {
            this.direction++;
            this.explore_count = 1;
        } else {
            this.explore_count++;
        }

        if (this.direction == 8) {
            this.direction = 1;
        }

        return d;
    }

    private int fuelPumpDistance() {
        int away_puel_x = this.current_point.getX();
        int away_puel_y = this.current_point.getY();
        int distance = away_puel_x > away_puel_y ? away_puel_x : away_puel_y;
        return distance;
    }
}
