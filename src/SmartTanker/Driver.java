package SmartTanker;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import uk.ac.nott.cs.g54dia.library.*;

/**
 * Created by JasonChen on 2/15/15.
 */
public class Driver {
    MemPoint current_point = new MemPoint(0, 0);

    public Action driveTo(MemPoint target) {
        int dx = target.x - current_point.x;
        int dy = target.y - current_point.y;

        if (dx > 0 && dy > 0) {
            this.current_point.x++;
            this.current_point.y++;
            return new MoveAction(MoveAction.NORTHEAST);
        } else if (dx > 0 && dy < 0) {
            this.current_point.x++;
            this.current_point.y--;
            return new MoveAction(MoveAction.SOUTHEAST);
        } else if (dx > 0 && dy == 0) {
            this.current_point.x++;
            return new MoveAction(MoveAction.EAST);
        } else if (dx < 0 && dy > 0) {
            this.current_point.x--;
            this.current_point.y++;
            return new MoveAction(MoveAction.NORTHWEST);
        } else if (dx < 0 && dy < 0) {
            this.current_point.x--;
            this.current_point.y--;
            return new MoveAction(MoveAction.SOUTHWEST);
        } else if (dx < 0 && dy == 0) {
            this.current_point.x--;
            return new MoveAction(MoveAction.WEST);
        } else if (dx == 0 && dy > 0) {
            this.current_point.y++;
            return new MoveAction(MoveAction.NORTH);
        } else if (dx == 0 && dy < 0) {
            this.current_point.y--;
            return new MoveAction(MoveAction.SOUTH);
        } else {
            throw new ValueException("Already there");
        }
    }

    public MemPoint getCurrentPoint() {
        return current_point;
    }
}
