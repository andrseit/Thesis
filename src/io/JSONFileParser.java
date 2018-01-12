package io;

import evs.EV;
import evs.strategy.Strategy;
import station.EVObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import station.StationInfo;

import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.toIntExact;

/**
 * Created by Darling on 2/8/2017.
 */

public class JSONFileParser {


    private int num_slots;

    public ArrayList<StationInfo> readStationData(String path) {

        ArrayList<StationInfo> stations = new ArrayList<>();
        int num_chargers;
        int x, y;
        int id = 0;
        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
            reader = new FileReader(path);
            BufferedReader in = new BufferedReader(reader);

            String line;

            line = in.readLine();
            num_slots = Integer.parseInt(line);

            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);

                JSONObject station_json = (JSONObject) object;

                num_chargers = toIntExact((long) station_json.get("chargers"));

                JSONObject location = (JSONObject) station_json.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));

                stations.add(new StationInfo(id, x, y, num_chargers));
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
        return stations;
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


    public ArrayList<EV> readEVsData(String path) {

        ArrayList<EV> evs = new ArrayList<>();
        JSONParser parser = new JSONParser();
        Reader reader = null;
        try {
            reader = new FileReader(path);
            BufferedReader in = new BufferedReader(reader);

            String line;
            int id = 0;
            while ((line = in.readLine()) != null) {

                Object object = parser.parse(line);

                JSONObject json_ev = (JSONObject) object;

                int x = toIntExact((long) json_ev.get("x"));
                int y = toIntExact((long) json_ev.get("y"));
                int f_x = toIntExact((long) json_ev.get("f_x"));
                int f_y = toIntExact((long) json_ev.get("f_y"));

                JSONObject preferences = (JSONObject) json_ev.get("preferences");

                int energy = toIntExact((long) preferences.get("energy"));
                int bid = toIntExact((long) preferences.get("bid"));

                //data.setEnergy(energy);

                int inform_slot = toIntExact((long) preferences.get("inform"));

                //data.setInformSlot(inform_slot);

                int start_slot = toIntExact((long) preferences.get("start"));
                int end_slot = toIntExact((long)(preferences.get("end")));
                int max_distance = toIntExact((long) (preferences.get("distance")));

                JSONObject strategy = (JSONObject) json_ev.get("strategy");

                int s_energy = toIntExact((long) strategy.get("energy"));
                int s_start = toIntExact((long) strategy.get("start"));
                int s_end = toIntExact((long)(strategy.get("end")));
                int s_prob = toIntExact((long)(strategy.get("probability")));
                int s_rounds = toIntExact((long)(strategy.get("rounds")));

                EV ev = new EV(id, x, y, f_x, f_y, start_slot, end_slot, energy, bid, max_distance,
                        new Strategy(s_energy, s_start, s_end, s_prob, s_rounds));
                evs.add(ev);
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

        return evs;
    }

    public EVObject parseBidsString (String bids) {
        EVObject ev = new EVObject();
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

    public int getSlotsNumber() {
        return num_slots;
    }
}
