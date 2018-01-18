package various;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Thesis on 20/12/2017.
 */
class DataGenerator {

    public void generateEVsFile(int evs_num, int slots_num, int bid_bound) {

        try {
            FileWriter writer = new FileWriter("evs.json");
            Random random = new Random();

            for (int i = 0; i < evs_num; i++) {
                JSONObject ev = new JSONObject();
                JSONObject pref = new JSONObject();
                JSONObject strategy = new JSONObject();

                int bid = random.nextInt(bid_bound) + 1;
                int start = random.nextInt(slots_num);
                int end = random.nextInt(slots_num - start) + start;
                int energy = random.nextInt(end - start + 1) + 1;
                int inform;
                if (start == 0) {
                    inform = 0;
                } else {
                    inform = random.nextInt(start);
                }
                pref.put("bid", bid);
                pref.put("start", start);
                pref.put("end", end);
                pref.put("energy", energy);
                pref.put("inform", inform);


                if (start != 0) {
                    start = random.nextInt(start);
                }
                end = random.nextInt(slots_num - end) + end;
                energy = random.nextInt(energy) + 1;
                int probability = random.nextInt(100) + 1;
                int rounds = random.nextInt(5);
                strategy.put("start", start);
                strategy.put("end", end);
                strategy.put("energy", energy);
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
