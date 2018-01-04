package optimize;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * Created by Thesis on 3/1/2018.
 * A mini cplex that is used in negotiations to assign the suggestion slots
 * to the most profitables one
 * e.g. if less energy is 2 (from 4) then it should choose the 2 slots
 * with the minimum cost, so that there is maximum profit
 */
public class ProfitCPLEX {

    private IloCplex cp;
    private IloNumVar[] var;
    private IloLinearNumExpr objective;
    private int[] map;

    public ProfitCPLEX () {
        try {
            cp = new IloCplex();
            cp.setOut(null);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public int modelLessEnergy (int[] price, int start, int end, int energy, int bid) {
        try {
            int slots = end - start + 1;
            map = new int[slots];
            System.out.println("Start: " + start + " End: " + end + " Slots: " + slots + " price size: " + price.length);
            var = new IloNumVar[slots];
            objective = cp.linearNumExpr();
            IloLinearNumExpr energy_constraint = cp.linearNumExpr();
            for (int s = 0; s < slots; s++) {
                var[s] = cp.boolVar("var(" + s + ")");
                energy_constraint.addTerm(1, var[s]);
                if (bid - price[s] == 0) {
                    objective.addTerm(0.001, var[s]);
                } else {
                    objective.addTerm(bid, var[s]);
                    objective.addTerm(-price[s], var[s]);
                }
            }
            cp.addEq(energy, energy_constraint);
            cp.addMaximize(objective);

            if (cp.solve()) {
                return (int) cp.getValue(objective);
            } else {
                System.out.println("Problem could not be solved!");
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int modelWiderWindow (int[] chargers, int[] price, int start, int end, int energy, int bid) {
        try {
            int slots = end - start + 1;
            map = new int[slots];
            //System.out.println("Start: " + start + " End: " + end + " Slots: " + slots + " price size: " + price.length);
            var = new IloNumVar[slots];
            objective = cp.linearNumExpr();
            IloLinearNumExpr energy_constraint = cp.linearNumExpr();
            for (int s = 0; s < slots; s++) {
                var[s] = cp.boolVar("var(" + s + ")");
                if (chargers[s] == 0) {
                    cp.addEq(0.0, var[s]);
                }
                energy_constraint.addTerm(1, var[s]);
                if (bid - price[s] == 0) {
                    objective.addTerm(0.001, var[s]);
                } else {
                    objective.addTerm(bid, var[s]);
                    objective.addTerm(-price[s], var[s]);
                }
            }
            cp.addEq(energy, energy_constraint);
            cp.addMaximize(objective);

            if (cp.solve()) {
                return (int) cp.getValue(objective);
            } else {
                System.out.println("Problem could not be solved!");
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Returns the slots that the ev is going to be placed
     * @return
     */
    public int[] getMiniMap () {

            try {
                for (int s = 0; s < map.length; s++) {
                    if (cp.getValue(var[s]) == 1.0)
                        map[s] = 1;
                }

            } catch (IloException e) {
                e.printStackTrace();
            }
       return map;
    }
}
