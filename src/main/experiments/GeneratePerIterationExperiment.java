package main.experiments;

import main.ExecutionFlow;

public class GeneratePerIterationExperiment extends GenerateOnceExperiment {

    public GeneratePerIterationExperiment(String id, int iterations, String stationsPath, String evsPath, String systemPath, String statisticsPath) {
        super(id, iterations, stationsPath, evsPath, systemPath, statisticsPath);
    }

    @Override
    public void run(String mode) {
        for (int i = 0; i < getIterations(); i++) {
            generateData();
            ExecutionFlow exe = new ExecutionFlow(getStationsPath(), getEvsPath(), getSystemPath());
            exe.useDelays(isUseDelays());
            selectMode(mode, exe);
        }
    }
}
