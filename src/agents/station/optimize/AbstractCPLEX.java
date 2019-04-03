package agents.station.optimize;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import agents.station.EVObject;

import java.util.ArrayList;

public abstract class AbstractCPLEX {


    protected IloCplex cp;
    protected IloNumVar[][] var;
    protected IloNumVar[] charges;
    protected IloLinearNumExpr objective;

    private int evs_number;
    protected int slots_number;
    private int[][] schedule;
    private int[] who_charges;

    protected abstract void addObjectiveFunction (ArrayList<EVObject> evs, int[] price, int min_slot);

    public AbstractCPLEX() {
        try {
            cp = new IloCplex();
            cp.setParam(IloCplex.DoubleParam.TiLim, 2000);
            cp.setParam(IloCplex.DoubleParam.EpGap, 0.09);
            cp.setOut(null);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param list_size
     * @param slots_number = max_slot - min_slot + 1
     */
    private void initializeVariables(int list_size, int slots_number) {
        //this.chargers = chargers.clone();
        //System.out.println("EVs: " + list_size + ", slots: " + slots_number);
        evs_number = list_size;
        this.slots_number = slots_number;
        var = new IloNumVar[evs_number][slots_number];
        charges = new IloNumVar[evs_number];

        try {
            objective = cp.linearNumExpr();

            for (int e = 0; e < evs_number; e++) {
                for (int s = 0; s < slots_number; s++) {
                    var[e][s] = cp.boolVar("var(" + e + ", " + s + ")");
                }
                charges[e] = cp.boolVar("c(" + e + ")");
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void lockPreviousBidders (ArrayList<EVObject> evs) {

        try {
            for (int e = 0; e < evs.size(); e++) {
                if (evs.get(e).isCharged()) {
                    cp.addEq(charges[e], 1);
                    //System.out.println(agents.evs.get(e).toString());
                }
            }
        } catch (IloException e1) {
            e1.printStackTrace();
        }
    }


    private void addChargersConstraint(int[] chargers, int min_slot) {

        try {
            for (int s = 0; s < slots_number; s++) {
                IloLinearNumExpr chargers_constraint = cp.linearNumExpr();
                for (int ev = 0; ev < evs_number; ev++) {
                    chargers_constraint.addTerm(1, var[ev][s]);
                }
                // s + min_slot e.g. min_slot = 5, current slot = 0, charger[current] = charger[5] -> current + min
                cp.addLe(chargers_constraint, chargers[s + min_slot]);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }


    private void addEnergyConstraints(ArrayList<EVObject> evs, int min_slot) {
        try {
            for (int ev = 0; ev < evs.size(); ev++) {

                EVObject current = evs.get(ev);
                boolean[] checked_slot = new boolean[slots_number];

                IloLinearNumExpr ev_energy_constraint = cp.linearNumExpr();

                int start = current.getStartSlot();
                int end = current.getEndSlot();
                for (int s = 0; s < slots_number; s++) {
                    if (s >= start - min_slot && s <= end - min_slot) {
                        ev_energy_constraint.addTerm(1, var[ev][s]);
                        checked_slot[s] = true;
                    }
                }

                for (int s = 0; s < slots_number; s++) {
                    if (!checked_slot[s])
                        cp.addEq(0, var[ev][s]);
                }

                cp.addEq(ev_energy_constraint, cp.prod(charges[ev], evs.get(ev).getEnergy()));
            }


        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void solveLinearProblem(ArrayList<EVObject> evs, int slots_number, int min_slot, int max_slot) {

        //System.out.println(min_slot + " -- " + max_slot);
        try {

            if (cp.solve()) {

                schedule = new int[evs_number][slots_number];

                for (int ev = 0; ev < evs.size(); ev++) {

                    for (int s = min_slot; s <= max_slot; s++) {
                        //System.out.print(cp.getValue(var[ev][s - min_slot]) + " ");
                        schedule[ev][s] = (int) cp.getValue(var[ev][s - min_slot]);
                    }
                    //System.out.println();
                }
                setWhoCharges();
                //System.out.println(objective);
                //System.out.println("Utility: " + cp.getValue(objective));
            } else {
                System.out.println("Problem could not be solved: ");
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param evs
     * @param slots_number
     * @param price
     * @param chargers
     * @param min_slot     is the minimum slot that the agents.evs requested for energy, the map computed here will start from there
     * @param max_slot     and end here
     */
    public void model(ArrayList<EVObject> evs, int slots_number, int[] price, int[] chargers, int min_slot, int max_slot) {
        this.initializeVariables(evs.size(), max_slot - min_slot + 1);
        this.addEnergyConstraints(evs, min_slot);
        this.addChargersConstraint(chargers, min_slot);
        this.lockPreviousBidders(evs);
        this.addObjectiveFunction(evs, price, min_slot);
        this.solveLinearProblem(evs, slots_number, min_slot, max_slot);
        this.clearModel();
    }


    private void clearModel() {
        try {
            cp.clearModel();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }


    public int[][] getScheduleMap() {
        return schedule;
    }

    private void setWhoCharges() {
        who_charges = new int[evs_number];

        for (int ev = 0; ev < evs_number; ev++) {
            try {
                if (cp.getValue(charges[ev]) > 0)
                    who_charges[ev] = 1;
                else
                    who_charges[ev] = 0;
            } catch (IloException e) {
                e.printStackTrace();
            }

        }
    }

    public int[] getWhoCharges() {

        return who_charges;
    }

}
