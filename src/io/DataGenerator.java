package io;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Thesis on 20/12/2017.
 */
public class DataGenerator {



    public void generateEVsFile(int evsNumber, int slotsNumber, int bidBound, int gridSize) {

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
                int start = random.nextInt(slotsNumber);
                int end = random.nextInt(slotsNumber - start) + start;
                int energy = random.nextInt(end - start + 1) + 1;
                int inform = 0;
                if (start != 0)
                    inform = random.nextInt(start);
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
}
