package main;

import station.Station;
import various.ArrayTransformations;

/**
 * Created by Darling on 31/7/2017.
 */
public class SchedulingProcess {

    private Station station;

    public void test () {
        Station station = new Station();
        //station.getStationOccupancy();

        int[][] test_array = new int[5][10];
        test_array[0][1] = 1;
        test_array[2][2] = 1;
        test_array[1][1] = 1;
        test_array[2][2] = 1;
        test_array[1][4] = 1;
        test_array[1][0] = 1;
        test_array[4][7] = 1;
        test_array[2][7] = 1;
        test_array[1][6] = 1;
        test_array[2][4] = 1;
        test_array[3][8] = 1;
        test_array[0][9] = 1;
        System.out.println();
        System.out.println("=============");

        ArrayTransformations t = new ArrayTransformations();
        t.printIntArray(test_array);
        t.getColumnsCount(test_array);
        System.out.println("-------------");
        int[][] a = t.shrinkArray(test_array, 3);
        System.out.println(".");
        t.printIntArray(a);


    }

    /*
    public void offline () throws ParseException, org.json.simple.parser.ParseException, IOException {

        fr = new FileReaderEvs();
        station = new Station("data.txt");
        JSONFileParser json_parser = new JSONFileParser();
        ArrayList<EV> evs = json_parser.readEVsData();

        System.out.println("--------");


        int slots = station.getSlotsNumber();
        // starting process - at every slot make an auction
        //ArrayList<EV>[] sorted_evs = sortEVs();


        // apli periptwsi opou diavazei me ti mia ola ta bids
        System.out.println("----");
        station.setEVBidders(evs);
        station.computeSchedule();
//        int[] demand = station.stationDemand();
//        for (int i = 0; i < demand.length; i++) {
//            System.out.print(demand[i] + " ");
//        }


    }
    */

    /*
    private ArrayList<EV>[] sortEVs () {
        ArrayList<EV>[] evs = new ArrayList[station.getSlotsNumber()];
        ArrayList<EV> evs_list = fr.getEvsList();

        Collections.sort(evs_list, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev1.getStartSlot() - ev2.getStartSlot();
            }
        });

        for (int s = 0; s < station.getSlotsNumber(); s++) {
            evs[s] = new ArrayList<EV>();
            for(EV ev: evs_list) {
                if(ev.getStartSlot() == s) {
                    evs[s].add(ev);
                }
            }
        }

        for (int s = 0; s < station.getSlotsNumber(); s++) {
            System.out.println(s);
            for (EV ev: evs[s]) {
                ev.printEV();
            }
        }

        return evs;
    }
    */
}
