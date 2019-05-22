package main.experiments;

import main.ExecutionFlow;

/**
 * Rus experiments for already saved data
 * No need to generate new
 */
public class SavedDataExperiment extends Experiment {

    public SavedDataExperiment(String id, int iterations, String stationsPath, String evsPath, String systemPath, String statisticsPath) {
        super(id, iterations, stationsPath, evsPath, systemPath, statisticsPath);
    }

    @Override
    public void run(String mode) {
        ExecutionFlow exe = new ExecutionFlow(getStationsPath(), getEvsPath(), getSystemPath());
        exe.useDelays(isUseDelays());
        for (int i = 0; i < getIterations(); i++) {
            selectMode(mode, exe);
        }
    }
}
