package io;

import evs.EV;
import evs.strategy.Strategy;
import new_classes.Station;
import optimize.AlternativesCPLEX;
import optimize.ProfitCPLEX;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import station.EVObject;
import station.StationInfo;
import station.auction.OptimalSchedule;
import station.communication.StationReceiver;
import station.offline.SimpleStation;
import station.online.SimpleOnlineStation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;

public class JSONFileParser {

    private int slotsNumber;

    public ArrayList<Station> readStations (String path) {
        ArrayList<Station> stations = new ArrayList<>();
        int chargersNumber, x, y, id = 0;

        JSONParser parser = new JSONParser();
        Reader reader;
        try {
            reader = new FileReader("files/" + path);
            BufferedReader in = new BufferedReader(reader);

            String line;

            line = in.readLine();
            slotsNumber = Integer.parseInt(line);

            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);

                JSONObject station_json = (JSONObject) object;

                chargersNumber = toIntExact((long) station_json.get("chargers"));

                JSONObject location = (JSONObject) station_json.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));
                String pricePath = station_json.get("price_file").toString();

                JSONObject flagsObject = (JSONObject) station_json.get("flags");
                HashMap<String, Integer> flags = new HashMap<>();
                flags.put("window", toIntExact((long) flagsObject.get("window")));
                flags.put("suggestion", toIntExact((long) flagsObject.get("suggestion")));
                flags.put("cplex", toIntExact((long) flagsObject.get("cplex")));

                System.out.println(pricePath);
                StationPricing pr = setPrice(pricePath);
                // setting the same optimizer to all stations - change that later
                stations.add(new Station(id, x, y, chargersNumber, new OptimalSchedule(new ProfitCPLEX()), new AlternativesCPLEX(), pr.getPrice(), slotsNumber));
                id++;
            }
            reader.close();

        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }

        return stations;
    }



    public ArrayList<SimpleStation> readOfflineStations (String path) {
        ArrayList<SimpleStation> stations = new ArrayList<>();
        int num_chargers;
        int x, y;
        int id = 0;
        JSONParser parser = new JSONParser();
        Reader reader;
        try {
            reader = new FileReader("files/" + path);
            BufferedReader in = new BufferedReader(reader);

            String line;

            line = in.readLine();
            slotsNumber = Integer.parseInt(line);

            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);

                JSONObject station_json = (JSONObject) object;

                num_chargers = toIntExact((long) station_json.get("chargers"));

                JSONObject location = (JSONObject) station_json.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));
                String pricePath = station_json.get("price_file").toString();

                JSONObject flagsObject = (JSONObject) station_json.get("flags");
                HashMap<String, Integer> flags = new HashMap<>();
                flags.put("window", toIntExact((long) flagsObject.get("window")));
                flags.put("suggestion", toIntExact((long) flagsObject.get("suggestion")));
                flags.put("cplex", toIntExact((long) flagsObject.get("cplex")));

                StationPricing pr = setPrice(pricePath);
                //stations.add(new SimpleStation(new StationInfo(id, x, y, num_chargers), slotsNumber, pr.getPrice(), pr.getRenewables(), flags));
                id++;
            }
            reader.close();

        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    public ArrayList<SimpleOnlineStation> readOnlineStations (String path) {
        ArrayList<SimpleOnlineStation> stations = new ArrayList<>();
        int num_chargers;
        int x, y;
        int id = 0;
        JSONParser parser = new JSONParser();
        Reader reader;
        try {
            reader = new FileReader("files/" + path);
            BufferedReader in = new BufferedReader(reader);

            String line;

            line = in.readLine();
            slotsNumber = Integer.parseInt(line);

            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);

                JSONObject station_json = (JSONObject) object;

                num_chargers = toIntExact((long) station_json.get("chargers"));

                JSONObject location = (JSONObject) station_json.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));
                String pricePath = station_json.get("price_file").toString();

                JSONObject flagsObject = (JSONObject) station_json.get("flags");
                HashMap<String, Integer> flags = new HashMap<>();
                flags.put("window", toIntExact((long) flagsObject.get("window")));
                flags.put("suggestion", toIntExact((long) flagsObject.get("suggestion")));
                flags.put("cplex", toIntExact((long) flagsObject.get("cplex")));
                flags.put("instant", toIntExact((long) flagsObject.get("instant")));



                StationPricing pr = setPrice(pricePath);
                //stations.add(new SimpleOnlineStation(new StationInfo(id, x, y, num_chargers), slotsNumber, pr.getPrice(), pr.getRenewables(), flags));
                id++;
            }
            reader.close();

        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    public ArrayList<StationInfo> readStationData(String path) {

        ArrayList<StationInfo> stations = new ArrayList<>();
        int num_chargers;
        int x, y;
        int id = 0;
        JSONParser parser = new JSONParser();
        Reader reader;
        try {
            reader = new FileReader(path);
            BufferedReader in = new BufferedReader(reader);

            String line;

            line = in.readLine();
            slotsNumber = Integer.parseInt(line);

            while ((line = in.readLine()) != null) {
                Object object = parser.parse(line);

                JSONObject station_json = (JSONObject) object;

                num_chargers = toIntExact((long) station_json.get("chargers"));

                JSONObject location = (JSONObject) station_json.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));

                //stations.add(new StationInfo(id, x, y, num_chargers));
                id++;
            }


            reader.close();

        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    private StationPricing setPrice (String path) {
        int[] price = new int[slotsNumber];
        int[] renewables = new int[slotsNumber];
        try {
            BufferedReader in = new BufferedReader(new FileReader("files/price/" + path + ".txt"));
            String line;
            for (int s = 0; s < slotsNumber; s++) {
                line = in.readLine();
                String[] tokens = line.split(",");
                price[s] = Integer.parseInt(tokens[0]);
                renewables[s] = Integer.parseInt(tokens[1]);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StationPricing(price, renewables);
    }

    public void writeToFile() {
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

        //System.out.println(obj.toJSONString());
    }

    public String getJSONStringEV(int bid, int start, int end, int energy) {

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
        Reader reader;
        try {
            reader = new FileReader("files/" + path);
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
                int end_slot = toIntExact((long) (preferences.get("end")));
                int max_distance = toIntExact((long) (preferences.get("distance")));

                JSONObject strategy = (JSONObject) json_ev.get("strategy");

                int s_energy = toIntExact((long) strategy.get("energy"));
                int s_start = toIntExact((long) strategy.get("start"));
                int s_end = toIntExact((long) (strategy.get("end")));
                int s_prob = toIntExact((long) (strategy.get("probability")));
                int s_rounds = toIntExact((long) (strategy.get("rounds")));
                double s_range = Double.parseDouble(strategy.get("range").toString());
                String s_priority = strategy.get("priority").toString();

                EV ev = new EV(id, inform_slot, x, y, f_x, f_y, start_slot, end_slot, energy, bid, max_distance,
                        new Strategy(s_energy, s_start, s_end, s_range, s_prob, s_rounds, s_priority));
                evs.add(ev);
                id++;
            }
            reader.close();
        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }

        return evs;
    }

    public EVObject parseBidsString(String bids) {
        EVObject ev = new EVObject();
        try {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(bids);

            JSONObject json_ev = (JSONObject) object;

            int energy = toIntExact((long) json_ev.get("energy"));
            ev.setEnergy(energy);

            int start = toIntExact((long) json_ev.get("start"));
            int end = toIntExact((long) json_ev.get("end"));
            int bid = toIntExact((long) json_ev.get("bid"));

            ev.addEVPreferences(start, end, bid, energy);

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        return ev;
    }

    public int getSlotsNumber() {
        return slotsNumber;
    }

    private class StationPricing {
        private int[] price;
        private int[] renewables;

        public StationPricing(int[] price, int[] renewables) {
            this.price = price;
            this.renewables = renewables;
        }

        public int[] getPrice() {
            return price;
        }

        public int[] getRenewables() {
            return renewables;
        }
    }
}
