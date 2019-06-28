package agents.station.optimize;

import agents.evs.Preferences;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import agents.station.EVObject;
import various.ArrayTransformations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 12/3/2019.
 */
public class AlternativesCPLEX implements Optimizer {

    private IloCplex cp;
    private IloNumVar[][] chargesAtSlot;
    private IloLinearNumExpr objective;

    public AlternativesCPLEX () {
        try {
            cp = new IloCplex();
            cp.setParam(IloCplex.DoubleParam.TiLim, 2000);
            cp.setParam(IloCplex.DoubleParam.EpGap, 0.09);
            cp.setOut(null);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int[][] optimize(int slotsNumber, int currentSlot, ArrayList<EVObject> evs, int[] remainingChargers, int[] price) {
        int trimmedSlots = slotsNumber - currentSlot; // computeSuggestions the schedule for only the >= minSlot slots
        System.out.println("Slots number: " + trimmedSlots + ", min slot: " + currentSlot);
        initializeVariables(evs.size(), trimmedSlots);
        addChargersConstraint(remainingChargers, evs.size(), trimmedSlots, currentSlot);
        addEnergyConstraint(evs, trimmedSlots);
        addObjectiveFunction(evs, remainingChargers, trimmedSlots, currentSlot);
        return solveLinearProblem(evs.size(), slotsNumber, currentSlot);
    }

    public double calculateSlotMultiplier (ArrayList<EVObject> evs, int slotsNumber, int currentSlot, int[] chargers) {
        double damage = 0;
        for (EVObject ev: evs) {
            Preferences preferences = ev.getPreferences();
            int slotDamage = 0;
            for (int slot = preferences.getStart(); slot >= currentSlot; slot--) {
                damage += slotDamage;
                slotDamage++;
            }
            slotDamage = 0;
            for (int slot = preferences.getEnd(); slot < slotsNumber + currentSlot; slot++) {
                damage += slotDamage;
                slotDamage++;
            }
        }
        damage = 0;
        for (int s = 0; s < slotsNumber; s++) {
            damage += chargers[s]*s;
        }
        //System.out.println(damage);
        //System.out.println("Distance damage: " + 100.0/damage);
        return 100.0/damage;
    }

    public double calculateOccupancyMultiplier (int[] chargers, int slotsNumber, int currentSlot) {
        double damage = 0;
        for (int slot = 0; slot < slotsNumber; slot++) {
            damage += chargers[slot + currentSlot];
        }
        //System.out.println("Occupancy damage: " + 100.0/damage);
        return 100.0/damage;
    }

    private void initializeVariables (int evsNumber, int slotsNumber) {
        chargesAtSlot = new IloNumVar[evsNumber][slotsNumber];
        try {
            objective = cp.linearNumExpr();
            for (int e = 0; e < evsNumber; e++) {
                for (int s = 0; s < slotsNumber; s++) {
                    chargesAtSlot[e][s] = cp.boolVar("var(" + e + ", " + s + ")");
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addChargersConstraint(int[] chargers, int evsNumber, int slotsNumber, int currentSlot) {

        // till which slot the station can compute an alternative - so that it leaves space for future evs
        try {
            for (int s = 0; s < slotsNumber; s++) {
                IloLinearNumExpr chargers_constraint = cp.linearNumExpr();
                for (int ev = 0; ev < evsNumber; ev++) {
                    chargers_constraint.addTerm(1, chargesAtSlot[ev][s]);
                }
                // s + min_slot e.g. min_slot = 5, current slot = 0, charger[current] = charger[5] -> current + min
                cp.addLe(chargers_constraint, chargers[s + currentSlot]);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addEnergyConstraint (ArrayList<EVObject> evs, int slotsNumber) {
        //int maxSlot = 3 * slotsNumber / 4;
        //System.out.println("Slots Number: " + slotsNumber + ", Max slot: " + maxSlot);

        try {
            for (int ev = 0; ev < evs.size(); ev++) {
                EVObject current = evs.get(ev);
                int energy = current.getEnergy();
                IloLinearNumExpr energy_constraint = cp.linearNumExpr();
                for (int slot = 0; slot < slotsNumber; slot++) {
                    energy_constraint.addTerm(1, chargesAtSlot[ev][slot]);
                    //if (slot > 65)
                        //cp.addEq(chargesAtSlot[ev][slot], 0);
                }
                cp.addLe(energy_constraint, energy);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }


    }

    protected void addObjectiveFunction(ArrayList<EVObject> evs, int[] remainingChargers, int slotsNumber, int currentSlot) {
        //System.out.println("Profit CPLEX");
        double distance = calculateSlotMultiplier(evs, slotsNumber, currentSlot, remainingChargers);
        double occupancy = calculateOccupancyMultiplier(remainingChargers, slotsNumber, currentSlot);
        try {
            for (int ev = 0; ev < evs.size(); ev++) {
                EVObject current = evs.get(ev);
                int start = current.getStartSlot() - currentSlot;
                int end = current.getEndSlot() - currentSlot;

                for (int s = 0; s < slotsNumber; s++) {
                    objective.addTerm(10*occupancy, chargesAtSlot[ev][s]);
                    int penalty = 0;
                    if (s < start)
                        penalty += start - s;
                    else if (s > end)
                        penalty += s - end;
                    objective.addTerm(-(distance * (s)), chargesAtSlot[ev][s]);
                }
            }
            cp.addMaximize(objective);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private int[][] solveLinearProblem(int evsNumber, int slotsNumber, int currentSlot) {
        int[][] scheduleMap = new int[evsNumber][slotsNumber];
        //System.out.println(currentSlot + " + " + slotsNumber);
        try {
            if (cp.solve()) {
                for (int ev = 0; ev < evsNumber; ev++) {
                    for (int slot = 0; slot < slotsNumber; slot++) {
                        if (slot < currentSlot)
                            scheduleMap[ev][slot] = 0;
                        else {
                            if (cp.getValue(chargesAtSlot[ev][slot - currentSlot]) > 0)
                                scheduleMap[ev][slot] = 1;
                            else
                                scheduleMap[ev][slot] = 0;
                        }
                    }
                    //System.out.println();
                }
                //System.out.println(objective);
                //System.out.println("Utility: " + cp.getValue(objective));
            } else {
                System.out.println("Problem could not be solved: ");
            }
            cp.clearModel();
        } catch (IloException e) {
            e.printStackTrace();
        }
        return scheduleMap;
        //return ArrayTransformations.clearLines(scheduleMap, 0.2);
    }
}
