package SmartTanker;

import uk.ac.nott.cs.g54dia.library.Cell;
import uk.ac.nott.cs.g54dia.library.EmptyCell;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JasonChen on 2/15/15.
 */
public class MemMap {
    Map<MemPoint, Cell> map = new HashMap<MemPoint, Cell>();

    public void remember(MemPoint p, Cell c) {
        if (this.map.get(p) != null) {
            this.map.put(p, c);
        }
    }

    public Cell getCell(MemPoint p) {
        Cell c = this.map.get(p);
        return c;
    }

}
