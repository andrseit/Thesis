package various;

import station.EVInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.toIntExact;

/**
 * Created by Darling on 2/8/2017.
 */

public class JSONFileParser {

    private int num_slots;
    private int num_chargers;

    public void readStationData() {

        ArrayList<EVInfo> evs = new ArrayList<EVInfo>();
        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
            reader = new FileReader("station.json");
            Object object = parser.parse(reader);

            JSONObject json_ev = (JSONObject) object;

            num_slots = toIntExact((long) json_ev.get("slots"));
            num_chargers = toIntExact((long) json_ev.get("chargers"));

            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public void writeToFile () {
        JSONObject obj = new JSONObject();

        obj.put("id", 0);
        obj.put("energy", 3);

        JSONArray slots = new JSONArray();
        JSONObject slot = new JSONObject();
        slot.put("start", 0);
        slot.put("end", 3);
        slot.put("bid", 10);
        slots.add(slot);
        slot = new JSONObject();
        slot.put("start", 4);
        slot.put("end", 10);
        slot.put("bid", 5);
        slots.add(slot);

        obj.put("slots", slots);

        try {
            FileWriter file = new FileWriter("data.json");
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(obj.toJSONString());
    }

    public String getJSONStringEV (int bid, int start, int end, int energy) {

        JSONObject obj = new JSONObject();
        obj.put("energy", energy);
        obj.put("bid", bid);
        obj.put("start", start);
        obj.put("end", end);
        JSONArray slots = new JSONArray();


        return obj.toJSONString();
    }


    public ArrayList<EVData> readEVsData() {

        ArrayList<EVData> evs = new ArrayList<EVData>();
        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
            reader = new FileReader("data.json");
            BufferedReader in = new BufferedReader(reader);

            String line;
            while ((line = in.readLine()) != null) {

                Object object = parser.parse(line);

                JSONObject json_ev = (JSONObject) object;


                JSONObject preferences = (JSONObject) json_ev.get("preferences");

                int energy = toIntExact((long) preferences.get("energy"));
                int bid = toIntExact((long) preferences.get("bid"));

                //data.setEnergy(energy);

                int inform_slot = toIntExact((long) preferences.get("inform"));

                //data.setInformSlot(inform_slot);

                int start_slot = toIntExact((long) preferences.get("start"));
                int end_slot = toIntExact((long)(preferences.get("end")));

                JSONObject strategy = (JSONObject) json_ev.get("strategy");

                int s_energy = toIntExact((long) strategy.get("energy"));
                int s_start = toIntExact((long) strategy.get("start"));
                int s_end = toIntExact((long)(strategy.get("end")));
                int s_prob = toIntExact((long)(strategy.get("probability")));
                int s_rounds = toIntExact((long)(strategy.get("rounds")));

                EVData data = new EVData(energy, bid, start_slot, end_slot, inform_slot);
                data.setStrategy(s_start, s_end, s_energy, s_prob, s_rounds);
                data.setJSONString(line);
                evs.add(data);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return evs;
    }

    public EVInfo parseBidsString (String bids) {
        EVInfo ev = new EVInfo();
        try {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(bids);

            JSONObject json_ev = (JSONObject) object;

            int energy = toIntExact((long) json_ev.get("energy"));
            ev.setEnergy(energy);

            int start  = toIntExact((long) json_ev.get("start"));
            int end = toIntExact((long) json_ev.get("end"));
            int bid = toIntExact((long) json_ev.get("bid"));

            ev.addEVPreferences(start, end, bid, energy);

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        return ev;
    }

    public int getSlotsNumber() { return num_slots; }

    public int getChargersNumber() { return num_chargers; }
}
