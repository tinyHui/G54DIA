package SmartTanker.Planner;

import SmartTanker.MemMap;
import SmartTanker.MemPoint;
import SmartTanker.Status;
import SmartTanker.TaskPair;
import uk.ac.nott.cs.g54dia.library.Tanker;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by JasonChen on 2/26/15.
 */
public class Solution {
    private final static Double MUTATE_RATE = 0.5;
    private final static TaskPair FUEL_PAIR = new TaskPair(MemPoint.FUEL_PUMP, null);

    private ArrayList<TaskPair> visit_list = new ArrayList<TaskPair>();
    private ArrayList<TaskPair> visit_list_plan = new ArrayList<TaskPair>();

    private Random rand = new Random(System.currentTimeMillis());
    private MemMap map;
    private int water_level = -1;
    private int completed_count = 0;
    private int delivered_water = 0;
    private int fuel_level;
    private long time_spend = 0;
    private long score = 0;

    public Solution(ArrayList<TaskPair> visit_list, MemMap map, Status status) {
        this.visit_list = (ArrayList<TaskPair>) visit_list.clone();
        this.water_level = status.water_level;
        this.completed_count = status.completed_count;
        this.delivered_water = status.delivered_water;
        this.fuel_level = status.fuel_level;
        this.map = map;
    }

    public void initialise(MemPoint current_point) {
        ArrayList<TaskPair> visit_list_copy = (ArrayList<TaskPair>) this.visit_list.clone();
        int size = visit_list_copy.size();
        int current_size = size;
        this.visit_list.clear();

        TaskPair current_pair = null;
        for (int i = 0; i < size; i++) {
            if (rand.nextDouble() > MUTATE_RATE) {
                // not mutate, find the nearest one
                int min_distance = 10000000;
                int min_index = 0;
                int index = 0;
                for (TaskPair tp : visit_list_copy) {
                    int distance = current_point.calcDistance(tp.p);
                    if (distance < min_distance) {
                        current_pair = tp;
                        min_distance = distance;
                        min_index = index;
                    }
                    index++;
                }
                visit_list_copy.remove(min_index);
                current_size--;
            } else {
                int index = this.rand.nextInt(current_size);
                current_pair = visit_list_copy.get(index);
                visit_list_copy.remove(index);
                current_size--;
            }
            this.visit_list.add(current_pair);
            current_point = (MemPoint) current_pair.p.clone();
        }


    }

    private MemPoint checkFuel(MemPoint current_point, MemPoint target_point) {
        int distance_c_t = current_point.calcDistance(target_point);
        int distance_f_t = MemPoint.FUEL_PUMP.calcDistance(target_point);
        int cost = distance_c_t + target_point.calcDistanceToFuel();

        if (cost > this.fuel_level) {
            // not enough fuel to go and back, go back fuel pump first
            int size = this.visit_list_plan.size();
            if (size == 0) {
                // none of the point been added, but not enough fuel
                this.visit_list_plan.add(FUEL_PAIR);
            } else {
                this.visit_list_plan.add(size - 1, FUEL_PAIR);
            }
            // fuel level max
            this.fuel_level = Tanker.MAX_FUEL - distance_f_t;
            // update time spend
            this.time_spend += cost + 1;
        } else {
            this.fuel_level -= distance_c_t;
            // update time spend
            this.time_spend += distance_c_t;
        }
        this.time_spend++;

        return (MemPoint) target_point.clone();
    }

    public void generate(MemPoint current_point) {
        for (TaskPair tp : this.visit_list) {
            MemPoint target_point;

            if (tp.t.getRequired() > this.water_level) {
                // not enough water
                MemPoint well = this.map.getMidWell(current_point, tp.p);
                if (well == null) {
                    // not enough explore
                    this.visit_list_plan.clear();
                    break;
                } else {
                    well = (MemPoint) well.clone();
                }
                // go well
                this.visit_list_plan.add(new TaskPair(well, null));
                // target p is well
                target_point = well;
                // check fuel and update current p
                current_point = checkFuel(current_point, target_point);
                // water level max
                this.water_level = Tanker.MAX_WATER;
            }

            // finish task
            this.visit_list_plan.add(new TaskPair(tp.p, tp.t));
            // update water level
            this.water_level -= tp.t.getRequired();
            // target p is station
            target_point = (MemPoint) tp.p.clone();
            // check fuel
            current_point = checkFuel(current_point, target_point);
            // update score
            this.completed_count++;
            this.delivered_water += tp.t.getRequired();
            this.score = (long)this.completed_count * (long)this.delivered_water;
        }
    }

    public long getScore() {
        return this.score - this.time_spend;
    }

    public ArrayList<TaskPair> getVisitList() {
        return this.visit_list_plan;
    }
}
