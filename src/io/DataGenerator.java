package io;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.Random;

import static java.lang.Math.toIntExact;

/**
 * Created by Thesis on 20/12/2017.
 */
public class DataGenerator {

    private int stationsNumber;
    private int[][] stationLocation;
    private int evsNumber;
    private int slotsNumber;
    private int gridSize;
    private boolean stationsGenerated;

    public DataGenerator(int stationsNumber, int evsNumber, int slotsNumber, int gridSize) {
        this.stationsNumber = stationsNumber;
        this.evsNumber = evsNumber;
        this.slotsNumber = slotsNumber;
        this.gridSize = gridSize;
        stationLocation = new int[stationsNumber][2];
    }

    public void generateEVsFile(int minEnergy, int maxEnergy, double sEnergy, double windowLength) {
        if (!stationsGenerated)
            System.err.println("Please generate or read agents.station file first!");
        else {
            stationsGenerated = false;
            this.generateEVs(5, minEnergy, maxEnergy, sEnergy, windowLength);
        }
    }


    private void generateEVs(int bidBound, int minEnergy, int maxEnergy, double sEnergy, double windowLength) {
        try {
            FileWriter writer = new FileWriter("files/evs.json");
            Random random = new Random();

            for (int i = 0; i < evsNumber; i++) {
                JSONObject ev = new JSONObject();
                JSONObject pref = new JSONObject();
                JSONObject strategy = new JSONObject();

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

                writer.write(ev.toJSONString() + "\n");
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateStationFile(int slots_num, int chargers_num) {

        try {
            FileWriter writer = new FileWriter("files/station.json");

            JSONObject station = new JSONObject();
            station.put("slots", slots_num);
            station.put("chargers", chargers_num);
            writer.write(station.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateStationFile() {
        try {
            FileWriter writer = new FileWriter("files/station.json");
            writer.write(slotsNumber + "\n");

            JSONObject station = new JSONObject();
            JSONObject location = new JSONObject();
            JSONObject flags = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 0);
            location.put("y", 0);
            station.put("location", location);
            stationLocation[0][0] = 0;
            stationLocation[0][1] = 0;
            station.put("price_file", "station_0");
            flags.put("window", 0);
            flags.put("cplex", 0);
            flags.put("suggestion", 0);
            flags.put("instant", 1);
            station.put("flags", flags);
            writer.write(station.toJSONString() + "\n");

            station = new JSONObject();
            location = new JSONObject();
            flags = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 1);
            location.put("y", 0);
            stationLocation[1][0] = 1;
            stationLocation[1][1] = 0;
            station.put("location", location);
            station.put("price_file", "station_1");
            flags.put("window", 0);
            flags.put("cplex", 0);
            flags.put("suggestion", 0);
            flags.put("instant", 1);
            station.put("flags", flags);
            writer.write(station.toJSONString() + "\n");

            station = new JSONObject();
            location = new JSONObject();
            flags = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 1);
            location.put("y", 1);
            stationLocation[2][0] = 1;
            stationLocation[2][1] = 1;
            station.put("location", location);
            station.put("price_file", "station_0");
            flags.put("window", 0);
            flags.put("cplex", 0);
            flags.put("suggestion", 0);
            flags.put("instant", 1);
            station.put("flags", flags);
            writer.write(station.toJSONString() + "\n");

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

    public void generateRandomStations (int maxChargers) {
        try {
            FileWriter writer = new FileWriter("files/station.json");
            writer.write(slotsNumber + "\n");

            Random random = new Random();

            for (int s = 0; s < stationsNumber; s++) {
                JSONObject station = new JSONObject();
                JSONObject location = new JSONObject();
                JSONObject flags = new JSONObject();
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
                station.put("flags", flags);
                writer.write(station.toJSONString() + "\n");
            }
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

    public void generatePriceFile () {

        Random random = new Random();
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

    public void readStationFile() {
        stationsGenerated = true;
        Reader reader;
        JSONParser parser = new JSONParser();
        try {
            reader = new FileReader("files/station.json");
            BufferedReader in = new BufferedReader(reader);
            String line;
            // read slots
            in.readLine();
            int id = 0;
            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);
                JSONObject station_json = (JSONObject) object;
                JSONObject location = (JSONObject) station_json.get("location");
                stationLocation[id][0] = toIntExact((long) location.get("x"));
                stationLocation[id][1] = toIntExact((long) location.get("y"));
                id++;
            }
            reader.close();
        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }


    }

    public int getSlotsNumber () {
        return slotsNumber;
    }
}
