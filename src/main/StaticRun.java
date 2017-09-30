package main;

import evs.EV;
import station.Station;
import various.EVData;
import various.JSONFileParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Darling on 20/9/2017.
 */
public class StaticRun {

    private ArrayList<EV> evs;


    private void readEVsData () {
        JSONFileParser p = new JSONFileParser();
        ArrayList<EVData> evs_data = p.readEVsData();
        evs = new ArrayList<>();

        for (EVData e: evs_data) {
            EV ev = new EV();
            ev.setEnergy(e.getEnergy());
            ev.setInformTime(e.getInformSlot());
            for (Integer[] b: e.getBids()) {
                ev.addSlotsPreferences(b[0], b[1], b[2]);
            }
            evs.add(ev);
        }
    }

    public void offline () {

        System.out.println("Initializing station...");

        Station station = new Station();

        System.out.println("Reading evs from file...");

        this.readEVsData();

        for (EV ev: evs) {
            station.addEVBidder(ev);
        }

        station.printEVBidders();
        station.computeSchedule();
    }


    public void online () {

        this.readEVsData();
        Station station = new Station();
        int slots_number = station.getSlotsNumber();

        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getInformTime() - ev2.getInformTime();
            }
        });

        for (EV ev: evs) {
            queue.offer(ev);
        }


        //System.out.println(slots_number);
        for (int slot = 0; slot < slots_number; slot++) {

            if (!queue.isEmpty()) {
                while (queue.peek().getInformTime() == slot) {
                    station.addEVBidder(queue.poll());
                    if (queue.isEmpty())
                        break;
                }
            } else { break; }

            System.out.println("-------------------------- Slot " + slot + " -------------------------------");
            station.printEVBidders();
            station.computeSchedule();
        }
        station.printPayments();
    }

}
