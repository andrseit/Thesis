package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import various.EVData;
import various.JSONFileParser;

import java.util.ArrayList;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {


        int[][] array = new int[0][0];
        System.out.println(array.length);

        StaticRun sr = new StaticRun();
        //sr.online();
        //sr.offline();
        online();
    }


    private static void online() {

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

    private void offline () {

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
                Object[] arguments = new Object[3];
                arguments[0] = e.getEnergy();
                arguments[1] = e.getInformSlot();
                arguments[2] = e.getBids();
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
