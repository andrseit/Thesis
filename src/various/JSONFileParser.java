package various;

import evs.EV;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.toIntExact;

/**
 * Created by Darling on 2/8/2017.
 */

public class JSONFileParser {

    private int num_slots;
    private int num_chargers;

    public void redStationData () {

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

    public String getJSONStringEV (ArrayList<Integer[]> bids, int energy) {

        JSONObject obj = new JSONObject();
        obj.put("energy", energy);

        JSONArray slots = new JSONArray();

        for (Integer[] b: bids) {

            JSONObject slot = new JSONObject();
            slot.put("start", b[0]);
            slot.put("end", b[1]);
            slot.put("bid", b[2]);
            slots.add(slot);

            System.out.print("Start: " + b[0] + " ");
            System.out.print("End: " + b[1] + " ");
            System.out.println("Bid: " + b[2]);
        }

        obj.put("bids", slots);
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
                EVData data = new EVData();
                Object object = parser.parse(line);

                JSONObject json_ev = (JSONObject) object;

                int energy = toIntExact((long) json_ev.get("energy"));

                data.setEnergy(energy);

                int inform_slot = toIntExact((long) json_ev.get("inform"));

                data.setInformSlot(inform_slot);

                //EV ev = new EV(id, energy);

                JSONArray slots = (JSONArray) json_ev.get("bids");

                @SuppressWarnings("unchecked")
                Iterator<JSONObject> it = slots.iterator();
                while (it.hasNext()) {
                    JSONObject json_slot = it.next();
                    int start  = toIntExact((long) json_slot.get("start"));
                    int end = toIntExact((long) json_slot.get("end"));
                    int bid = toIntExact((long) json_slot.get("bid"));


                    data.addBid(start, end, bid);
                    //ev.addSlotsPreferences(start, end, bid);
                }
                //evs.add(ev);
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
            System.out.println("-----------");
            System.out.println(energy);
            ev.setEnergy(energy);

            JSONArray slots = (JSONArray) json_ev.get("bids");

            @SuppressWarnings("unchecked")
            Iterator<JSONObject> it = slots.iterator();
            while (it.hasNext()) {
                JSONObject json_slot = it.next();
                int start  = toIntExact((long) json_slot.get("start"));
                int end = toIntExact((long) json_slot.get("end"));
                int bid = toIntExact((long) json_slot.get("bid"));
                System.out.println(start + ", " + end + ", " + bid);
                ev.addSlotsPreferences(start, end, bid);
            }
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        return ev;
    }

    public int getSlotsNumber() { return num_slots; }

    public int getChargersNumber() { return num_chargers; }
}
