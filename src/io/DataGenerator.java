package io;

import evs.EV;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Darling on 29/7/2017.
 */
public class DataGenerator {

    public void generateEVsData(int num_evs, int slots_num) {

        ArrayList<JSONObject> evs = new ArrayList<JSONObject>();
        Random rand = new Random();
        for(int ev = 0; ev < num_evs; ev++) {

            JSONObject ev_object = new JSONObject();

            int id = ev;
            int energy = 1 + rand.nextInt(4);
            ev_object.put("id", id);
            ev_object.put("energy", energy);
            // generate random number of bids
            JSONArray bids = new JSONArray();
            int bids_num = 2 + rand.nextInt(5);
            System.out.println("Bids number: " + bids_num + " energy: " + energy);
            int last_end_slot = -1;
            for (int b = 0; b < bids_num; b++) {
                JSONObject bid_object = new JSONObject();

                int min_start_slot = last_end_slot + 1;
                int max_start_slot = slots_num - energy - 1;

                System.out.println("last: " + last_end_slot + " final: " + (max_start_slot - last_end_slot + 1));
                if (last_end_slot + energy >= slots_num || max_start_slot - min_start_slot <= 0)
                    break;

                int start_slot = min_start_slot + rand.nextInt(max_start_slot - min_start_slot + 1);
                System.out.println("start slot: " + start_slot);
                if (start_slot + energy >= slots_num)
                    break;

                int gap = 5;
                int min_end_slot = start_slot + energy - 1;
                int max_end_slot = slots_num - energy;
                if (min_end_slot + gap >= slots_num)
                    max_end_slot = slots_num - 1;

                int end_slot = min_end_slot + rand.nextInt( max_end_slot - min_end_slot + 1);
                System.out.println("bid: " + b + ": start = " + start_slot + ", end = " + end_slot);
                last_end_slot = end_slot;
                int bid = 1 + rand.nextInt(10);
                bid_object.put("start", start_slot);
                bid_object.put("end", end_slot);
                bid_object.put("bid", bid);
                bids.add(bid_object);
            }
            System.out.println("/////////////////////////");
            ev_object.put("bids", bids);

            evs.add(ev_object);
        }
        writeToFile(evs);
    }

    private void writeToFile(ArrayList<JSONObject> evs){
        String path = "data.json";
        try {
            FileWriter writer = new FileWriter(path);

            for (JSONObject ev: evs) {
                writer.write(ev.toJSONString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
