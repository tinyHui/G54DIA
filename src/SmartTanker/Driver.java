package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
    MemPoint current_point = new MemPoint(0, 0);

    public int getDirection(MemPoint target) {
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

        switch (direction) {
            case MoveAction.NORTHEAST:
                this.current_point.x++;
                this.current_point.y++;
                break;
            case MoveAction.SOUTHEAST:
                this.current_point.x++;
                this.current_point.y--;
                break;
            case MoveAction.EAST:
                this.current_point.x++;
                break;
            case MoveAction.NORTHWEST:
                this.current_point.x--;
                this.current_point.y++;
                break;
            case MoveAction.SOUTHWEST:
                this.current_point.x--;
                this.current_point.y--;
                break;
            case MoveAction.WEST:
                this.current_point.x--;
                break;
            case MoveAction.NORTH:
                this.current_point.y++;
                break;
            case MoveAction.SOUTH:
                this.current_point.y--;
                break;
            default:
                throw new ValueException("Already there");
        }

        return new MoveAction(direction);
    }

    public MemPoint getCurrentPoint() {
        return current_point;
    }
}
