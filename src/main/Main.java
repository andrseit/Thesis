package main;

import agents.evs.Preferences;
import main.experiments.GenerateOnceExperiment;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {
        GenerateOnceExperiment experiment = new GenerateOnceExperiment("online_no_suggestions", 1, "station.json", "evs.json",
                "system.json", "files/experiments/statistics");
        experiment.useDelays(false);
        //experiment.setStationsValues(2, 1);
        experiment.setEVsValues(5, 3, 5, 1.8, 1.8);
        //experiment.setSystemParameters(10, 2);
        experiment.run("online");
        //System.out.println(getPreferencesDistance(new Preferences(7, 9, 3), new Preferences(3, 5, 3), 10));
    }

    public static double getPreferencesDistance (Preferences initial, Preferences accepted, int slotsNumber) {

        if (accepted == null)
            return 0.0;

        int start = initial.getStart();
        int end = initial.getEnd();
        int energy = initial.getEnergy();

        int fStart = accepted.getStart();
        int fEnd = accepted.getEnd();
        int fEnergy = accepted.getEnergy();

        if (fStart >= start && fEnd <= end && fEnergy == energy)
            return 100.0;

        int maxShift;
        if (start > slotsNumber - end - 1)
            maxShift = start;
        else
            maxShift = slotsNumber - end - 1;

        int maxWiden = slotsNumber - (end - start + 1);

        int maxEnergyLoss = energy - 1;

        int shift = (start > fStart) ? start - fStart : fStart - start;
        double shiftPer = ((double) shift/(double) maxShift)*100;

        int widen = (fEnd - fStart > end - start) ? (fEnd - fStart) - (end - start) : 0;
        double widenPer = ((double) widen/(double) maxWiden)*100;

        int energyLoss = energy - fEnergy;
        double energyPer = ((double) energyLoss/(double) maxEnergyLoss)*100;

        double total = 100 - (0.5*(0.5*widenPer + 0.5*shiftPer) + 0.5*energyPer);

        return total;
    }
}
