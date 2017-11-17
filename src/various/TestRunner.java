package various;

import evs.EV;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import optimize.CPLEX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Thesis on 17/11/2017.
 */
public class TestRunner {


    private int[] price;
    private int[] chargers;
    private CPLEX c;
    private int slots;

    public void testRun () {
        JSONFileParser parser = new JSONFileParser();

        ArrayList<EVData> data = parser.readEVsData();
        ArrayList<EV> bidders = new ArrayList<>();

        for (EVData d: data) {
            System.out.println(d.toString());
            bidders.add(parser.parseBidsString(d.toString()));
        }

        for (EV ev: bidders) {
            System.out.println(ev.toString());
        }

        slots = 10;
        price = new int[slots];
        chargers = new int[slots];
        for (int s = 0; s < slots/2; s++) {
            price[s] = 1;
        }
        for (int s = slots/2; s < slots; s++) {
            price[s] = 2;
        }
        for (int s = 0; s < slots; s++) {
            chargers[s] = 1;
        }

        c = new CPLEX();
        c.model(bidders, slots, price, chargers, 0, 9);

        for (int i = 0; i < c.getScheduleMap().length; i++) {
            for (int j = 0; j < c.getScheduleMap()[i].length; j++) {
                System.out.print(c.getScheduleMap()[i][j] + " ");
            }
            System.out.println();
        }
        vcgPayments(bidders);
    }

    private void vcgPayments (ArrayList<EV> bidders) {
        int init_utility = c.getUtility();
        PriorityQueue<EV> queue = new PriorityQueue<EV>(10, new Comparator<EV>() {
            @Override
            public int compare(EV ev1, EV ev2) {
                return ev2.getPays() - ev1.getPays();
            }
        });

        for (EV ev: bidders) {
            // fill the queue
            queue.offer(ev);
        }

        if (queue.size() != 1) {
            while (!queue.isEmpty()) {
                EV removed = queue.poll();
                bidders.remove(removed);
                c.model(bidders, slots, price, chargers, 0, 9);
                int new_utility = c.getUtility();
                System.out.println("Initial: " + init_utility + ", new: " + new_utility + ", bid: " + removed.getBid());
                int pays = new_utility - (init_utility - removed.getBid()*removed.getEnergy());
                System.out.println("EV pays: " + pays);
                bidders.add(removed);
            }
        }

    }


    public void online() {

        Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");

        // Create a default profile
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.print("profile created\n");

        System.out.println("Launching a whole in-process platform..."+profile);
        AgentContainer mainContainer = rt.createMainContainer(profile);

        // now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        System.out.println("Launching the agent container ..."+pContainer);

        jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
        System.out.println("Launching the agent container after ..."+pContainer);

        System.out.println("containers created");

        try {

            mainContainer.createNewAgent("station", "agents.online.StationOnlineAgent", null).start();
            mainContainer.createNewAgent("clock", "agents.online.ClockAgent", null).start();
            Thread.sleep(500);
            JSONFileParser fp = new JSONFileParser();
            ArrayList<EVData> evs = fp.readEVsData();
            int counter = 0;
            for(EVData e: evs) {
                //EVData e = evs.get(0);
                Object[] arguments = new Object[3];
                arguments[0] = e.getEnergy();
                arguments[1] = e.getInformSlot();
                arguments[2] = e.getBids();
                mainContainer.createNewAgent("ev" + String.valueOf(counter), "agents.online.EVOnlineAgent", arguments).start();
                counter ++;
            }

            AgentController rma = mainContainer.createNewAgent("rma",
                    "jade.tools.rma.rma", new Object[0]);
            rma.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void offline () {

        Runtime rt = Runtime.instance();

// Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");

// Create a default profile
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.print("profile created\n");

        System.out.println("Launching a whole in-process platform..."+profile);
        AgentContainer mainContainer = rt.createMainContainer(profile);

// now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        System.out.println("Launching the agent container ..."+pContainer);

        jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
        System.out.println("Launching the agent container after ..."+pContainer);

        System.out.println("containers created");

        try {
            mainContainer.createNewAgent("station", "agents.offline.StationAgent", null).start();
            Thread.sleep(500);
            JSONFileParser fp = new JSONFileParser();
            ArrayList<EVData> evs = fp.readEVsData();
            int counter = 0;
            for(EVData e: evs) {
                Object[] arguments = new Object[5];
                arguments[0] = e.getEnergy();
                arguments[1] = e.getInformSlot();
                arguments[2] = e.getBid();
                arguments[3] = e.getStart();
                arguments[4] = e.getEnd();
                mainContainer.createNewAgent("ev" + String.valueOf(counter), "agents.offline.EVAgent", arguments).start();
                counter ++;
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
