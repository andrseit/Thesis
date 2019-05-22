package io;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.toIntExact;

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

    private void generateEVs(int evsNumber, int bidBound, int minEnergy, int maxEnergy, double sEnergy, double windowLength, int slotsNumber, int gridSize) {
        try {
            FileWriter writer = new FileWriter("files/evs.json");
            Random random = new Random();
            JSONObject rootObject = new JSONObject();
            JSONObject evs = new JSONObject();


            for (int i = 0; i < evsNumber; i++) {
                JSONObject ev = new JSONObject();
                JSONObject pref = new JSONObject();
                JSONObject strategy = new JSONObject();

                ev.put("id", i);
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
                ev.put("f_x", f_x);
                ev.put("f_y", f_y);

                // preferences
                int energy = random.nextInt(maxEnergy - minEnergy + 1) + minEnergy;
                int start = random.nextInt(slotsNumber - energy - minDistance) + minDistance;
                int minEnd = start + energy - 1;
                int maxEnd = start + (int)(energy * windowLength) + 1;
                int end = random.nextInt(Math.min(slotsNumber, maxEnd) - minEnd) + minEnd;

                int inform = 0;
                if (start != 0)
                    inform = random.nextInt(start - minDistance + 1);
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
                int priority = random.nextInt(1);
                if (priority == 0)
                    strategy.put("priority", "price");
                else
                    strategy.put("priority", "distance");
                ev.put("preferences", pref);
                ev.put("strategy", strategy);

                evs.put("ev" + i, ev);

            }
            rootObject.put("evs", evs);
            writer.write(rootObject.toJSONString() + "\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
