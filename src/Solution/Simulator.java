package Solution;

import uk.ac.nott.cs.g54dia.library.*;

/**
 * Created by JasonChen on 2/12/15.
 */
public class Simulator {
    /**
     * Time for which execution pauses so that GUI can update.
     * Reducing this value causes the simulation to run faster.
     */
//    private static int DELAY = 100;
    private static int DELAY = 50;

    /**
     * Number of timesteps to execute
     */
    private static int DURATION = 10 * 10000;

    public static void main(String[] args) {
        // Create an environment
        Environment env = new Environment((Tanker.MAX_FUEL/2)-5);
        // Create our tanker
        SmartTanker t = new SmartTanker();
        // Create a GUI window to show our tanker
        TankerViewer tv = new TankerViewer(t);
        tv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        // Start executing the Tanker
        while (env.getTimestep()<(DURATION)) {
            // Advance the environment timestep
            env.tick();
            // Update the GUI
            tv.tick(env);
            // Get the current view of the tanker
            Cell[][] view = env.getView(t.getPosition(), Tanker.VIEW_RANGE);
            // Let the tanker choose an action
            Action a = t.senseAndAct(view, env.getTimestep());
            // Try to execute the action
            try {
                a.execute(env, t);
            } catch (OutOfFuelException dte) {
                System.out.println("Tanker out of fuel!");
                break;
            } catch (ActionFailedException afe) {
                System.err.println("Failed: " + afe.getMessage());
            }
            try { Thread.sleep(DELAY);} catch (Exception e) { }
        }
    }
}
