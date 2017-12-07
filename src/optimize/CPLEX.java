package optimize;

import evs.EV;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Darling on 28/7/2017.
 * objective -  fortise osa pio polla mporeis
 *              fortise autous me tis megalyteres prosfores
 *
 * constraints - (1) kathe oxima fortizei oso prepei - isws mporei na xalarwsei auto
 *               (2) na min ksepernietai to orio twn fortistwn
 *               (3) na min spataleitai perissoteri energeia apo oso yparxei
 */
public class CPLEX {


    private IloCplex cp;
    private IloNumVar[][] var;
    private IloNumVar[] charges;
    private IloNumVar[][] ev_bid; // which bid the station chooses
    private IloLinearNumExpr objective;

    private int evs_number;
    private int slots_number;
    private int[][] schedule;
    private int[][] charges_int;
    private int[] who_charges;

    // value of the objective function
    private int utility;

    public CPLEX () {
        try {
            cp = new IloCplex();
            cp.setOut(null);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param list_size
     * @param slots_number = max_slot - min_slot + 1
     */
    private void initializeVariables (int list_size, int slots_number) {
        //this.chargers = chargers.clone();
        System.out.println("EVs: " + list_size + ", slots: " + slots_number);
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

    private void lockPreviousBidders (int[][] previous_schedule, int previous_bidders_number) {

        try {
            for (int ev = 0; ev < previous_bidders_number; ev++) {
                for (int slot = 0; slot < previous_schedule[0].length; slot++) {
                    if (previous_schedule[ev][slot] == 1) {
                        cp.addEq(var[ev][slot], 1);
                    }
                }
                cp.addEq(charges[ev], 1);
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

    }

    private void addChargersConstraint (int[] chargers, int min_slot) {

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

    private void addObjectiveAndEnergyConstraints (ArrayList<EV> evs, int[] price, int min_slot) {
        try {


            for (int ev = 0; ev < evs.size(); ev++) {

                EV current = evs.get(ev);
                int ev_position = ev;
                boolean[] checked_slot = new boolean[slots_number];

                IloLinearNumExpr ev_energy_constraint = cp.linearNumExpr();
                IloLinearNumExpr zero = cp.linearNumExpr();


                //for (int b = 0; b < current.getBidsNumber(); b++) {

                    //int start = current.getStartSlot(b); // get start slot
                    //int end = current.getEndSlot(b); // get end slot
                    //int bid = current.getBid(b); // get bid for these slots

                    int start = current.getStartSlot();
                    int end = current.getEndSlot();
                    int bid = current.getBid();
                    for (int s = 0; s < slots_number; s++) {
                        if (s >= start - min_slot && s <= end - min_slot) {

                            ev_energy_constraint.addTerm(1, var[ev_position][s]);

                            if (bid - price[s + min_slot] == 0) {
                                objective.addTerm(0.5, var[ev_position][s]);
                            } else {
                                objective.addTerm(bid, var[ev_position][s]);
                                objective.addTerm(-price[s + min_slot], var[ev_position][s]);
                            }

                            checked_slot[s] = true;
                        }
                    }
                //}

                for (int s = 0; s < slots_number; s++) {
                    if (!checked_slot[s])
                        cp.addEq(0, var[ev_position][s]);
                }


                cp.addEq(ev_energy_constraint, cp.prod(charges[ev_position], evs.get(ev).getEnergy()));
            }

            //System.out.println(cp);
            cp.addMaximize(objective);

        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void solveLinearProblem (ArrayList<EV> evs, int slots_number, int min_slot, int max_slot) {

        System.out.println(min_slot + " -- " + max_slot);
        try {

            if (cp.solve()) {

                schedule = new int[evs_number][slots_number];
                charges_int = new int[evs_number][2];


                for (int ev = 0; ev < evs.size(); ev++) {

                    int ev_position = ev;

                    if ((int) cp.getValue(charges[ev_position]) == 1)
                        evs.get(ev).setCharged(true);
                    else
                        evs.get(ev).setCharged(false);

                    for (int s = min_slot; s <= max_slot; s++) {
                        //System.out.print(cp.getValue(var[ev][s - min_slot]) + " ");
                        schedule[ev_position][s] = (int) cp.getValue(var[ev_position][s - min_slot]);
                    }
                    //System.out.println();

                    utility = (int) cp.getValue(objective);

                }
                setWhoCharges();
                System.out.println(objective);
                System.out.println("Utility: " + cp.getValue(objective));
            } else {
                System.out.println("Problem could not be solved!");
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param evs
     * @param slots_number
     * @param price
     * @param chargers
     * @param min_slot is the minimum slot that the evs requested for energy, the map computed here will start from there
     * @param max_slot and end here
     */
    public void model (ArrayList<EV> evs, int slots_number, int[] price, int[] chargers, int min_slot, int max_slot) {
        this.initializeVariables(evs.size(), max_slot - min_slot + 1);
        //this.lockPreviousBidders(previous_schedule, previous_bidders_number);
        this.addObjectiveAndEnergyConstraints(evs, price, min_slot);
        this.addChargersConstraint(chargers, min_slot);
        this.solveLinearProblem(evs, slots_number, min_slot, max_slot);
        this.clearModel();
    }



    public void buildModel (ArrayList<EV> evs, int chargers_number, int slots_number, int[] price, int[][] previous_schedule, int previous_bidders_number) {


        evs_number = evs.size() + previous_bidders_number; // the number of all the evs in the schedule
        this.slots_number = slots_number; // the number of the slots
        var = new IloNumVar[evs_number][slots_number]; // the variable that stores if an ev charges in a slot
        charges = new IloNumVar[evs_number]; // defines if an ev will be charged
        ev_bid = new IloNumVar[evs_number][]; // defines which bid of an ev is going to be use. e.g. ev made 3 bids, but bid 2 will be selected, so ev_bid[ev][1] = 1

        try {

            // initialize variables
            for (int i = 0; i < evs_number; i++) {
                for (int j = 0; j < slots_number; j++) {
                    var[i][j] = cp.boolVar("var(" + i + ", " + j + ")");
                }
                charges[i] = cp.boolVar("c(" + i + ")");
            }



            IloLinearNumExpr obj = cp.linearNumExpr(); // objective function



            // CONSTRAINTS

            // for every new vehicle
            for (int ev = 0; ev < evs.size(); ev++) {

                int ev_position = ev + previous_bidders_number; // the position of the new evs in the schedule, as the first "previous_bidders_number" lines are occupied by the older evs

                boolean[] checked_slot = new boolean[slots_number]; // if a slot is checked in some bid, then don't add it to zero



                IloLinearNumExpr p = cp.linearNumExpr();
                IloLinearNumExpr zero = cp.linearNumExpr(); // if an ev has not bidden for a slot, then var[ev][s] should be zero - this helps to make sure that this happens


                EV current = evs.get(ev);

                ev_bid[ev_position] = new IloNumVar[current.getSlots().size()]; // set an array with a size of bids number


                // for each bid of the ev, initialize its decision variable
                for (int i = 0; i < ev_bid[ev_position].length; i++) {
                    ev_bid[ev_position][i] = cp.boolVar();
                }

                // for every bid of the vehicle
                for (int i = 0; i < current.getBidsNumber(); i++) {
                    IloLinearNumExpr inner = cp.linearNumExpr();

                    int start = current.getStartSlot(i); // get start slot
                    int end = current.getEndSlot(i); // get end slot
                    int bid = current.getBid(i); // get bid for these slots

                    // for each slot in the bid
                    for (int s = 0; s < slots_number; s++) {
                        //IloLinearNumExpr price_constraint = cp.linearNumExpr();


                        if (s >= start && s <= end) {
                            int v = price[s] * bid; // the final cost for the slot for the specific ev
                            inner.addTerm(1, var[ev_position][s]);
                            p.addTerm(1, var[ev_position][s]);
                            //obj.addTerm(v, var[ev][s]);

                            int temp_price = price[s];
                            if (bid - price[s] == 0) {
                                obj.addTerm(0.5, var[ev_position][s]);
                            } else {
                                obj.addTerm(bid, var[ev_position][s]);
                                obj.addTerm(-temp_price, var[ev_position][s]);
                            }

                            zero.remove(var[ev_position][s]);

                            //price_constraint.addTerm(1, var[ev_position][s]);


                            //cp.addLe(cp.prod(price[s], var[ev_position][s]), bid); // if the slot is selected then the bid of the ev has to be higher than the price

                            // do not add at zero
                            checked_slot[s] = true;
                        }

                        // check every slot and add the slots that the ev did not bid to zero

                        for (int c = 0; c < slots_number; c ++) {
                            if (!checked_slot[c])
                                zero.addTerm(1, var[ev_position][c]);
                        }

                    }

                    cp.addEq(inner, cp.prod(ev_bid[ev_position][i], current.getEnergy()));
                }

//                for (int t = evs.get(ev).getStartSlot(); t < evs.get(ev).getEndSlot() + 1; t++) {
//                    p.addTerm(1, var[ev][t]);
//                }


                cp.addEq(p, cp.prod(charges[ev_position], evs.get(ev).getEnergy()));
                cp.addEq(zero, 0);
            }

            for (int t = 0; t < slots_number; t++) {
                IloLinearNumExpr p = cp.linearNumExpr();
                for (int ev = 0; ev < evs_number; ev++) {
                    p.addTerm(1, var[ev][t]);
                }
                cp.addLe(p, chargers_number);
            }


            // lock the positions of the previous bidders
            for (int ev = 0; ev < previous_bidders_number; ev++) {
                int temp = 0;
                for (int slot = 0; slot < slots_number; slot++) {
                    if (previous_schedule[ev][slot] == 1) {
                        temp++;

                        cp.addEq(var[ev][slot], 1);
                    }
                }
                if (temp > 0)
                    cp.addEq(charges[ev], 1);
            }
            // constraint gia energeia, alla den tin exw valei akoma



//            for (int ev = 0; ev < evs.size(); ev ++) {
//
//                int start_slot = evs.get(ev).getStartSlot();
//                int end_slot = evs.get(ev).getEndSlot() + 1;
//
//                for (int s = start_slot; s < end_slot; s++) {
//                    obj.addTerm(evs.get(ev).getBid(), var[ev][s]);
//                }
//            }
            cp.addMaximize(obj);
            //System.out.println(obj);

            if(cp.solve())
            {
                schedule = new int[evs_number][slots_number];
                charges_int = new int[evs_number][2];



                // setting charging state for the new evs
                for (int ev = 0; ev < evs.size(); ev++) {

                    charges_int[ev][0] = evs.get(ev).getId();
                    charges_int[ev][1] = (int) cp.getValue(charges[ev]);
                    if ((int) cp.getValue(charges[ev]) == 1)
                        evs.get(ev).setCharged(true);
                    else
                        evs.get(ev).setCharged(false);
                }

                for (int ev = 0; ev < evs_number; ev++) {
                    for (int s = 0; s < slots_number; s++) {
                        schedule[ev][s] = (int) cp.getValue(var[ev][s]);
                        //System.out.print(cp.getValue(var[ev][s]) + " ");
                    }


                    //System.out.println();
                }

                System.out.println("Schedule computed");
                this.clearModel();
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

    }

    private void clearModel()
    {
        try {
            cp.clearModel();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public int[][] getCharges () { return charges_int; }

    public int[][] getScheduleMap () { return schedule; }

    public int getUtility () { return utility; }

    private void setWhoCharges () {
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
    public int[] getWhoCharges () {

        return who_charges;
    }
 }
