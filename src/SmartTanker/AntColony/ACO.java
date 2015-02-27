package SmartTanker.AntColony;

import SmartTanker.*;
import uk.ac.nott.cs.g54dia.library.Task;

import java.util.*;

/**
 * Created by JasonChen on 2/26/15.
 */
public class ACO {
    final static int ANT_MAX = 50;

    private MemMap map;
    private MemPoint current_point;
    private Status status;

    public ACO(MemMap map, MemPoint current_point, Status status) {
        this.map = map;
        this.current_point = current_point;
        this.status = status;
    }

    public void start(Queue<TaskPair> plan_list, HashMap<Task, MemPoint> tl) {
        // convert unfinished tasks from hash map to array list
        ArrayList<TaskPair> visit_list = new ArrayList<TaskPair>();
        ArrayList<TaskPair> best_visit_list = new ArrayList<TaskPair>();
        long best_score = 0;

        for (Map.Entry<Task, MemPoint> pairs : tl.entrySet()) {
            Task t = pairs.getKey();
            MemPoint p = pairs.getValue();
            visit_list.add(new TaskPair(p, t, 0));
        }

        // place ants
        for (int i = 0; i < ANT_MAX; i++) {
            // generate a new solution
            Solution s = new Solution(visit_list, this.map, this.status);
            s.generate(current_point);
            if (s.getScore() > best_score) {
                best_score = s.getScore();
                best_visit_list = s.getVisitList();
            }
        }

        if (best_visit_list.size() == 0) {
            System.out.println("Wrong");
        }
        System.out.println("Generate: " + best_visit_list.size());

        plan_list.clear();
        for (TaskPair t : best_visit_list) {
            plan_list.add(t);
        }

        System.out.println("\tAfter add: " + best_visit_list.size());
    }
}
