package optimize;

import ilog.concert.IloException;
import station.EVObject;

import java.util.ArrayList;

/**
 * Created by Thesis on 26/1/2018.
 */
public class ServiceCPLEX extends AbstractCPLEX {
    @Override
    protected void addObjectiveFunction(ArrayList<EVObject> evs, int[] price, int min_slot) {
        System.out.println("Service CPLEX");
        try {
            for (int ev = 0; ev < evs.size(); ev++) {
                objective.addTerm(1.0, charges[ev]);
            }
            cp.addMaximize(objective);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
