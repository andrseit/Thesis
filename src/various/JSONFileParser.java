package various;

import evs.EV;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.log;
import static java.lang.Math.toIntExact;

/**
 * Created by Darling on 2/8/2017.
 */

public class JSONFileParser {

    private int num_slots;
    private int num_chargers;

    public void readStationData() {

        ArrayList<EV> evs = new ArrayList<EV>();
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

                int energy = toIntExact((long) json_ev.get("energy"));
                int bid = toIntExact((long) json_ev.get("bid"));

                //data.setEnergy(energy);

                int inform_slot = toIntExact((long) json_ev.get("inform"));

                //data.setInformSlot(inform_slot);

                int start_slot = toIntExact((long) json_ev.get("start"));
                int end_slot = toIntExact((long)(json_ev.get("end")));

                EVData data = new EVData(energy, bid, start_slot, end_slot, inform_slot);
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

    public EV parseBidsString (String bids) {
        EV ev = new EV();
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
