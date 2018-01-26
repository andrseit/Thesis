package optimize;

import ilog.concert.IloException;
import station.EVObject;

import java.util.ArrayList;

/**
 * Created by Thesis on 26/1/2018.
 */
public class ProfitCPLEX extends AbstractCPLEX {
    @Override
    protected void addObjectiveFunction(ArrayList<EVObject> evs, int[] price, int min_slot) {
        System.out.println("Profit CPLEX");
        try {
            for (int ev = 0; ev < evs.size(); ev++) {
                EVObject current = evs.get(ev);
                int start = current.getStartSlot();
                int end = current.getEndSlot();

                for (int s = 0; s < slots_number; s++) {
                    if (s >= start - min_slot && s <= end - min_slot) {
                        objective.addTerm(price[s], var[ev][s]);
                    }
                }
            }
            cp.addMaximize(objective);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
