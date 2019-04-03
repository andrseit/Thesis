package agents.station.optimize;

import ilog.concert.IloException;
import agents.station.EVObject;

import java.util.ArrayList;

public class ServiceCPLEX extends AbstractCPLEX {
    @Override
    protected void addObjectiveFunction(ArrayList<EVObject> evs, int[] price, int min_slot) {
        //System.out.println("Service CPLEX");
        try {

            for (int ev = 0; ev < evs.size(); ev++) {
                objective.addTerm(1.0, charges[ev]);
                EVObject current = evs.get(ev);
                int start = current.getStartSlot();
                int end = current.getEndSlot();

                for (int s = 0; s < slots_number; s++) {
                    if (s >= start - min_slot && s <= end - min_slot) {
                        objective.addTerm(0.01, var[ev][s]);
                    }
                }
            }

            cp.addMaximize(objective);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
