package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

/**
 * Created by JasonChen on 2/12/15.
 */
public class SmartTanker extends Tanker {
    Boolean LOW_FUEL = false;
    int DIRECTION = 1;

    int explore_count = 1;
    Task t = null;
    Cell current_cell = null;
    Point current_point = null;
    Point nearest_station = null;
    Point nearest_well = null;
    Point history_point = null;

    public SmartTanker() {}

    @Override
    public Action senseAndAct(Cell[][] view, long timestep) {
        this.current_cell = this.getCurrentCell(view);
        this.current_point = this.current_cell.getPoint();
        scanView(view);
        fuelCheck();

        // not enough fuel to go back and refill
        if (this.LOW_FUEL &&
                !(this.current_cell instanceof FuelPump)) {
            this.history_point = this.current_point;
            return new MoveTowardsAction(FUEL_PUMP_LOCATION);
        }

        // at fuel pump
        if (this.current_cell instanceof FuelPump) {
            // no gas, refuel
            if (this.getFuelLevel() < MAX_FUEL - 1) {
                this.LOW_FUEL = false;
                return new RefuelAction();
            }
            if (this.history_point != null) {
                // if got history point, go back
                Point h = this.history_point;
                this.history_point = null;
                return new MoveTowardsAction(h);
            }
        } else
        // if at water well, refill water
        if (this.current_cell instanceof Well &&
                this.getWaterLevel() != MAX_WATER) {
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
                    this.getWaterLevel() != MAX_WATER) {
                // find nearest well and water level not at maximum
                return new MoveTowardsAction(nearest_well);
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
            if (this.getWaterLevel() > this.t.getWaterDemand()) {
                if (this.current_point.equals(this.t.getStationPosition())) {
                    // at task cell, finish it
                    return new DeliverWaterAction(this.t);
                } else {
                    // not at task cell, go there
                    return new MoveTowardsAction(this.t.getStationPosition());
                }
            } else {
                // give all water then refill water
                if (this.getWaterLevel() == 0) {
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
        int direction = -1;
        switch (this.DIRECTION) {
            case 1:
            case 2:
            case 15:
            case 16:
                direction = MoveAction.NORTHEAST;
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 11:
            case 12:
            case 13:
            case 14:
                direction = MoveAction.SOUTH;
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                direction = MoveAction.NORTHWEST;
                break;
            default:
                throw new ValueException("Unrecognised Direction");
        }

        if (this.explore_count == (int)(VIEW_RANGE * Math.sqrt(2))) {
            this.explore_count = 1;
            if (this.DIRECTION == 16) {
                this.DIRECTION = 1;
            } else {
                this.DIRECTION++;
            }
        } else {
            this.explore_count++;
        }
        return direction;
    }

    private void fuelCheck() {
        int away_puel_x = this.current_point.getX();
        int away_puel_y = this.current_point.getY();
        int distance = away_puel_x > away_puel_y ? away_puel_x : away_puel_y;
        // remain fuel can only go back fuel pump directly
        if (this.getFuelLevel() - 10 < distance) {
            this.LOW_FUEL = true;
        }
    }
}
