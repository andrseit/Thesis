package main.experiments;

import agents.station.statistics.StationStatistics;
import io.StatisticsWriter;
import main.ExecutionFlow;

import java.io.File;
import java.util.ArrayList;

public abstract class Experiment {

    private String id;
    private int iterations;
    private int currentIteration;

    // paths of the files containing the data
    // this are used in the execution flow
    private String stationsPath;
    private String evsPath;
    private String systemPath;
    private String statisticsPath;

    private boolean useDelays; // shall the execution use delays and cancellations?

    public Experiment(String id, int iterations, String stationsPath, String evsPath, String systemPath, String statisticsPath) {
        this.id = id;
        this.iterations = iterations;
        this.stationsPath = stationsPath;
        this.evsPath = evsPath;
        this.systemPath = systemPath;
        this.statisticsPath = statisticsPath;
        currentIteration = 0;
    }

    public abstract void run (String mode);

    protected void selectMode (String mode, ExecutionFlow exe) {
        if (mode.equalsIgnoreCase("online"))
            exe.runOnline();
        else if (mode.equalsIgnoreCase("offline"))
            exe.runOffline();
        currentIteration++;
        exportStatistics(exe.getStationStatistics());
    }

    public void exportStatistics (ArrayList<StationStatistics> stationStatistics) {
        String experimentDirectory = statisticsPath + "/experiment_" + id;
        File directory = new File(experimentDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdir();
            if (!created)
                System.err.println("Failed to created experiment statistics directory!");
        }
        System.out.println(statisticsPath + "experiment_" + id + "." + currentIteration + ".csv");
        String csvLine = "";
        for (StationStatistics statistics: stationStatistics) {
            for (int i = 0; i < statistics.size(); i++)
                StatisticsWriter.addCSVLine(experimentDirectory + "/experiment_" + id + "." + currentIteration + ".csv", statistics.getSlotStatistics(i).toCSV());
        }
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public int getIterations() {
        return iterations;
    }

    public String getStationsPath() {
        return stationsPath;
    }

    public String getEvsPath() {
        return evsPath;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public void useDelays(boolean useDelays) {
        this.useDelays = useDelays;
    }

    public boolean isUseDelays() {
        return useDelays;
    }
}