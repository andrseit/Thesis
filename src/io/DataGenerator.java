package io;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import station.StationInfo;

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

    public void generateEVsFile () {
        if (!stationsGenerated)
            System.err.println("Please generate or read station file first!");
        else {
            stationsGenerated = false;
            this.generateEVs(5);
        }
    }


    private void generateEVs(int bidBound) {
        try {
            FileWriter writer = new FileWriter("evs.json");
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
                int minDistance = Integer.MAX_VALUE; // distances from the closest and the farthest station
                for (int st = 0; st < stationLocation.length; st++) {
                    int stationDistance = Math.abs(x - stationLocation[st][0]) + Math.abs(y - stationLocation[st][1]);
                    if (stationDistance < minDistance)
                        minDistance = stationDistance;
                    if (stationDistance > maxDistance)
                        maxDistance = stationDistance;
                }
                System.out.println("min: " + minDistance + ", max: " + maxDistance);

                // final destination
                int f_x = random.nextInt(gridSize);;
                int f_y = random.nextInt(gridSize);;
                while ((f_x == x) && (f_y == y)) {
                    f_x = random.nextInt(gridSize);
                    f_y = random.nextInt(gridSize);
                }
                ev.put("f_x", f_x);
                ev.put("f_y", f_y);


                // preferences
                int start = random.nextInt(slotsNumber - minDistance) + minDistance;
                int end = random.nextInt(slotsNumber - start) + start;
                int energy = random.nextInt(end - start + 1) + 1;
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
                if (start !=0)
                    s_start = random.nextInt(start);
                int s_end = random.nextInt(slotsNumber - end) + end;
                int s_energy = random.nextInt(energy) + 1;
                int probability = random.nextInt(100) + 1;
                int rounds = random.nextInt(5);
                strategy.put("start", s_start);
                strategy.put("end", s_end);
                strategy.put("energy", s_energy);
                strategy.put("probability", probability);
                strategy.put("rounds", rounds);

                ev.put("preferences", pref);
                ev.put("strategy", strategy);

                writer.write(ev.toJSONString()+"\n");
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateStationFile (int slots_num, int chargers_num) {

        try {
            FileWriter writer = new FileWriter("station.json");

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

    public void generateStationFile () {
        try {
            FileWriter writer = new FileWriter("station.json");
            writer.write(slotsNumber+"\n");

            JSONObject station = new JSONObject();
            JSONObject location = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 0);
            location.put("y", 0);
            station.put("location", location);
            stationLocation[0][0] = 0;
            stationLocation[0][1] = 0;
            writer.write(station.toJSONString()+"\n");

            station = new JSONObject();
            location = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 1);
            location.put("y", 0);
            stationLocation[1][0] = 1;
            stationLocation[1][1] = 0;
            station.put("location", location);
            writer.write(station.toJSONString()+"\n");

            station = new JSONObject();
            location = new JSONObject();
            station.put("chargers", 1);
            location.put("x", 1);
            location.put("y", 1);
            stationLocation[2][0] = 1;
            stationLocation[2][1] = 1;
            station.put("location", location);
            writer.write(station.toJSONString()+"\n");

            writer.flush();
            writer.close();


            for (int s = 0; s < stationsNumber; s++) {
                System.out.println("station_" + s + " <" + stationLocation[s][0] + ", " + stationLocation[s][1] + ">");
            }

            stationsGenerated = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readStationFile () {
        stationsGenerated = true;
        Reader reader;
        JSONParser parser = new JSONParser();
        try {
            reader = new FileReader("station.json");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
