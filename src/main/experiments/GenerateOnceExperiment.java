package main.experiments;

import io.DataGenerator;
import io.JSONFileParser;
import main.ExecutionFlow;
import main.experiments.parameters.EVsParameters;
import main.experiments.parameters.StationsParameters;
import main.experiments.parameters.SystemParameters;

import java.util.Scanner;

/**
 * Generates data only one time and then runs the execution *iterations* times
 * The user must set the data parameters
 */
public class GenerateOnceExperiment extends Experiment {

    private StationsParameters stationsParameters;
    private EVsParameters eVsParameters;
    private SystemParameters systemParameters;

    private boolean setStations, setEvs, setSystem;


    public GenerateOnceExperiment(String id, int iterations, String stationsPath, String evsPath, String systemPath, String statisticsPath) {
        super(id, iterations, stationsPath, evsPath, systemPath, statisticsPath);
        setStations = false;
        setEvs = false;
        setSystem = false;
    }

    public void setStationsValues (int stationsNumber, int maxChargers) {
        stationsParameters = new StationsParameters(stationsNumber, maxChargers);
        setStations = true;
    }

    public void setEVsValues (int evsNumber, int minEnergy, int maxEnergy, double sEnergy, double windowLength) {
        eVsParameters = new EVsParameters(evsNumber, minEnergy, maxEnergy, sEnergy, windowLength);
        setEvs = true;
    }

    public void setSystemParameters(int slotsNumber, int gridSize) {
        systemParameters = new SystemParameters(slotsNumber, gridSize);
        setSystem = true;
    }

    @Override
    public void run(String mode) {

        generateData();

        for (int i = 0; i < getIterations(); i++) {
            ExecutionFlow exe = new ExecutionFlow(getStationsPath(), getEvsPath(), getSystemPath());
            selectMode(mode, exe);
        }
    }

    protected void generateData () {
        DataGenerator generator = new DataGenerator();
        JSONFileParser parser = new JSONFileParser();

        if (setStations)
            generator.generateRandomStations(stationsParameters.getStationsNumber(), stationsParameters.getMaxChargers());
        else {
            System.out.println("Using already saved stations' file!");
            // This is very bad design - fix it in the future!
            generator.readStationFile(getStationsPath());
        }

        if (setEvs) {
            // if the user didn't specify the system parameters, reader the already saved ones
            // to use in the generation of the evs
            if (!setSystem)
                systemParameters = parser.readSystemParameters(getSystemPath());
            generator.generateEVsFile(eVsParameters.getEvsNumber(), eVsParameters.getMinEnergy(), eVsParameters.getMaxEnergy(), eVsParameters.getsEnergy(),
                    eVsParameters.getWindowLength(), systemParameters.getSlotsNumber(), systemParameters.getGridSize());
        }
        else
            System.out.println("Using already saved evs' file!");

        if (setSystem)
            generator.generateSystemParameters(systemParameters.getSlotsNumber(), systemParameters.getGridSize());
        else
            System.out.println("Using already saved system's file!");
    }
}
