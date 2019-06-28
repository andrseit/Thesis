package io;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.*;

/**
 * Created by Thesis on 20/12/2017.
 */
public class DataGenerator {

    private int[][] stationLocation;
    private boolean stationsGenerated;

    public void generateEVsFile(int evsNumber, int minEnergy, int maxEnergy, double sEnergy, double windowLength, int slotsNumber, int gridSize) {
        if (!stationsGenerated)
            System.err.println("Please generate or read agents.station file first!");
        else {
            stationsGenerated = false;
            this.generateEVs(evsNumber,5, minEnergy, maxEnergy, sEnergy, windowLength,slotsNumber, gridSize);
        }
    }

    private double delay = 0.0; // how many evs will delay
    int delayed = 0;
    private void generateEVs(int evsNumber, int bidBound, int minEnergy, int maxEnergy, double sEnergy, double windowLength, int slotsNumber, int gridSize) {
        try {

            // we suppose station works 06.00 to 06.00, we also suppose that no customer will arrive after 12am
            // morning : 06.00 - 12.00 (slots : 0 - 72), afternoon : 12.00 - 18.00 (73 - 144), evening : 18.00 - 00.00 (145 - 216)
            double morning = 0.3, afternoon = 0.5, evening = 0.2;
            int generated = 0;


            FileWriter writer = new FileWriter("files/evs.json");
            JSONObject rootObject = new JSONObject();
            JSONObject evs = new JSONObject();



            for (int i = 0; i < evsNumber; i++) {
                // uncomment line below comment anything else into this for loop for total random time points
                evs.put("ev" + i, generateEV(i, 0, slotsNumber, bidBound, minEnergy, maxEnergy, sEnergy, windowLength, slotsNumber, gridSize, evsNumber));
            /*
                for (int i = 0; i < evsNumber*morning; i++) {
                    evs.put("ev" + generated, generateEV(generated, 0, (slotsNumber/4),
                            bidBound, minEnergy, maxEnergy, sEnergy, windowLength, slotsNumber, gridSize));
                    generated++;
                }
                for (int i = 0; i < evsNumber*afternoon; i++) {
                    evs.put("ev" + generated, generateEV(generated, (slotsNumber/4) + 1, (2*(slotsNumber/4)),
                            bidBound, minEnergy, maxEnergy, sEnergy, windowLength, slotsNumber, gridSize));
                    generated++;
                }
                for (int i = 0; i < evsNumber*evening; i++) {
                    if (!(generated >= evsNumber)) {
                        evs.put("ev" + generated, generateEV(generated, (2*(slotsNumber/4)), (3 * (slotsNumber / 4)),
                                bidBound, minEnergy, maxEnergy, sEnergy, windowLength, slotsNumber, gridSize));
                        generated++;
                    }
                }
                if (generated < evsNumber) {
                    for (int i = 0; i < evsNumber - generated; i++) {
                        evs.put("ev" + generated, generateEV(generated, (2 * (slotsNumber / 4)), (3 * (slotsNumber / 4)),
                                bidBound, minEnergy, maxEnergy, sEnergy, windowLength, slotsNumber, gridSize));
                        generated++;
                    }
                }
             */

            }
            rootObject.put("evs", evs);
            writer.write(rootObject.toJSONString() + "\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject generateEV (int id, int minStart, int maxStart, int bidBound, int minEnergy, int maxEnergy, double sEnergy, double windowLength, int slotsNumber, int gridSize, int evsNumber) {
        Random random = new Random();
        JSONObject ev = new JSONObject();
        JSONObject pref = new JSONObject();
        JSONObject strategy = new JSONObject();

        ev.put("id", id);
        // location
        int x = random.nextInt(gridSize);
        int y = random.nextInt(gridSize);
        ev.put("x", x);
        ev.put("y", y);
        int maxDistance = -1;
        int minDistance = Integer.MAX_VALUE; // distances from the closest and the farthest agents.station
        for (int[] aStationLocation : stationLocation) {
            int stationDistance = Math.abs(x - aStationLocation[0]) + Math.abs(y - aStationLocation[1]);
            if (stationDistance < minDistance)
                minDistance = stationDistance;
            if (stationDistance > maxDistance)
                maxDistance = stationDistance;
        }
        //System.out.println("min: " + minDistance + ", max: " + maxDistance);

        // final destination
        int f_x = random.nextInt(gridSize);
        int f_y = random.nextInt(gridSize);
        while ((f_x == x) && (f_y == y)) {
            f_x = random.nextInt(gridSize);
            f_y = random.nextInt(gridSize);
        }
        ev.put("f_x", 0);
        ev.put("f_y", 0);

        // preferences
        minStart += minDistance;
        int start = random.nextInt(maxStart - minStart) + minStart;
        int energy;
        if (slotsNumber - start <= minEnergy && slotsNumber - start <= maxEnergy)
            energy = slotsNumber - start;
        else
            energy = random.nextInt(Math.min(slotsNumber - start, maxEnergy) - minEnergy + 1) + minEnergy;
        //System.out.println("Energy: " + energy + " in [" + minStart + ", " + maxStart + "]" + ", distance: " + minDistance);
        int minEnd = start + energy - 1;
        int maxEnd = start + (int)(energy * windowLength) + 1;
        int end = random.nextInt(Math.min(slotsNumber, maxEnd) - minEnd) + minEnd;

        //System.out.println("-> " + start + "-" + end + "/" + energy);
        if (start + energy > slotsNumber)
            System.err.println("Start plus energy exceeds slots number");

        int inform = 0;
        if (start != 0)
            inform = random.nextInt(start - minDistance + 1);
        if (start == inform) {
            start = inform + 1;
            energy--;
        }
        int distance = random.nextInt(gridSize);
        int bid = random.nextInt(bidBound) + 1;

        pref.put("inform", inform);
        pref.put("start", start);
        pref.put("end", end);
        pref.put("energy", energy);
        pref.put("bid", bid);
        pref.put("distance", distance);


        int s_start = 0;
        if (start != 0)
            s_start = random.nextInt(start);
        int s_end = random.nextInt(slotsNumber - end) + end;
        int s_energy = random.nextInt(energy) + 1;
        int probability = random.nextInt(100) + 1;
        int rounds = random.nextInt(4);
        int temp = random.nextInt(81) + 120;
        double s_range = ((double)temp) / 100;
        strategy.put("start", s_start);
        strategy.put("end", s_end);
        strategy.put("energy", s_energy);
        strategy.put("probability", probability);
        strategy.put("rounds", rounds);
        strategy.put("range", s_range);
        if (delayed < evsNumber*delay) {
            strategy.put("delay", 1);
            delayed++;
        }
        else
            strategy.put("delay", 0);
        int priority = random.nextInt(1);
        if (priority == 0)
            strategy.put("priority", "price");
        else
            strategy.put("priority", "distance");
        ev.put("preferences", pref);
        ev.put("strategy", strategy);
        return ev;
    }

    public void generateSystemParameters (int slotsNumber, int gridSize) {
        try {
            FileWriter writer = new FileWriter("files/system.json");
            JSONObject rootObject = new JSONObject();
            rootObject.put("slots", slotsNumber);
            rootObject.put("gridSize", gridSize);
            writer.write(rootObject.toJSONString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateRandomStations (int stationsNumber, int maxChargers) {
        stationLocation = new int[stationsNumber][2];
        try {
            FileWriter writer = new FileWriter("files/station.json");

            JSONObject rootObject = new JSONObject();

            JSONObject stationsJSON = new JSONObject();

            Random random = new Random();

            for (int s = 0; s < stationsNumber; s++) {
                JSONObject station = new JSONObject();
                JSONObject location = new JSONObject();
                JSONObject flags = new JSONObject();
                station.put("id", s);
                station.put("chargers", random.nextInt(maxChargers) + 1);
                location.put("x", 0);
                location.put("y", 0);
                station.put("location", location);
                stationLocation[0][0] = 0;
                stationLocation[0][1] = 0;
                station.put("price_file", "station_"+s);
                flags.put("window", 0);
                flags.put("cplex", 0);
                flags.put("suggestion", 0);
                flags.put("instant", 1);
                flags.put("alternatives", 1);
                flags.put("virtual_demand", 1);
                station.put("flags", flags);
                stationsJSON.put("station" + s, station);
            }
            rootObject.put("stations", stationsJSON);
            writer.write(rootObject.toJSONString());
            writer.flush();
            writer.close();


            /*
            for (int s = 0; s < stationsNumber; s++) {
                System.out.println("station_" + s + " <" + stationLocation[s][0] + ", " + stationLocation[s][1] + ">");
            }
            */

            stationsGenerated = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generatePriceFile (int stationsNumber, int slotsNumber) {

        //Random random = new Random();
        for (int s = 0; s < stationsNumber; s++) {
            try {
                FileWriter writer = new FileWriter("files/price/station_" + s + ".txt");
                for (int i = 0; i < slotsNumber; i++) {
                    //writer.write("2," + String.valueOf(random.nextInt(5) + 1) + "\n");
                    writer.write("2," + "0\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void readStationFile(String path) {
        stationsGenerated = true;
        JSONParser parser = new JSONParser();
        try {
            System.out.println(path);
            Object obj = parser.parse(new FileReader("files/" + path));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject stationsRootJSON = (JSONObject) jsonObject.get("stations");

            ArrayList<JSONObject> stationsJSON = new ArrayList<>();
            for (Object key : stationsRootJSON.keySet()) {
                JSONObject stationJSON = (JSONObject) stationsRootJSON.get(key);
                stationsJSON.add(stationJSON);
            }

            stationLocation = new int[stationsJSON.size()][2];
            for (JSONObject stationJSON : stationsJSON) {
                int id = toIntExact((long) stationJSON.get("id"));

                JSONObject location = (JSONObject) stationJSON.get("location");
                stationLocation[id][0] = toIntExact((long) location.get("x"));
                stationLocation[id][1] = toIntExact((long) location.get("y"));
            }
            for (int s = 0; s < stationLocation.length; s++) {
                System.out.println("id: " + s + " -> x: " + stationLocation[s][0] + ", y: " + stationLocation[s][1]);
            }

        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }
    }
}
